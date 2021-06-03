FROM centos:7

VOLUME /data

WORKDIR /data

ARG SIGNING_KEYID
ARG SIGNING_PASSWORD
ARG GPG_PATH
ARG GPG_B64

RUN yum update -y && yum install -y expect which rpm-sign

CMD ./travis-sign-rpm.sh /data/rd-cli-tool/build/distributions
