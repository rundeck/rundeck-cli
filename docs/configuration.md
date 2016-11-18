---
layout: page
title: Configuration
permalink: configuration.html
---

# Configuration

Define access credentials as user/password or Token value:

	export RUNDECK_URL=http://rundeck:4440

	export RUNDECK_TOKEN=....

	# or

	export RUNDECK_USER=username
	export RUNDECK_PASSWORD=password

Define a specific API version to use, by using the complete API base:

	export RUNDECK_URL=http://rundeck:4440/api/12

All requests will be made using that API version.

**Bypass an external URL**:

If your Rundeck server has a different *external URL* than the one you are accessing,
you can tell the `rd` tool to treat redirects to that external URL as
if they were to the original URL you specified.

	export RUNDECK_URL=http://internal-rundeck:4440/rundeck
	export RUNDECK_BYPASS_URL=https://rundeck.mycompany.com

This will rewrite any redirect to `https://rundeck.mycompany.com/blah`
as `http://internal-rundeck:4440/rundeck/blah`.

Note: if you include the API version in your `RUNDECK_URL`, e.g. `http://internal-rundeck:4440/rundeck/api/12` then
the `RUNDECK_BYPASS_URL` will be replaced by `http://internal-rundeck:4440/rundeck`.

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

Use the `DEBUG` env var to turn on HTTP debugging:

	export DEBUG=1 # basic http request debug
	export DEBUG=2 # http headers
	export DEBUG=3 # http body