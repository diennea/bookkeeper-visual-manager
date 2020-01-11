#!/bin/bash
HERE=$(dirname $0)
$JAVA_HOME/bin/java -Djava.util.logging.config.file=$HERE/cli-logging.properties -jar $HERE/../blobit-cli.jar $@
