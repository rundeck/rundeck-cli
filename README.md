# Rundeck CLI Tool

[![Build Status](https://travis-ci.org/rundeck/rundeck-cli.svg?branch=main)](https://travis-ci.org/rundeck/rundeck-cli)

This is the official CLI tool for [Rundeck](https://github.com/rundeck/rundeck).

# Documentation 

[https://rundeck.github.io/rundeck-cli/](https://rundeck.github.io/rundeck-cli/)

* [Changelog](https://rundeck.github.io/rundeck-cli/changes/)
* [Install](https://rundeck.github.io/rundeck-cli/install)
* [Configuration](https://rundeck.github.io/rundeck-cli/configuration)
* [SSL Configuration](https://rundeck.github.io/rundeck-cli/configuration/ssl/)
* [Commands](https://rundeck.github.io/rundeck-cli/commands)
* [Scripting](https://rundeck.github.io/rundeck-cli/scripting)
* [Java API Library](https://rundeck.github.io/rundeck-cli/javalib/)
* [Extensions](https://rundeck.github.io/rundeck-cli/extensions/)

## Bundled Extensions

* [rd acl](https://rundeck.github.io/rd-ext-acl/) - Test and generate Rundeck ACL policy files

# Javadoc

* [rd-api-client ![javadoc](https://javadoc.io/badge2/org.rundeck.api/rd-api-client/javadoc.svg)](https://javadoc.io/doc/org.rundeck.api/rd-api-client)
* [rd-cli-lib ![javadoc](https://javadoc.io/badge2/org.rundeck.cli/rd-cli-lib/javadoc.svg)](https://javadoc.io/doc/org.rundeck.cli/rd-cli-lib)

# Downloads

[Github Releases](https://github.com/rundeck/rundeck-cli/releases)

For apt and yum repos, see [Install](https://rundeck.github.io/rundeck-cli/install/)

# Howto

## Build

Build with gradle

Produces packages in: rd-cli-tool/build/distributions/
* rd-VERS.zip/.tar
* rundeck_cli_VERS.rpm
* rundeck_cli_VERS.deb

Produces Jars in: rd-cli-tool/build/libs
* rundeck-cli-VERS-all.jar (shadowed jar)


    ./gradlew build

## Build Lenient

Build with lenient mode dependency verification

	./gradlew build --dependency-verification lenient

## Install

Install to local path rd-cli-tool/build/install/rd/bin/rd

	./gradlew :rd-cli-tool:installDist

## Release

Release a new version

	./gradlew release