FROM centos:6

VOLUME /data

WORKDIR /data

ARG SIGNING_KEYID
ARG SIGNING_PASSWORD
ARG SIGNING_KEYRING_FILE

RUN yum update && yum install -y expect

CMD ./travis-sign-rpm.sh