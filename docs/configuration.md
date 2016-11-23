---
layout: page
category: doc
title: Configuration
permalink: /configuration/
---

# Configuration

Define access credentials as user/password or Token value:

	export RD_URL=http://rundeck:4440

	export RD_TOKEN=....

	# or

	export RD_USER=username
	export RD_PASSWORD=password

Define a specific API version to use, by using the complete API base:

	export RD_URL=http://rundeck:4440/api/12

All requests will be made using that API version.

**Prompting**

If you do not define the credentials as environment variables,
you will be prompted to enter a username/password or token in
the shell if a TTY is avaliable.

You can disable automatic prompting:

    export RD_PROMPT=false


**ANSI color**

By default, `rd` will print some output using ANSI escapes for colorized output.

You can disable this:

    export RD_COLOR=0

You can set the default colors used by info/output/error/warning output:

    export RD_COLOR_INFO=blue
    export RD_COLOR_WARN=orange
    export RD_COLOR_ERROR=cyan

**Bypass an external URL**:

If your Rundeck server has a different *external URL* than the one you are accessing,
you can tell the `rd` tool to treat redirects to that external URL as
if they were to the original URL you specified.

	export RD_URL=http://internal-rundeck:4440/rundeck
	export RD_BYPASS_URL=https://rundeck.mycompany.com

This will rewrite any redirect to `https://rundeck.mycompany.com/blah`
as `http://internal-rundeck:4440/rundeck/blah`.

Note: if you include the API version in your `RD_URL`, e.g. `http://internal-rundeck:4440/rundeck/api/12` then
the `RD_BYPASS_URL` will be replaced by `http://internal-rundeck:4440/rundeck`.

**HTTP/connect timeout**

Use `RD_HTTP_TIMEOUT` env var:

	# 30 second timeout
	export RD_HTTP_TIMEOUT=30

Note: if the timeout seems longer than you specify, it is because the "connection retry" is set to true
by default.

**Connection Retry**

Retry in case of recoverable connection issue (e.g. failure to connect):

Use `RD_CONNECT_RETRY` (default `true`):

	# don't retry
	export RD_CONNECT_RETRY=false

**Debug HTTP**

Use the `RD_DEBUG` env var to turn on HTTP debugging:

	export RD_DEBUG=1 # basic http request debug
	export RD_DEBUG=2 # http headers
	export RD_DEBUG=3 # http body