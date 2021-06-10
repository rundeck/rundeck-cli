#!/bin/bash
ARGS="${@}"
DIR=${ARGS[0]}

gpg --import $DIR/packaging/pubring.gpg

for i in "$DIR"/rd-cli-tool/build/distributions/*.deb; do
    dpkg-sig --verify "$i"
done
