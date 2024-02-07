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

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.logging.Logger;

/**
 * @author bmaxwell
 *
 */
@PermitAll
public abstract class AbstractEJB implements HelloRemote {

    private static String NODE_NAME = System.getProperty("jboss.node.name");

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    @Resource
    private SessionContext ctx;

    @Resource
    TransactionSynchronizationRegistry tsr;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String hello() {
        String response = String.format("hello() %s invoked on %s transaction key: %s", (ctx.getCallerPrincipal() == null ? "null" : ctx.getCallerPrincipal().getName()), NODE_NAME, tsr.getTransactionKey());
        log.info(response);
        return response;
    }
}