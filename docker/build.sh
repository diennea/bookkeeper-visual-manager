#!/bin/bash
set -e
ROOT_DIR=$(git rev-parse --show-toplevel)

cp $ROOT_DIR/standalone/target/bkvm-*.zip $ROOT_DIR/docker

cd $ROOT_DIR/docker
TARBALL=$(realpath bkvm-*.zip)
docker build -t bkvm/bkvm:latest --build-arg TARBALL=$(basename $TARBALL) .