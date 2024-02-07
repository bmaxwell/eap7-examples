package com.redhat.middleware.eap.examples;

import javax.ejb.Stateless;

@Stateless
public class HelloEJB2 extends AbstractEJB implements HelloRemote, HelloLocal {

}