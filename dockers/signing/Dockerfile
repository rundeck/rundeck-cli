FROM ubuntu:20.04

VOLUME /data

WORKDIR /data

ARG SIGNING_KEYID
ARG SIGNING_PASSWORD
ARG GPG_PATH
ARG SIGNING_KEY_B64
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update -y && apt-get install -y expect rpm dpkg-sig

CMD ./scripts/sign-packages.sh /data/rd-cli-tool/build/distributions
