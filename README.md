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

To pass the Metadata Service URI connection string you have to set the system property
`bookkeeper.visual.manager.metadataServiceUri` or run the provided script in bin
folder.

~~~~
BVM_PORT=8080
BVM_SERVICEURI=zk+null://localhost:2181/ledgers

./bin/run_dev.sh $BVM_SERVICEURI $BVM_PORT
~~~~

#### Deploy the war application on container

You can also deploy the `target/bookkeeper-visual-manager-XX.XX.war` on your
container passing the `bookkeeper.visual.manager.metadataServiceUri` location in a system property.

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
application a key/value properties file. An example file is provided in the conf/ folder.
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

## Development

0. Clone this repo
0. Make sure you have installed node (v11.8.0) and yarn (1.19.1).
0. Go into the frontend folder `src/main/bvmui` 
0. Install dependencies (use `yarn install` command)
0. Auto reload (use `yarn serve` command)
    ~~~~
    cd src/main/bvmui
    yarn install
    yarn serve
    ~~~~
0. Build (use `yarn build` command)
