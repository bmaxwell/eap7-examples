/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstart.jsf.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.jboss.as.quickstart.jsf.model.LoggerToggle;
import org.jboss.as.quickstart.jsf.model.LoggerToggle.ToggleEvent;
import org.jboss.as.quickstart.jsf.model.RunnableToggle;
import org.jboss.as.quickstart.jsf.model.RunnableToggle.Status;
import org.jboss.as.quickstart.jsf.model.RunnableToggle.StatusChangeEvent;
import org.jboss.as.quickstart.jsf.model.TestConfig;
import org.jboss.logging.Logger;

import com.redhat.middleware.eap.examples.EJBClientUtil;
import com.redhat.middleware.eap.examples.HelloRemote;

/**
 * The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an EL name
 */
@ViewScoped
@Model
public class Controller implements Serializable {

    private Logger logger = Logger.getLogger("Controller");

    @Inject
    private FacesContext facesContext;

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    @Inject
    @Push
    private PushContext push;

    private TestConfig testConfig = new TestConfig();
    private String response;
    private String status;
    // private boolean running = false;
    private RunnableToggle runnableToggle = new RunnableToggle("TestRunning");

    private StatusRunnable statusRunnable;
    private Thread statusThread;
    private int statusCheckInterval = 5; // seconds
    private List<Thread> threads = null;
    private List<InvocationRunnable> invocationRunnables = null;

    private LoggerToggle webLogger = new LoggerToggle("WebLogger");
    private LoggerToggle ejbLogger = new LoggerToggle("EJBLogger");

    @PostConstruct
    public void init() {
        runnableToggle.setPostCallback(new StatusChangeEvent() {
            @Override
            public void statusChange(Status newValue) throws Exception {
                pushUpdate();
            }
        });
    }

    public String getResponse() {
        return response;
    }

    private static class StatusRunnable implements Runnable {

        private static final Logger logger = Logger.getLogger("StatusThread");
        private boolean running = true;
        private Controller controller;
        private int interval = 5;

        StatusRunnable(Controller controller, int statusCheckInterval) {
            this.controller = controller;
            this.interval = statusCheckInterval;
        }

        @Override
        public void run() {
            logger.info("initialized");

            while (running) {
                try {
                    controller.pushUpdate();
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException ie) {
                }
            }
        }

        public void stop() {
            controller.pushUpdate();
            this.running = false;
            Thread.currentThread().interrupt();
        }
    }

    private static class InvocationRunnable implements Runnable {

        private Logger logger = Logger.getLogger("InvocationRunnable");
        private TestConfig testConfig;
        private Controller controller;
        private long invocation = 0L;
        private boolean running = true;

        public InvocationRunnable(Controller controller, TestConfig testConfig) {
            this.controller = controller;
            this.testConfig = testConfig;
        }

        @Override
        public void run() {

            String threadName = Thread.currentThread().getName();
            String configInfo = EJBClientUtil.getConfigInfo(testConfig.getServer(), testConfig.getEjbLookup());
            String response;

            if (testConfig.getNumInvocations() < 1) {
                // run until stopped
                while (running) {
                    try {
                        invocation++;
                        HelloRemote ejb = EJBClientUtil.getEJBRemote(testConfig.getServer(), testConfig.getEjbLookup());
                        response = ejb.hello();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        response = String.format("%s: %s", t.getClass().getName(), t.getMessage());
                    }

                    if (controller.getWebLogger().isLogging())
                        logger.infof("%s invocation=%d %s %s", threadName, invocation, configInfo, response);
                }
            } else {
                // run for the number of invocations
                for (int i = 0; (i < testConfig.getNumInvocations()) && running; i++) {
                    try {
                        invocation++;
                        HelloRemote ejb = EJBClientUtil.getEJBRemote(testConfig.getServer(), testConfig.getEjbLookup());
                        response = ejb.hello();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        response = String.format("%s: %s", t.getClass().getName(), t.getMessage());
                    }
                    if (controller.getWebLogger().isLogging())
                        logger.infof("%s invocation=%d %s %s", threadName, invocation, configInfo, response);
                }
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public long getInvocation() {
            return invocation;
        }
    }

    public String toggleStartStop() {
        // disabled if Starting or Stopping, so do nothing
        // else call start or stop
        if (!runnableToggle.isDisabled()) {
            if (runnableToggle.isRunning())
                stop();
            else if (runnableToggle.isStopped())
                start();
        }
        return "";
    }

    public String start() {

        if (runnableToggle.isStopped() && threads == null) {
            // not currently running test

            runnableToggle.starting();

            // set the callback on the ejblogger , this is done when invoke is called in case the host/port changes
            ejbLogger.setCallback(new ToggleEvent() {

                @Override
                public void toggle(boolean newValue) throws Exception {
                    // try to call the ejb to change the logging value, if it fails then throw an exception so the LoggerToggle
                    // will not toggle values
                    EJBClientUtil.getEJBRemote(testConfig.getServer(), testConfig.getEjbLookup()).setLogging(newValue);
                }
            });

            threads = new ArrayList<Thread>();
            invocationRunnables = new ArrayList<Controller.InvocationRunnable>();
            // create threads
            for (int i = 0; i < testConfig.getNumThreads(); i++) {
                InvocationRunnable runnable = new InvocationRunnable(this, testConfig);
                invocationRunnables.add(runnable);
                Thread thread = managedThreadFactory.newThread(runnable);
                thread.setName(String.format("InvocationRunnables-%d", i));
                threads.add(thread);
            }

            // start threads
            threads.forEach(t -> t.start());

            // start the status thread
            this.statusRunnable = new StatusRunnable(this, statusCheckInterval);
            this.statusThread = managedThreadFactory.newThread(this.statusRunnable);
            this.statusThread.start();

            response = String.format("Started threads: %s", testConfig);
            runnableToggle.started();

        } else {
            // already / still running
            response = String.format("Already running test with: %s", testConfig);
        }

        return "";
    }

    public String stop() {
        if (invocationRunnables != null) {

            runnableToggle.stopping();
            invocationRunnables.forEach(r -> r.setRunning(false));

        } else {
            response = "no threads running";
        }
        return "";
    }

    private String checkThreadStatus() {
        if (threads != null) {
            Integer alive = 0;
            for (int i = 0; i < threads.size(); i++) {
                if (threads.get(i).isAlive())
                    alive++;
            }

            // if none alive, then clear out the lists
            if (alive < 1) {
                threads = null;
                invocationRunnables = null;
                this.statusRunnable.stop();
                this.statusThread = null;
                runnableToggle.stopped();
            }

            return String.format("%d threads alive and running", alive);
        } else {
            return "no threads running";
        }
    }

    private void pushUpdate() {
        this.status = checkThreadStatus();
        logger.info("status: " + this.status);
        push.send("status");
    }

    private String getRootErrorMessage(Exception e) {
        // Default to general error message that registration failed.
        String errorMessage = "Registration failed. See server log for more information";
        if (e == null) {
            // This shouldn't happen, but return the default messages
            return errorMessage;
        }

        // Start with the exception and recurse to find the root cause
        Throwable t = e;
        while (t != null) {
            // Get the message from the Throwable class instance
            errorMessage = t.getLocalizedMessage();
            t = t.getCause();
        }
        // This is the root cause message
        return errorMessage;
    }

    public TestConfig getTestConfig() {
        return testConfig;
    }

    public void setTestConfig(TestConfig testConfig) {
        this.testConfig = testConfig;
    }

    public String getStatus() {
        return status;
    }

    public LoggerToggle getWebLogger() {
        return webLogger;
    }

    public LoggerToggle getEjbLogger() {
        return ejbLogger;
    }

    public RunnableToggle getRunnableToggle() {
        return runnableToggle;
    }

    private static void sleep() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }
    }
}