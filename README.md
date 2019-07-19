# Bookkeeper Visual Manager

Bookkeeper Visual Manager is an open source visual interface for [Apache
Bookkeeper](https://bookkeeper.apache.org/).

[![Build Status](https://travis-ci.org/diennea/bookkeeper-visual-manager.svg?branch=master)](https://travis-ci.org/apache/bookkeeper)

## Guide

### Requirements

Before deploying the .war file of Bookkeeper Visual Manager you have to make
sure that Zookkeeper and your Bookkeeper services are up and running.

### Quickstart

#### Deploy the war application using Maven Jetty Plugin
~~~~
git clone https://github.com/diennea/bookkeeper-visual-manager.git
cd bookkeeper-visual-manager

mvn clean install -DskipTests
~~~~

To pass the Zookeeper connection string you have to set the System Property
`zk.servers`.

~~~~
BVM_PORT=8080
BVM_ZK_SERVERS=zk+null://localhost:2181/ledgers

mvn jetty:run \
    -Dbookkeeper.visual.manager.metadataServiceUri=$BVM_ZK_SERVERS \
    -Djetty.http.port=$BVM_PORT

~~~~

#### Deploy the war application on container

You can also deploy the `target/bookkeeper-visual-manager-XX.XX.war` on your
container passing the `bookkeeper.visual.manager.metadataServiceUri` location in a System property.

~~~~

#### Deploy the war application using Maven Jetty Runner

Download the jetty-runner jar available at [Maven Central](https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/).
~~~~
BVM_PORT=8080
BVM_WAR_PATH=path/to/bvm/warfile

java -jar jetty-runner.jar $BVM_WAR_PATH --port $BVM_PORT
