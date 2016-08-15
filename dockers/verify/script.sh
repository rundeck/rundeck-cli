#!/bin/bash

#/ download bintray repo definition
curl -sS -f -L -o /etc/yum.repos.d/bintray.repo https://bintray.com/gschueler/rundeck-maint-staging-rpm/rpm

#/ enable gpg check for repo and artifacts
sed -i.bak s/gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/bintray.repo

#/ download bintray public key, add newline at end to fix rpm error
#( curl -sS -f -L  'https://bintray.com/user/downloadSubjectPublicKey?username=bintray' ; echo ) > /data/bintray.key

#/ import bintray key
echo "gpgkey=https://bintray.com/user/downloadSubjectPublicKey?username=bintray" >> /etc/yum.repos.d/bintray.repo

echo "import build@rundeck.org signing key"
#curl -sS -f -L 'http://rundeck.org/keys/BUILD-GPG-KEY-Rundeck.org.key' > build.key
rpm --import http://rundeck.org/keys/BUILD-GPG-KEY-Rundeck.org.key

yum install -y rundeck-cli