ARG VERS
FROM ubuntu:${VERS:-16.04}

RUN apt-get update
RUN apt-get --no-install-recommends -y install openjdk-8-jdk
RUN mkdir /root/.rd/
COPY test-rd.conf /root/.rd/rd.conf
COPY rundeck-cli_all.deb /root/rundeck-cli_all.deb
RUN dpkg -i /root/rundeck-cli_all.deb
