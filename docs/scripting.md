---
layout: page
category: doc
title: Scripting
permalink: /scripting/
---

# Scripting

Specifying formatted output for Job and Execution lists:

	# output only id and href
	rd jobs list -p myproject -% "%id %href"

	# output id and execution status
	rd executions query -p myproject -O 3d -% "%id %status"

## Date Format

For `rd executions` you can customize the default date format of `yyyy-MM-ddHH:mm:ssZ`:

    DATE_FORMAT="yyyy-MM-dd HH:mm z"

See [Java SimpleDateFormat][1]

[1]: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html

## Json and Yaml support

(currently work in progress)

JSON and YAML output:

Use
	
	RD_FORMAT=json

or

	RD_FORMAT=yaml

Examples:

	RD_FORMAT=json rd jobs list -v
	RD_FORMAT=json rd executions query -v
	RD_FORMAT=json rd executions list -v
	
	RD_FORMAT=yaml rd jobs list -v
	RD_FORMAT=yaml rd executions query -v
	RD_FORMAT=yaml rd executions list -v
