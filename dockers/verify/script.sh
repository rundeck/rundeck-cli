#!/bin/bash
ARGS="${@}"
DIR=${ARGS[0]}

rpm --import https://raw.githubusercontent.com/rundeck/packaging/main/pubring.gpg
rpm --import https://docs.rundeck.com/keys/BUILD-GPG-KEY-20230105.key

for i in "$DIR"/rd-cli-tool/build/distributions/*.rpm; do
    rpm -K "$i"
done
