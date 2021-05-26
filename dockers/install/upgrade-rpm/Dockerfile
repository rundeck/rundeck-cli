ARG VERS
FROM centos:${VERS:-7}

RUN yum -y update
RUN yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel which
RUN which java
RUN curl -sS -f -L -o /etc/yum.repos.d/bintray.repo https://bintray.com/rundeck/rundeck-rpm/rpm

RUN yum -y install rundeck-cli

RUN rd pond

COPY rundeck-cli-noarch.rpm /root/rundeck-cli-noarch.rpm

RUN rpm -U /root/rundeck-cli-noarch.rpm

CMD rd pond
