# docker images for signing/verifying rpms

useful on a mac without `rpm`

## sign

Use docker to run the rpm signing script.  

First build the rpm with `./gradlew build`.
	
	$ ./gradlew build

Build the docker image

	$ docker build dockers/signing -t rdrpmsign
	...
	Successfully built d0ed26db360e

Run the built docker image with env vars expected by the `travis-sign-rpm.sh` script:

	$ docker run -v $PWD:/data \
		-e SIGNING_KEYID=xyz \
		-e SIGNING_PASSWORD=abc \
		-e GPG_PATH=/path/to/existing/gpgkeys \
		rdrpmsign 
OR you can supply the secret key as base64 encoded string:

	$ docker run -v $PWD:/data \
		-e SIGNING_KEYID=xyz \
		-e SIGNING_PASSWORD=abc \
		-e GPG_PATH=/any/path \
		-e SIGNING_KEY_B64="base64 encoded key" \
		rdrpmsign 

This will sign the `build/distribution/*.rpm` files with a v3 signature.

## verify

Verify the installation of latest signed rpms from bintray. 

	$ docker build dockers/verify
	...
	Successfully built 3936c3b25bd2
	$ docker run -v $PWD/dockers/verify:/data 3936c3b25bd2
	...(output from script)

Or to test local rpms:

	$ docker run -i -v $PWD/build/distributions:/data 3936c3b25bd2 bash
	rpm --checksig *.rpm
	rundeck-cli-0.1.17-1.noarch.rpm: RSA sha1 ((MD5) PGP) md5 NOT OK (MISSING KEYS: (MD5) PGP#146e75aa)

In this case the signing key was not yet imported. Import with `rpm --import $KEY_URL`.
