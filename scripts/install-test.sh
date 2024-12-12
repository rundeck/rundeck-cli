#!/bin/bash

set -euo pipefail

DEBTAG=rdcli-deb
RPMTAG=rdcli-rpm
UBUNTUVERS="${UBUNTUVERS:-18.04 20.04 22.04}"
#UBUNTUVERS_18="18.04"
RPMJDK="${RPMJDK:-java-11-openjdk java-17-openjdk java-21-openjdk}"
DEBJDK="${DEBJDK:-openjdk-11-jdk openjdk-17-jdk openjdk-21-jdk}"
#DEBJDK_18="openjdk-11-jdk openjdk-17-jdk"

test_basic() {
  local TAG=$1
  set +e
  docker run "$TAG" rd pond 2>&1 | grep 'For your reference'
  ret=$?
  if [ $ret -ne 0 ]; then
    echo "test_basic failed with: $ret for $TAG"
  fi
  set -e
  test $ret -eq 0
}
test_ext() {
  local TAG=$1
  local EXT=$2
  set +e
  docker run -e RD_DEBUG=1 "$TAG" rd version 2>&1 | grep "Including extension: $EXT"
  ret=$?
  if [ $ret -ne 0 ]; then
    echo "test_ext failed with: $ret for $TAG and $EXT"
  fi
  set -e
  test $ret -eq 0
}

build_deb_version() {
  local TAG=$1
  local VERS=$2
  local JDK=$3
  local debfile
  debfile=$(ls rd-cli-tool/build/distributions/rundeck-cli_*-1_all.deb)

  cp "$debfile" dockers/install/debian/rundeck-cli_all.deb
  docker build --build-arg VERS="${VERS}" --build-arg JDK="${JDK}" dockers/install/debian -t "${TAG}"
}

build_rpm_version() {
  local TAG=$1
  local JDK=$2
  local rpmfile
  rpmfile=$(ls rd-cli-tool/build/distributions/rundeck-cli-*.noarch.rpm)

  cp "$rpmfile" dockers/install/rpm/rundeck-cli-noarch.rpm
  docker build --build-arg JDK="${JDK}" dockers/install/rpm -t "${TAG}"
}

run_all() {
  local TAG=$1
  test_basic "${TAG}"
  test_ext "${TAG}" "org.rundeck.client.tool.commands.repository.Plugins"
  test_ext "${TAG}" "org.rundeck.client.ext.acl.Acl"
}

main() {
  for JDK in $DEBJDK; do
    for VERS in $UBUNTUVERS; do
      build_deb_version "$DEBTAG$VERS-$JDK" "$VERS" "$JDK"
      run_all "$DEBTAG$VERS-$JDK"
    done
  done
  for JDK in $RPMJDK; do
    build_rpm_version "$RPMTAG-$JDK" "$JDK"
    run_all "$RPMTAG-$JDK"
  done
}

main
