FROM redhat/ubi8
ARG JDK=java-11-openjdk
RUN yum -y update
RUN yum -y install ${JDK} ${JDK}-devel which
RUN which java

RUN mkdir /root/.rd/
COPY test-rd.conf /root/.rd/rd.conf
COPY rundeck-cli-noarch.rpm /root/rundeck-cli-noarch.rpm
RUN rpm -i --prefix=/opt/rdtool /root/rundeck-cli-noarch.rpm
