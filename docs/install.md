---
layout: page
category: doc
title: Install
permalink: /install/
---


All artifacts can be downloaded from: [github releases](https://github.com/rundeck/rundeck-cli/releases/latest)

* [zip install](#zip-install) `rd-x.y.zip`/`rd-x.y.tar`
* [standalone executable jar](#jar-install) `rundeck-cli-x.y-all.jar`
* [rpm install](#yum-usage) `rundeck-cli-x.y.noarch.rpm`
* [debian install](#debian-usage) `rundeck-cli-x.y_all.deb`
* [arch install](#arch-linux-install)

### Jar install

Download the `rundeck-cli-x.y.z-all.jar` from [github releases](https://github.com/rundeck/rundeck-cli/releases/latest).

Simply execute:

    java -jar rundeck-cli-x.y.z-all.jar

### Zip install


Download the `rd-x.y.z.zip` from [github releases](https://github.com/rundeck/rundeck-cli/releases/latest).

Install:

    $ unzip rd-x.y.zip
	rd-x.y.z
	├── bin
	│   ├── rd
	│   └── rd.bat
	└── lib
	    ├── ....jar

Execute:

    rd-x.y.z/bin/rd

### Yum usage

---
**NOTE**: If you previously installed via Bintray Yum repo (RD version prior to 1.3.9), please remove the `/etc/yum.repos.d/bintray.repo` file if it exists.

---

Add Yum Repo: 

~~~{.sh}
curl https://raw.githubusercontent.com/rundeck/packaging/main/scripts/rpm-setup.sh 2> /dev/null | bash -s rundeck
~~~

Optional: enable all gpg checks:

~~~{.sh}
$ sed -i.bak s/gpgcheck=0/gpgcheck=1/ /etc/yum.repos.d/rundeck.repo
$ rpm --import https://raw.githubusercontent.com/rundeck/packaging/main/pubring.gpg
~~~


Finally: install rundeck-cli

~~~{.sh}
yum install rundeck-cli
~~~

#### Download RPM Package

Or Download individual packages at
[rundeck-cli rpms via packagecloud](https://packagecloud.io/app/pagerduty/rundeck/search?q=rundeck-cli&filter=rpms&filter=rpms&dist=) 



### Debian usage

---

**NOTE**: If you previously installed via the Bintray APT repo (rd version prior to 1.3.9), please remove the line `deb https://dl.bintray.com/rundeck/rundeck-deb /` from your `/etc/apt/sources.list` file.  

---

Install the packagecloud APT repo for rundeck:

~~~{.sh}
curl -s https://packagecloud.io/install/repositories/pagerduty/rundeck/script.deb.sh | os=any dist=any bash
~~~

Finally: install rundeck-cli

~~~{.sh}
apt-get install rundeck-cli
~~~

#### Download Deb Package

Or Download individual packages at [rundeck-cli debs via packagecloud](https://packagecloud.io/app/pagerduty/rundeck/search?q=rundeck-cli&filter=debs)


### Arch Linux install

Make sure you're familiarized with [the AUR](https://wiki.archlinux.org/index.php/Arch_User_Repository)

~~~{.sh}
git clone https://aur.archlinux.org/rundeck-cli.git
cd rundeck-cli
makepkg -i
~~~

