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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

/**
 * @author bmaxwell
 *
 */
public class EJBClientUtil {

    public static final String APP = "";
    public static final String MODULE = "ejb-helloworld-server";
    public static final String IFACE = "com.redhat.middleware.eap.examples.HelloRemote";

    public static final String EJB1 = "HelloEJB1";
    public static final String EJB2 = "HelloEJB2";

    public static final String EJB1_NAMING_PREFIX = String.format("%s/%s/%s!%s", APP, MODULE, EJB1, IFACE);
    public static final String EJB1_EJB_PREFIX = String.format("ejb:%s", EJB1_NAMING_PREFIX);

    public static final String EJB2_NAMING_PREFIX = String.format("%s/%s/%s!%s", APP, MODULE, EJB2, IFACE);
    public static final String EJB2_EJB_PREFIX = String.format("ejb:%s", EJB2_NAMING_PREFIX);

    private static final Logger logger = Logger.getLogger(EJBClientUtil.class.getSimpleName());

    private static String PROTOCOL = System.getProperty("protocol", "http");

    public static Context getInitialContext(Server server) throws NamingException {
        return getInitialContext(server.getHost(), server.getPort(), server.getUsername(), server.getPassword());
    }

    public static Context getInitialContext(String host, Integer port, String username, String password)
            throws NamingException {

//        /logger.infof("getInitialContext(host=%s , port=%d , username=%s)", host, port, username);

        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        env.put(Context.PROVIDER_URL, getProviderURL(host, port));

        if (username != null && password != null) {
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
        }

        //env.keySet().forEach(k -> System.out.printf("env: %s = %s\n", k, env.get(k)));

        return new InitialContext(env);
    }

    public static String getProviderURL(String host, Integer port) {
        // env.put(Context.PROVIDER_URL, String.format("remote+http://%s:%d", host, port));
        if ("http".equals(PROTOCOL) || "https".equals(PROTOCOL))
            return String.format("%s://%s:%d/wildfly-services", PROTOCOL, host, port);
        else
            return String.format("%s://%s:%d", PROTOCOL, host, port);
    }

    public static HelloRemote getEJBRemote(Server server, String ejbLookup) throws NamingException {
        return (HelloRemote) getInitialContext(server).lookup(ejbLookup);
    }

    public static String getConfigInfo(Server server, String ejbLookup) {
        return String.format("%s from %s:%s@%s", ejbLookup, server.getUsername(), server.getPassword(), getProviderURL(server.getHost(), server.getPort()));
    }

    private static class NameValue {

        private String name;
        private Object value;

        public NameValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }

    }

    public static void listJndiBindings(Server server) throws NamingException {

        Context ctx = getInitialContext(server);

        String root = "";

        System.out.printf("*** JNDI Binding List ***\n");
        System.out.printf("-----------------------------------------\n");

        extractBindings(ctx, root, 1);

        System.out.printf("-----------------------------------------\n");
    }

    private static void extractBindings(Context ctx, String root, int level) throws NamingException {

        NamingEnumeration<NameClassPair> enumeration = ctx.list(root);

        while (enumeration.hasMore()) {

            NameClassPair ncp = enumeration.next();

            System.out.printf("%" + level + "s %s className: %s nameInNamespace: %s\n", "-", ncp.getName(), ncp.getClassName(),
                    ncp.getNameInNamespace());

            if ("javax.naming.Context".equals(ncp.getClassName()))
                extractBindings(ctx, String.format("%s/%s", root, ncp.getName()), ++level);

        }
    }
}