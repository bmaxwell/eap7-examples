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

import com.redhat.middleware.eap.examples.EJBClientUtil;
import com.redhat.middleware.eap.examples.Server;

/**
 *
 */
public class TestConfig implements Serializable {

    private Server server = new Server();
    private String ejbLookup = "ejb:/ejb-over-http-server/HelloEJB1!com.redhat.middleware.eap.examples.HelloRemote";
    private int numThreads = 4;
    private long numInvocations = 100;

    public int getNumThreads() {
        return numThreads;
    }
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }
    public long getNumInvocations() {
        return numInvocations;
    }
    public void setNumInvocations(long numInvocations) {
        this.numInvocations = numInvocations;
    }
    public Server getServer() {
        return server;
    }
    public void setServer(Server server) {
        this.server = server;
    }
    public String getEjbLookup() {
        return ejbLookup;
    }
    public void setEjbLookup(String ejbLookup) {
        this.ejbLookup = ejbLookup;
    }

    @Override
    public String toString() {
        return String.format("TestConfig: numThreads=%d numInvocations=%d EJBInfo: %s", numThreads, numInvocations, EJBClientUtil.getConfigInfo(server, ejbLookup));
    }
}