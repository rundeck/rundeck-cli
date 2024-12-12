#!/bin/bash

set -euo pipefail
IFS=$'\n\t'
readonly ARGS=("$@")

DEBTAG=rdcli-deb
RPMTAG=rdcli-rpm
UBUNTUVERS="${UBUNTUVERS:-20.04 22.04}"
#UBUNTUVERS_18="18.04"
RPMJDK="${RPMJDK:-java-11-openjdk java-17-openjdk java-21-openjdk}"
DEBJDK="${DEBJDK:-openjdk-11-jdk openjdk-17-jdk openjdk-21-jdk}"
#DEBJDK_18="openjdk-11-jdk openjdk-17-jdk"
DEBFILE="${DEBFILE:-}"
RPMFILE="${RPMFILE:-}"

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
  local debfile=$4

  cp "$debfile" dockers/install/debian/rundeck-cli_all.deb
  docker build --build-arg VERS="${VERS}" --build-arg JDK="${JDK}" dockers/install/debian -t "${TAG}"
}

build_rpm_version() {
  local TAG=$1
  local JDK=$2
  local rpmfile=$3

  cp "$rpmfile" dockers/install/rpm/rundeck-cli-noarch.rpm
  docker build --build-arg JDK="${JDK}" dockers/install/rpm -t "${TAG}"
}

run_all() {
  local TAG=$1
  test_basic "${TAG}"
  test_ext "${TAG}" "org.rundeck.client.tool.commands.repository.Plugins"
  test_ext "${TAG}" "org.rundeck.client.ext.acl.Acl"
}

test_deb() {
  local debfile="$DEBFILE"
  if [ -z "$debfile" ] ; then
    debfile=$(ls rd-cli-tool/build/distributions/rundeck-cli_*-1_all.deb)
  fi
  for JDK in $DEBJDK; do
    for VERS in $UBUNTUVERS; do
      build_deb_version "$DEBTAG$VERS-$JDK" "$VERS" "$JDK" "$debfile"
      run_all "$DEBTAG$VERS-$JDK"
    done
  done
}
test_rpm() {
  local rpmfile="$RPMFILE"
  if [ -z "$rpmfile" ] ; then
    rpmfile=$(ls rd-cli-tool/build/distributions/rundeck-cli-*.noarch.rpm)
  fi
  for JDK in $RPMJDK; do
    build_rpm_version "$RPMTAG-$JDK" "$JDK" "$rpmfile"
    run_all "$RPMTAG-$JDK"
  done
}
all(){
  test_deb
  test_rpm
}
main() {
  case "${ARGS[0]}" in
    -deb) test_deb ;;
    -rpm) test_rpm ;;
    -all) all ;;
    * ) echo "Expected args -deb -rpm or -all "; exit 2 ;;
  esac
}

main
