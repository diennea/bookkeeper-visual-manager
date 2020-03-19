#!/usr/bin/env bash

BVM_SERVICEURI=${1:-}
BVM_PORT=${2:-8080}

if [ -z "$BVM_SERVICEURI" ]; then
    echo "Usage: $0 \"metadataServiceUri\" [port]"
    exit 1
fi

SCRIPTDIR=`dirname "$0"`
BVM_HOME=`cd ${SCRIPTDIR}/..;pwd`

# Start the backend without UI
mvn jetty:run -f $BVM_HOME -DskipYarn -Dbookkeeper.visual.manager.metadataServiceUri=$BVM_SERVICEURI -Djetty.http.port=$BVM_PORT 
echo "Started Bookkeeper Visual Manager on port ${BVM_PORT}.."

# Start the UI with hot-reload connected to the backend
BVMUI_HOME=`cd ${BVM_HOME}/src/main/bvmui;pwd`
yarn run serve --cwd $BVMUI_HOME -s "http://localhost:${BVM_PORT}"
