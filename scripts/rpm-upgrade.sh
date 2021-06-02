#!/usr/bin/env bash

set -euo pipefail

rpmfile=$(ls rd-cli-tool/build/distributions/rundeck-cli-*.noarch.rpm)

cp $rpmfile dockers/install/upgrade-rpm/rundeck-cli-noarch.rpm
docker build dockers/install/upgrade-rpm -t rdcli-rpm-upgrade
docker run rdcli-rpm-upgrade rd pond | grep 'For your reference'
