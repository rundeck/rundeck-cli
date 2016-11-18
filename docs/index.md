---
# You don't need to edit this file, it's empty on purpose.
# Edit theme's home layout instead if you wanna make some changes
# See: https://jekyllrb.com/docs/themes/#overriding-theme-defaults
layout: home
---

# Rundeck CLI Tool

## Running

Install `rd-0.x.y.zip`

	rd
	├── bin
	│   ├── rd
	│   └── rd.bat
	└── lib
	    ├── ....jar

The `rd` binary provides top level commands:

	$ rd help
	Available commands: [projects, executions, jobs, run, adhoc]
	...

The functionality of previous "rd-*" tools (from Rundeck) is no split into subcommands.

## More info

* [Configuration](configuration.html)
* [Commands](commands.html)
* [Scripting](scripting.html)
