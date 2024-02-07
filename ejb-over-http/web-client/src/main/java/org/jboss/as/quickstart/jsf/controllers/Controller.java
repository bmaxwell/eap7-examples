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
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Model;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.jboss.as.quickstart.jsf.model.TestConfig;
import org.jboss.logging.Logger;

import com.redhat.middleware.eap.examples.EJBClientUtil;
import com.redhat.middleware.eap.examples.HelloRemote;

/**
 * The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an EL name
 */
@SessionScoped
@Model
public class Controller implements Serializable {

    private Logger log = Logger.getLogger("Controller");

    @Inject
    private FacesContext facesContext;

    @Resource
    private ManagedThreadFactory managedThreadFactory;

    private TestConfig testConfig = new TestConfig();
    private String response;

    private List<Thread> threads = null;
    private List<InvocationRunnable> invocationRunnables = null;

    @PostConstruct
    public void init() {
    }

    public String getResponse() {
        return response;
    }

    private static class InvocationRunnable implements Runnable {

        private Logger logger = Logger.getLogger("InvocationRunnable");
        private TestConfig testConfig;
        private long invocation = 0L;
        private boolean running = true;

        public InvocationRunnable(TestConfig testConfig) {
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

    public String invoke() {

        if (threads == null) {
            // not currently running test
            threads = new ArrayList<Thread>();
            invocationRunnables = new ArrayList<Controller.InvocationRunnable>();
            // create threads
            for (int i = 0; i < testConfig.getNumThreads(); i++) {
                InvocationRunnable runnable = new InvocationRunnable(testConfig);
                invocationRunnables.add(runnable);
                Thread thread = managedThreadFactory.newThread(runnable);
                thread.setName(String.format("InvocationRunnables-%d", i));
                threads.add(thread);
            }

            // start threads
            threads.forEach(t -> t.start());

            response = String.format("Started threads: %s", testConfig);
        } else {
            // already / still running
            checkRunning();
            response = String.format("Already running test with: %s", testConfig);
        }

        return "";
    }

    public String stopRunning() {
        if (invocationRunnables != null) {
            invocationRunnables.forEach(r -> r.setRunning(false));
            checkRunning();
        } else {
            response = "no threads running";
        }
        return "";
    }

    public String checkRunning() {

        if (threads != null) {
            Integer alive = 0;
            for (int i = 0; i < threads.size(); i++) {
                if (threads.get(i).isAlive())
                    alive++;
            }

            // if none alive, then clear out the lists
            if(alive < 1) {
                threads = null;
                invocationRunnables = null;
            }

            response = String.format("%d threads alive and running", alive);
        } else {
            response = "no threads running";
        }

        return "";
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
}