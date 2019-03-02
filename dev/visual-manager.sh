#!/bin/bash
#
# Licensed to Diennea S.r.l. under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. Diennea S.r.l. licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

ERR_CODE=1

DEV_DIR=`dirname "$0"`

BKVM_UI="bookkeeper-visual-manager-ui"
BKVM_SERVER="bookkeeper-visual-manager-server"

PROJECT_HOME=`cd ${DEV_DIR}/..;pwd`

help() {
    cat <<EOF
Usage: visual-manager.sh <command> [<mode>] 
where command is one of:

[run commands]
    start | stop | restart | console 

    Execute command

[mode commands]
    compile          [DEFAULT] Compile project before running
    nocompile        Do not compile project before running

EOF
}

RUN() {
    if [ -z "$1" ]; then
        echo "no arg passed"
        exit $ERR_CODE
    fi

    if [ $2 = 1 ]; then
        mvn clean install -DskipTests
    fi

    if [ -d ${BKVM_SERVER}/target ]; then
        cd "${BKVM_SERVER}/target"
        unzip -o -q *.zip
        bookkeepervisualmanager-server*/bin/service server $1 conf/server.properties
    fi
}

# if no args specified, show usage
if [ $# -eq 0 ]; then
  help;
  exit $ERR_CODE
fi

COMMAND=$1
shift
MODE=$1
shift

COMPILE=1
case "$MODE" in
  nocompile)
    if ls $BKVM_SERVER/target/*zip 1> /dev/null 2>&1; then
        COMPILE=0
    fi
esac

case "$COMMAND" in
  start)
    RUN start $COMPILE
    ;;
  console)
    RUN stop $COMPILE
    ;;
  stop)
    RUN stop "nocompile"
    ;;
  restart)
    RUN restart "nocompile"
    ;;
  *)
    help;
    exit $ERR_CODE
    ;;
esac

