ARG VERS=7
FROM centos:$VERS
ARG JDK=java-1.8.0-openjdk
RUN yum -y update
RUN yum -y install ${JDK} ${JDK}-devel which
RUN which java

RUN mkdir /root/.rd/
COPY test-rd.conf /root/.rd/rd.conf
COPY rundeck-cli-noarch.rpm /root/rundeck-cli-noarch.rpm
RUN rpm -i --prefix=/opt/rdtool /root/rundeck-cli-noarch.rpm
