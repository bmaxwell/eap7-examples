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

/**
 *
 */
public class LoggerToggle implements Serializable {

    private String name;
    private boolean isLogging = true;
    private ToggleEvent callback;

    public static interface ToggleEvent {
        // throws exception if the new value cannot be set
        public void toggle(boolean newValue) throws Exception;
    }

    public LoggerToggle(String name) {
        this.name = name;
    }

    public void toggle() {

        if (callback != null) {
            try {
                boolean newValue = !isLogging;
                callback.toggle(newValue);
                this.isLogging = newValue;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            this.isLogging = !isLogging;
        }
    }

    public String getLabel() {
        return isLogging ? "Off" : "On";
    }

    public boolean isLogging() {
        return isLogging;
    }

    public void setLogging(boolean isLogging) {
        this.isLogging = isLogging;
    }

    public void setCallback(ToggleEvent callback) {
        this.callback = callback;
    }

    @Override
    public String toString() {
        return String.format("LoggerToggle name=%s isLogging=%s", name, isLogging);
    }
}