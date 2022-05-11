---
layout: page
title: RD - The CLI tool for Rundeck
---

The CLI tool for [Rundeck](https://github.com/rundeck/rundeck)

Latest Version                  | Requirements 
|-------------------------------------------
   [{{ site.app_version }}]({{site.url}}{{site.baseurl}}/changes/) |   Java 8 or Java 11

* [Documentation](https://docs.rundeck.com/docs/rd-cli/)
* [Source Code]({{site.github_url}}) 
* [Issues]({{site.github_url}}/issues)
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


