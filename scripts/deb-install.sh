#!/bin/bash

set -euo pipefail

debfile=$(ls build/distributions/rundeck-cli_*-1_all.deb)

cp $debfile dockers/install/debian/rundeck-cli_all.deb
docker build dockers/install/debian -t rdcli-deb
docker run -it rdcli-deb rd