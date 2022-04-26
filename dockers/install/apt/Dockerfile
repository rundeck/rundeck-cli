FROM ubuntu:22.04

RUN apt-get -y update
RUN apt-get -y install apt-transport-https curl

RUN curl -s https://packagecloud.io/install/repositories/pagerduty/rundeck/script.deb.sh | os=any dist=any bash

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk
RUN apt-get -y install rundeck-cli
