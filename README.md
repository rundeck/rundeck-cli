# Rundeck CLI Tool

[![Build Status](https://travis-ci.org/rundeck/rundeck-cli.svg?branch=main)](https://travis-ci.org/rundeck/rundeck-cli)

This is the official CLI tool for [Rundeck](https://github.com/rundeck/rundeck).

# Documentation 

[https://rundeck.github.io/rundeck-cli/](https://rundeck.github.io/rundeck-cli/)

* [Changelog](https://rundeck.github.io/rundeck-cli/changes/)
* [Install](https://docs.rundeck.com/docs/rd-cli/install.html)
* [Configuration](https://docs.rundeck.com/docs/rd-cli/configuration.html)
* [SSL Configuration](https://docs.rundeck.com/docs/rd-cli/configuration.html#ssl-configuration)
* [Commands](https://docs.rundeck.com/docs/rd-cli/commands.html)
* [Scripting](https://docs.rundeck.com/docs/rd-cli/scripting.html)
* [Java API Library](https://docs.rundeck.com/docs/rd-cli/javalib.html)
* [Extensions](https://docs.rundeck.com/docs/rd-cli/extensions.html)

## Bundled Extensions

* [rd acl](https://rundeck.github.io/rd-ext-acl/) - Test and generate Rundeck ACL policy files

# Javadoc

* [rd-api-client ![javadoc](https://javadoc.io/badge2/org.rundeck.api/rd-api-client/javadoc.svg)](https://javadoc.io/doc/org.rundeck.api/rd-api-client)
* [rd-cli-lib ![javadoc](https://javadoc.io/badge2/org.rundeck.cli/rd-cli-lib/javadoc.svg)](https://javadoc.io/doc/org.rundeck.cli/rd-cli-lib)

# Downloads

[Github Releases](https://github.com/rundeck/rundeck-cli/releases)

For apt and yum repos, see [Install](https://docs.rundeck.com/docs/rd-cli/install.html)

# Howto

## Build

Build with gradle

Produces packages in: rd-cli-tool/build/distributions:

> rd-VERS.zip/.tar
> rundeck_cli_VERS.rpm
> rundeck_cli_VERS.deb

Produces Jars in: rd-cli-tool/build/libs:
rundeck-cli-VERS-all.jar (shadowed jar)

    ./gradlew build

## Build Lenient

Build with lenient mode dependency verification

	./gradlew build --dependency-verification lenient

## Write Dependency Verification

Update dependency verification metadata and export any new keys.

    ./gradlew --write-verification-metadata sha256 --refresh-dependencies help
    ./gradlew --write-verification-metadata pgp,sha256 --refresh-keys --export-keys --refresh-dependencies help
    rm gradle/verification-keyring.gpg
    git add gradle/verification-metadata.xml
    git add gradle/verification-keyring.keys

## Owasp Dependency check

Check OWASP scan for dependencies

    ./gradlew dependencyCheckAggregate -Porg.gradle.dependency.verification.console=verbose --dependency-verification lenient

## Install Locally

Install to local path rd-cli-tool/build/install/rd/bin/rd

	./gradlew :rd-cli-tool:installDist

## Local Run

Run local installation at path rd-cli-tool/build/install/rd/bin/rd

	./rd-cli-tool/build/install/rd/bin/rd "${@}"

## Release

Release a new version

Uses [axion release](https://axion-release-plugin.readthedocs.io/en/latest/) plugin.

	./gradlew release

## Release using Snapshots

If you need to release with any SNAPSHOT dependency
Otherwise, axion-release will fail the prerelease check. Only do this for testing.

    ./gradlew release -Prelease.disableChecks


##  Next Minor Version

Updates minor version without releasing, e.g. 0.1.x-SNAPSHOT becomes 0.2.0-SNAPSHOT

    ./gradlew markNextVersion -Prelease.incrementer=incrementMinor

## Release Force version

Release and force a particular version

    ./gradlew release -Prelease.forceVersion=${1:?version argument must be specified}
