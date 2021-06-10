#!/bin/bash
ARGS="${@}"
DIR=${ARGS[0]}

rpm --import https://raw.githubusercontent.com/rundeck/packaging/main/pubring.gpg

for i in "$DIR"/rd-cli-tool/build/distributions/*.rpm; do
    rpm -K "$i"
done
