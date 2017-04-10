---
layout: page
category: doc
title: SSL Configuration
permalink: /configuration/ssl/
---

To use a self-signed or custom server certificate for `rd`, you will need to do the following:

1. Import the certificate to a truststore/keystore
2. Set the JVM properties needed to use the truststore

(**Note**: if you want to skip the rigamarole, and simply accept *all*
SSL certificates without verification,
see [Configuration - Insecure SSL]({{site.url}}{{site.baseurl}}/configuration/#insecure-ssl)

## 1. Import the certificate

You can get the server certificate in many ways, (e.g. connect to the server in a
web browser, allow the unsafe connection, then use the browser to download the certificate.)

Otherwise you can use the `openssl` tool (unix) to print it directly.

Set `KEYSTORE` and `CERTFILE` to paths to create the cert and keystore:

	export CERTFILE=server-cert.txt
	export KEYSTORE=mykeystore

Set `HOST` and `PORT` environment variables to your HTTPS server host and port:

	openssl s_client -connect $HOST:$PORT 2>&1 \
	| sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' \
	> $CERTFILE

You can also see the signature by using the Java `keytool` to print it:

	keytool -printcert -sslserver $HOST:$PORT

Next create a new keystore and import the certificate:

	keytool -import -file $CERTFILE \
		-alias $HOST \
		-keystore $KEYSTORE \
		-noprompt \ # this will skip the prompt to trust the certificate 
		-storepass CHANGEME # change to another password

(Or leave off the `-storepass` to be prompted to enter a password).

Now you have imported the certifcate into the keystore we can use to connect to the server.

## 2. Configuration

Export `RD_OPTS` for `rd`:

	export RD_OPTS="-Djavax.net.ssl.trustStore=$KEYSTORE"

If you used a different trust store "type" you can also set that with this opt:
 	
	-Djavax.net.ssl.trustStoreType=jks


Then, [Setup your Rundeck connection info]({{site.url}}{{site.baseurl}}/configuration/), and you can use `rd`.

	export RUNDECK_URL="https://$HOST:$PORT/api/18"
	export RUNDECK_TOKEN="..."
	rd system
