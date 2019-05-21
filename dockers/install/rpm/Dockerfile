FROM centos:6

RUN yum -y update
RUN yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel

RUN mkdir /root/.rd/
COPY test-rd.conf /root/.rd/rd.conf
COPY rundeck-cli-noarch.rpm /root/rundeck-cli-noarch.rpm
RUN rpm -i --prefix=/opt/rdtool /root/rundeck-cli-noarch.rpm
