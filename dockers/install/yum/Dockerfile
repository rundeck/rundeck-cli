FROM centos:8

RUN yum -y update
RUN yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel which
RUN yum -y install wget
RUN curl https://raw.githubusercontent.com/rundeck/packaging/main/scripts/rpm-setup.sh 2> /dev/null | bash -s rundeck
RUN sed -i.bak s/gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/rundeck.repo
RUN rpm --import https://raw.githubusercontent.com/rundeck/packaging/main/pubring.gpg

RUN yum -y install rundeck-cli

