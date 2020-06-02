# Release

Uses [axion release](https://axion-release-plugin.readthedocs.io/en/latest/) plugin.

    ./gradlew release

If you need to release with a SNAPSHOT dependency:

    ./gradlew release -Prelease.disableChecks

Otherwise, axion-release will fail the prerelease check. Only do this for testing.

## Next minor version

    ./gradlew markNextVersion -Prelease.incrementer=incrementMinor

Updates minor version without releasing, e.g. 0.1.x-SNAPSHOT becomes 0.2.0-SNAPSHOT

## Force version

    ./gradlew release -Prelease.forceVersion=3.0.0
