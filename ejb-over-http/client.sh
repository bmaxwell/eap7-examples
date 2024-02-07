#!/bin/bash

jbossHome=$1
shift

clientJars=$jbossHome/bin/client/jboss-client.jar:$jbossHome/bin/client/jboss-cli-client.jar
class=com.redhat.middleware.eap.examples.client.StandaloneClient
jar=standalone-java-client/target/ejb-helloworld-standalone-java-client.jar
cp=$clientJars:$jar

java -Djava.util.logging.manager=org.jboss.logmanager.LogManager -cp $cp $class $*
