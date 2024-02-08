package com.redhat.middleware.eap.examples;

import javax.ejb.Remote;

@Remote
public interface HelloRemote {

    public String hello();

    public void setLogging(boolean isLogging);

}