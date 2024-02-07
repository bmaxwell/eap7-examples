/*
 * Copyright 2023 Red Hat, Inc.
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

package com.redhat.middleware.eap.examples;

/**
 * @author bmaxwell
 *
 */
public class Server {

    private static final String DEFAULT_HOST = "localhost";
    private static final Integer DEFAULT_PORT = 8080;

    private String host = DEFAULT_HOST;
    private Integer port = DEFAULT_PORT;
    private String username = "ejbuser";
    private String password = "redhat1!";

    public Server() {
    }

    public Server(String host) {
        this(host, DEFAULT_PORT, null, null);
    }

    public Server(String host, Integer port) {
        this(host, port, null, null);
    }

    public Server(String host, Integer port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return String.format("Server: host=%s port=%d username=%s password=%s", host, port, username, password);
    }
}