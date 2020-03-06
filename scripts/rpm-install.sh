#!/usr/bin/env bash

set -euo pipefail

rpmfile=$(ls rd-cli-tool/build/distributions/rundeck-cli-*.noarch.rpm)

cp $rpmfile dockers/install/rpm/rundeck-cli-noarch.rpm
docker build dockers/install/rpm -t rdcli-rpm-install
docker run -it rdcli-rpm-install rd pond | grep 'For your reference'
