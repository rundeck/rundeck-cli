---
# You don't need to edit this file, it's empty on purpose.
# Edit theme's home layout instead if you wanna make some changes
# See: https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: page
title: RD
---

The CLI tool for [Rundeck](https://github.com/rundeck/rundeck)

Version                  | Requirements 
|-------------------------------------------
   [{{ site.app_version }}](changes) |   Java 8 or Java 11

* [Source Code on Github]({{site.github_url}}) 
* [Issues]({{site.github_url}}/issues)

[changes]: {{site.url}}{{site.baseurl}}/changes/

## Documentation


* [Install]({{site.url}}{{site.baseurl}}/install)
* [Configuration]({{site.url}}{{site.baseurl}}/configuration)
* [SSL Configuration]({{site.url}}{{site.baseurl}}/configuration/ssl/)
* [Commands]({{site.url}}{{site.baseurl}}/commands)
* [Scripting]({{site.url}}{{site.baseurl}}/scripting)
* [Java API Library]({{site.url}}{{site.baseurl}}/javalib)
{% for post in site.documentation %}* [{{ post.title }}]({{site.url}}{{site.baseurl}}{{ post.permalink }})
{% endfor %}