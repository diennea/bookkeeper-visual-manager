#!/usr/bin/env bash

BVM_SERVICEURI=${1:-"zk+null://127.0.0.1:2181/ledgers"}
BVM_PORT=${2:-8086}

if [ -z "$BVM_SERVICEURI" ]; then
    echo "Usage: $0 \"metadataServiceUri\" [port]"
    exit 1
fi

SCRIPTDIR=`dirname "$0"`
BVM_HOME=`cd ${SCRIPTDIR}/..;pwd`

# Start the backend without UI
mvn clean install -DskipTests jetty:run -f $BVM_HOME -DskipYarn -Dbookkeeper.visual.manager.metadataServiceUri=$BVM_SERVICEURI -Djetty.http.port=$BVM_PORT -Dcheckstyle.skip
echo "Started Bookkeeper Visual Manager on port ${BVM_PORT}.."
