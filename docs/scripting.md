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

## Json and Yaml support

(currently work in progress)

JSON and YAML output:

	RD_FORMAT=json rd jobs list
	RD_FORMAT=yaml rd jobs list