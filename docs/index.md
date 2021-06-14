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

{% for post in site.pages %}
{%- if post.title -%}
{% if post.path != 'index.md' -%}
* [{{ post.title }}]({{site.url}}{{site.baseurl}}{{ post.permalink }})
{% endif -%}
{%- endif -%}
{%- endfor -%}
{%- for post in site.documentation -%}
{% if post.title -%}
* [{{ post.title }}]({{site.url}}{{site.baseurl}}{{ post.permalink }})
{% endif -%}
{%- endfor -%}
