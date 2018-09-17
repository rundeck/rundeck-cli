---
layout: page
category: doc
title: Install
permalink: /install/
---


Download all artifacts from: [github releases](https://github.com/rundeck/rundeck-cli/releases)

* [zip install](#zip-install) `rd-x.y.zip`/`rd-x.y.tar`
* [standalone executable jar](#jar-install) `rundeck-cli-x.y-all.jar`
* [rpm install](#yum-usage) `rundeck-cli-x.y.noarch.rpm`
* [debian install](#debian-usage) `rundeck-cli-x.y_all.deb`
* [arch install](#arch-linux-install)

Additional Yum/Debian repos hosted by:

[![bintray]({{site.url}}{{site.baseurl}}/images/downloads-by-bintray-150.png)](https://bintray.com)

### Jar install

Simply execute:

    java -jar rundeck-cli-x.y-all.jar

### Zip install

Install `rd-0.x.y.zip`

    $ unzip rd-x.y.zip
	rd
	├── bin
	│   ├── rd
	│   └── rd.bat
	└── lib
	    ├── ....jar


### Yum usage

[![Download](https://api.bintray.com/packages/rundeck/rundeck-rpm/rundeck-cli/images/download.svg?version={{site.app_version}}) ](https://bintray.com/rundeck/rundeck-rpm/rundeck-cli/{{site.app_version}}/link) via Bintray 

~~~{.sh}
$ wget https://bintray.com/rundeck/rundeck-rpm/rpm -O bintray.repo
$ sudo mv bintray.repo /etc/yum.repos.d/
$ yum install rundeck-cli
~~~

optional: enable all gpg checks:

~~~{.sh}
$ sed -i.bak s/gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/bintray.repo
$ echo "gpgkey=https://bintray.com/user/downloadSubjectPublicKey?username=bintray" >> /etc/yum.repos.d/bintray.repo
$ rpm --import http://rundeck.org/keys/BUILD-GPG-KEY-Rundeck.org.key 
~~~

optional: enable only rpm gpg checks:

~~~{.sh}
$ sed -i.bak s/^gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/bintray.repo
$ echo "gpgkey=http://rundeck.org/keys/BUILD-GPG-KEY-Rundeck.org.key" >> /etc/yum.repos.d/bintray.repo 
~~~

### Debian usage

 [ ![Download](https://api.bintray.com/packages/rundeck/rundeck-deb/rundeck-cli/images/download.svg?version={{site.app_version}}) ](https://bintray.com/rundeck/rundeck-deb/rundeck-cli/{{site.app_version}}/link)
via Bintray

~~~{.sh}
echo "deb https://dl.bintray.com/rundeck/rundeck-deb /" | sudo tee -a /etc/apt/sources.list
curl "https://bintray.com/user/downloadSubjectPublicKey?username=bintray" > /tmp/bintray.gpg.key
apt-key add - < /tmp/bintray.gpg.key
apt-get -y install apt-transport-https
apt-get -y update
apt-get -y install rundeck-cli
~~~

### Arch Linux install

Make sure you're familiarized with [the AUR](https://wiki.archlinux.org/index.php/Arch_User_Repository)

~~~{.sh}
git clone https://aur.archlinux.org/rundeck-cli.git
cd rundeck-cli
makepkg -i
~~~

