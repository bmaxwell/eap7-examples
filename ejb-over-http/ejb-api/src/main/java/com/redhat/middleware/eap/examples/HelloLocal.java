package com.redhat.middleware.eap.examples;

import javax.ejb.Local;

@Local
public interface HelloLocal {

    public String hello();

    public void setLogging(boolean isLogging);
}