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

This will sign the `rd-cli-tool/build/distribution/*.rpm` files with a v3 signature.

This will sign the `rd-cli-tool/build/distribution/*.deb` files.

## verify rpm

Verify the signature of signed rpm. 

	$ docker build dockers/verify -t rpmsigverify
	...
	Successfully built 3936c3b25bd2

	$ docker run -i -v $PWD:/build rpmsigverify
	/build/rd-cli-tool/build/distributions/rundeck-cli-1.3.9.SNAPSHOT-1.noarch.rpm: rsa sha1 (md5) pgp md5 OK

If it fails you would see something like: 

	rundeck-cli-0.1.17-1.noarch.rpm: RSA sha1 ((MD5) PGP) md5 NOT OK (MISSING KEYS: (MD5) PGP#146e75aa)

## verify deb

Verify the signature of signed deb. 

	$ docker build dockers/verifydeb -t debsigverify
	...
	Successfully built 3936c3b25bd2

	$ docker run -i -v $PWD:/build debsigverify
	gpg: /root/.gnupg/trustdb.gpg: trustdb created
    gpg: key F192529298C4C654: public key "PagerDuty (Rundeck release signing) <signing@rundeck.com>" imported
    gpg: Total number processed: 1
    gpg:               imported: 1

	Processing /data/rundeck-cli_1.3.9.SNAPSHOT-1_all.deb...
    GOODSIG _gpgbuilder 31327DA0C35EA6B88B9D4648F192529298C4C654 1623364592

If it fails you would see something like:
 
    UNKNOWNSIG _gpgbuilder 98C4C654
