/*
 * Copyright 2024 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.quickstart.jsf.model;

import java.io.Serializable;

import org.jboss.logging.Logger;

/**
 *
 */
public class RunnableToggle implements Serializable {

    private static Logger logger = Logger.getLogger("RunnableToggle");
    private String name;
    private boolean running = false;
    private StatusChangeEvent preCallback;
    private StatusChangeEvent postCallback;
    private Status status = Status.Stopped;

    public enum Status {
        Stopped, Starting, Running, Stopping
    }

    public static interface StatusChangeEvent {
        // throws exception if the new value cannot be set
        public void statusChange(Status newValue) throws Exception;
    }

    public RunnableToggle(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {

        if(preCallback != null) {
            try {
                preCallback.statusChange(status);
                this.status = status;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            this.status = status;
        }

        if(postCallback != null) {
            try {
                postCallback.statusChange(this.status);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public String getStatus() {
        return status.toString();
    }

    public void starting() {
        setStatus(Status.Starting);
    }

    public void started() {
        setStatus(Status.Running);
    }

    public void stopping() {
        setStatus(Status.Stopping);
    }

    public void stopped() {
        setStatus(Status.Stopped);
    }

    public boolean isStarting() {
        return this.status == Status.Starting;
    }

    public boolean isRunning() {
        return this.status == Status.Running;
    }

    public boolean isStopping() {
        return this.status == Status.Stopping;
    }

    public boolean isStopped() {
        return this.status == Status.Stopped;
    }

    public String getLabel() {
        switch (this.status) {
            case Stopped:
                return "Start";
            case Running:
                return "Stop";
            default:
                return this.status.toString();
        }
    }

    public boolean isDisabled() {
        return this.status == Status.Stopping || this.status == Status.Starting;
    }

    // public boolean isRunning() {
    // return running;
    // }
    //
    // public void setRunning(boolean running) {
    // this.running = running;
    // }

    public void setPreCallback(StatusChangeEvent preCallback) {
        this.preCallback = preCallback;
    }

    public void setPostCallback(StatusChangeEvent postCallback) {
        this.postCallback = postCallback;
    }

    @Override
    public String toString() {
        return String.format("RunnableToggle name=%s running=%s", name, running);
    }
}