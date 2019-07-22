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

To pass the Metadata Service URI connection string you have to set the System property
`bookkeeper.visual.manager.metadataServiceUri`.

~~~~
BVM_PORT=8080
BVM_SERVICEURI=zk+null://localhost:2181/ledgers

# System property configuration
mvn jetty:run \
    -Dbookkeeper.visual.manager.metadataServiceUri=$BVM_SERVICEURI \
    -Djetty.http.port=$BVM_PORT
~~~~

#### Deploy the war application on container

You can also deploy the `target/bookkeeper-visual-manager-XX.XX.war` on your
container passing the `bookkeeper.visual.manager.metadataServiceUri` location in a System property.

#### Deploy the war application using Maven Jetty Runner

Download the jetty-runner jar available at [Maven
Central](https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/).

~~~~
BVM_PORT=8080
BVM_WAR_PATH=path/to/warfile

# Environment Variable configuration
BVM_CONF_PATH=/path/to/conf java -jar jetty-runner.jar $BVM_WAR_PATH --port $BVM_PORT
~~~~

### Advanced configuration

In order to use a more advanced configuration you need to provide to the
application a properties file.  
**The provided file can also be your Bookkeeper Server configuration.**

| Property             | Value                              |
|----------------------|------------------------------------|
| metadataServiceUri   | Location of the Bookkeeper Server. |
| zkConnectionTimeout  | First connection timeout.          |
| zkTimeout            | Zookeeper session timeout.         |

You can provide this file to the application in three ways:
1. **System Property**: bookkeeper.visual.manager.config.path  
    ~~~~
    BVM_PORT=8080
    BVM_CONF_PATH=path/to/warfile

    mvn jetty:run \
        -Dbookkeeper.visual.manager.config.path=$BVM_CONF_PATH \
        -Djetty.http.port=$BVM_PORT
    ~~~~
2. **Environment Variable**: BVM_CONF_PATH
    ~~~~
    BVM_PORT=8080
    BVM_WAR_PATH=path/to/warfile

    BVM_CONF_PATH=path/to/conf java -jar jetty-runner.jar $BVM_WAR_PATH --port $BVM_PORT
    ~~~~
3. **Deployment Descriptor**: bookkeeper.visual.manager.config.path in the web.xml
    ~~~~
    <context-param>
        <param-name>bookkeeper.visual.manager.config.path</param-name>
        <param-value>path/to/file</param-value>
    </context-param>
    ~~~~