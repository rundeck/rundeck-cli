ARG VERS=16.04
FROM ubuntu:${VERS}
ARG JDK=openjdk-8-jdk
RUN apt-get update
RUN apt-get install -y software-properties-common
RUN add-apt-repository -y ppa:openjdk-r/ppa
RUN apt-get update
RUN apt-get install -y ${JDK}
RUN mkdir /root/.rd/
COPY test-rd.conf /root/.rd/rd.conf
COPY rundeck-cli_all.deb /root/rundeck-cli_all.deb
RUN dpkg -i /root/rundeck-cli_all.deb
