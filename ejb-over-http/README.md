#eap7-examples

# Build

mvn clean install

# Deploy

## Deploy EJB to EJB Server(s)
cp ejb-server/target/ejb-over-http-server.jar

## Deploy Client War to Client Server
cp web-client/target/ejb-over-http-web-client.war

# Testing

## Using Web Client

http://[host]:8080/ejb-over-http-web-client/index.jsf

Click Invoke , this will start the number of threads and run for n invocations.  If number of invocations is set to 0, it will continue to invoke until stopped.

Click Stop Running , this will break the invocation loop on the threads and should stop.

Click Check Running, this will check and report back how many threads are running

## Using Standalone Java Client

See client.sh to run StandaloneClient in standalone-java-client 
TODO: currently this standalone-java-client only invokes once, it needs to be updated to take # thread, # invocations, etc
