#!/bin/bash

set -euo pipefail

DEBTAG=rdcli-deb
RPMTAG=rdcli-rpm
UBUNTUVERS="16.04 18.04"
CENTOSVERS="7 8"

test_basic() {
  local TAG=$1
  set +e
  docker run $TAG rd pond 2>&1 | grep 'For your reference'
  ret=$?
  if [ $ret -ne 0 ]; then
    echo "test_basic failed with: $ret"
  fi
  set -e
  test $ret -eq 0
}
test_ext() {
  local TAG=$1
  local EXT=$2
  set +e
  docker run -e RD_DEBUG=1 $TAG rd version 2>&1 | grep "Including extension: $EXT"
  ret=$?
  if [ $ret -ne 0 ]; then
    echo "test_ext failed with: $ret"
  fi
  set -e
  test $ret -eq 0
}

build_deb_version() {
  local TAG=$1
  local VERS=$2
  local debfile=$(ls rd-cli-tool/build/distributions/rundeck-cli_*-1_all.deb)

  cp $debfile dockers/install/debian/rundeck-cli_all.deb
  docker build --build-arg VERS=${VERS} dockers/install/debian -t "${TAG}"
}

build_rpm_version() {
  local TAG=$1
  local VERS=$2
  local rpmfile=$(ls rd-cli-tool/build/distributions/rundeck-cli-*.noarch.rpm)

  cp $rpmfile dockers/install/rpm/rundeck-cli-noarch.rpm
  docker build --build-arg VERS=${VERS} dockers/install/rpm -t "${TAG}"
}

run_all() {
  local TAG=$1
  test_basic "${TAG}"
  test_ext "${TAG}" "org.rundeck.client.tool.commands.repository.Plugins"
  test_ext "${TAG}" "org.rundeck.client.ext.acl.Acl"
}

main() {
  for VERS in $UBUNTUVERS; do
    build_deb_version "$DEBTAG$VERS" $VERS
    run_all "$DEBTAG$VERS"
  done
  for VERS in $CENTOSVERS; do
    build_rpm_version "$RPMTAG$VERS" $VERS
    run_all "$RPMTAG$VERS"
  done
}

main
