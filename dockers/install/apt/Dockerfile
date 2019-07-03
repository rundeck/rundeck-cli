FROM ubuntu:16.04

RUN apt-get -y update
RUN apt-get -y install apt-transport-https curl

RUN echo "deb https://dl.bintray.com/rundeck/rundeck-deb /" | tee -a /etc/apt/sources.list

RUN curl "https://bintray.com/user/downloadSubjectPublicKey?username=bintray" > /tmp/bintray.gpg.key
RUN apt-key add - < /tmp/bintray.gpg.key


RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk
RUN apt-get -y install rundeck-cli
