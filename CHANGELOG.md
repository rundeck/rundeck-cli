## unreleased

* #79 Add `job run` support for file upload, using `-opt@ file` or `-opt @file` syntax

## 1.0.8

* Check the follow flag on run with delay. Fixes [Issue #76](https://github.com/rundeck/rundeck-cli/issues/76)
* Default API version set to 18, fixes [Issue #74](https://github.com/rundeck/rundeck-cli/issues/74)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.7...v1.0.8)

## 1.0.7

* add `rd projects configure` subcommands

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.6...v1.0.7)

## 1.0.6

* log: add `rd run --delay` flag relative schedule
* log: `run --follow --at` waits before following
* Fix [Issue #65](https://github.com/rundeck/rundeck-cli/issues/65) run -f halts before job execution finishes
* log: add projects info -p name
* Fix [Issue #68](https://github.com/rundeck/rundeck-cli/issues/68) improve projects list output
* Fix [Issue #66](https://github.com/rundeck/rundeck-cli/issues/66) don't prompt when token or user+pass set
* Fix [Issue #63](https://github.com/rundeck/rundeck-cli/issues/63) allow RD_INSECURE_SSL=true

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.5...v1.0.6)

## 1.0.5

* Fix weird character
* Fix [Issue #60](https://github.com/rundeck/rundeck-cli/issues/60) add conf file sourcing in shadow rd script

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.4...v1.0.5)

## 1.0.4

* Fix [Issue #51](https://github.com/rundeck/rundeck-cli/issues/51) rd project scm should honor RD_PROJECT env var
* Fix [Issue #55](https://github.com/rundeck/rundeck-cli/issues/55) jobs info -%/--outformat option causes blank output
* unix: ~/.rd/rd.conf file can export env vars like RD_URL. fix [Issue #54](https://github.com/rundeck/rundeck-cli/issues/54)
* fix: NPE on 400 response to keys upload/create

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.3...v1.0.4)

## 1.0.3

* Add CHANGELOG fix [Issue #48](https://github.com/rundeck/rundeck-cli/issues/48)
* fix [Issue #44](https://github.com/rundeck/rundeck-cli/issues/44) add more SCM commands:

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.2...v1.0.3)

## 1.0.2

Date: 2016-12-13

* [#45](https://github.com/rundeck/rundeck-cli/issues/45)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.1...v1.0.2)

## 1.0.1

Date: 2016-12-13

* [#42](https://github.com/rundeck/rundeck-cli/issues/42)
* [#43](https://github.com/rundeck/rundeck-cli/issues/43)
* [#47](https://github.com/rundeck/rundeck-cli/issues/47)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.0...v1.0.1)

## 1.0.0

Date: 2016-12-05

Initial 1.0 release
