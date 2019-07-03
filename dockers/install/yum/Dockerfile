FROM centos:6

RUN yum -y update
RUN yum -y install java-1.8.0-openjdk java-1.8.0-openjdk-devel
RUN yum -y install wget
RUN wget https://bintray.com/rundeck/rundeck-rpm/rpm -O bintray.repo
RUN mv bintray.repo /etc/yum.repos.d/

RUN sed -i.bak s/gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/bintray.repo
RUN echo "gpgkey=https://bintray.com/user/downloadSubjectPublicKey?username=bintray" >> /etc/yum.repos.d/bintray.repo

RUN rpm --import https://rundeck.org/keys/BUILD-GPG-KEY-Rundeck.org.key

RUN yum -y install rundeck-cli

