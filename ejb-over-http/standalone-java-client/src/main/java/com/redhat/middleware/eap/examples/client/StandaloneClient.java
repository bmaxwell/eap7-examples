package com.redhat.middleware.eap.examples.client;

import com.redhat.middleware.eap.examples.EJBClientUtil;
import com.redhat.middleware.eap.examples.Server;

public class StandaloneClient {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }
    private static final Server SERVER1 = new Server();

    public static void main(String[] args) throws Exception {

        System.out.printf("Log JNDI Bindings\n");
        EJBClientUtil.listJndiBindings(SERVER1);

        for (String ejbLookup : new String[] { EJBClientUtil.EJB1_EJB_PREFIX, EJBClientUtil.EJB1_NAMING_PREFIX }) {
            try {
                System.out.printf("Try invoking ejb with: '%s'\n", ejbLookup);
                System.out.printf("-----------------------------------------\n");
                EJBClientUtil.getEJBRemote(SERVER1, ejbLookup).hello();
                System.out.printf("SUCCESS invoking: '%s'\n", ejbLookup);
                System.out.printf("-----------------------------------------\n");
                System.out.flush();
            } catch(Throwable t) {
                System.err.printf("FAILED to invoke: '%s'\n", ejbLookup);
                t.printStackTrace();
            }
        }
    }
}