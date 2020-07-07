#!/bin/bash

set -euo pipefail

debfile=$(ls rd-cli-tool/build/distributions/rundeck-cli_*-1_all.deb)

cp $debfile dockers/install/debian/rundeck-cli_all.deb

tag=rdcli-deb
UBUNTUVERS="16.04 18.04"

test_basic() {
  local TAG=$1
  docker run -it $TAG rd pond | grep 'For your reference'
}
test_acl() {
  local TAG=$1

  set +o pipefail
  docker run -it $TAG rd acl | grep 'ACLPolicy'
  set -o pipefail
}
build_version() {
  local VERS=$1
  docker build --build-arg VERS=${VERS} dockers/install/debian -t "${tag}${VERS}"
}
run_version() {
  local VERS=$1
  build_version $VERS

  test_basic "${tag}${VERS}"
  test_acl "${tag}${VERS}"
}
run_all() {
  for VERS in $UBUNTUVERS; do
    run_version $VERS
  done
}
main() {
  run_all
}
main
