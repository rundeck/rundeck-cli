---
layout: page
title: Change Log
permalink: /changes/
---

## 2.0.9

* fix: NPE if system info does not return all data

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.8...v2.0.9)

## 2.0.8

* Fix: rd acl validate fails [Issue #541](https://github.com/rundeck/rundeck-cli/issues/541)
* fix [Issue #538](https://github.com/rundeck/rundeck-cli/issues/538) format parameter overrides content type
* log: support json format for `rd jobs list`
* log: Add json option for `rd jobs load`
* Fix: rd.conf not working

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.7...v2.0.8)

## 2.0.7

* Remove use of XML in API calls (deprecated in Rundeck 4.x)
* Remove `--xml` option for executions metrics
* fix [Issue #465](https://github.com/rundeck/rundeck-cli/issues/465): user+pass auth url incorrect if API version specified in URL

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.6...v2.0.7)

## 2.0.6

* dependency updates
* fix java 11 warnings [Issue #509](https://github.com/rundeck/rundeck-cli/issues/509)
* [Issue #511](https://github.com/rundeck/rundeck-cli/issues/511)
* Fix: metrics data command

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.5...v2.0.6)

## 2.0.5

* fix: incorrect exit codes for command failure
* fix [Issue #468](https://github.com/rundeck/rundeck-cli/issues/468) executions deletebulk --require exit code when 0 results
* log: extend `run --at/-@` timezone parsing format support
* Fix: run --at/-@ argument should work
* log: Upgrade jackson databind to 2.14.2

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.4...v2.0.5)

## 2.0.4

* Add component options to project archives import [Issue #482](https://github.com/rundeck/rundeck-cli/issues/482)
* Update groovy to 3.0.13 [Issue #447](https://github.com/rundeck/rundeck-cli/issues/447)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.3...v2.0.4)

## 2.0.3

* Fix [Issue #474](https://github.com/rundeck/rundeck-cli/issues/474) add --force option for execution abort
* Fix: Add --file to ensure backward compatibility

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.2...v2.0.3)

## 2.0.2

* Fix a signing issue for RPM artifacts [Issue #478](https://github.com/rundeck/rundeck-cli/issues/478)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.1...v2.0.2)

## 2.0.1

* Fix [Issue #458](https://github.com/rundeck/rundeck-cli/issues/458) space separated scm scm perform options not working
* Fix [Issue #446](https://github.com/rundeck/rundeck-cli/issues/446) exit code should not be 0 when run -f execution fails
* Fix [Issue #453](https://github.com/rundeck/rundeck-cli/issues/453) RD_INSECURE_SSL causes npe

[Changes](https://github.com/rundeck/rundeck-cli/compare/v2.0.0...v2.0.1)

## 2.0.0

*Note*: This release contains some under the hood changes, that may change the behavior.
Please [log an issue](https://github.com/rundeck/rundeck-cli/issues) if you encounter any problems.
* Refactored CLI framework to use [picocli](https://picocli.info/)
    * This was a major overhaul, but should be exactly compatible with rd 1.x
* Updated dependencies.
* Deprecations:
    * These old commands and options will still work but will emit a warning if used:
    * `rd system/projects acls`: `upload` subcommand replaced with `update`
    * `rd tokens`: `reveal` subcommand replaced with `info`
    * `rd tokens delete`: option `-t/--token` replaced with `-i/--id`

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.4.3...v2.0.0)

## 1.4.3

* Fix [Issue #433](https://github.com/rundeck/rundeck-cli/issues/433)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.4.2...v1.4.3)

## 1.4.2

(build and release updates)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.4.1...v1.4.2)

## 1.4.1

(build and release updates)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.4.0...v1.4.1)

## 1.4.0

* Add cluster execution mode toggle [Issue #380](https://github.com/rundeck/rundeck-cli/issues/380)
* Emit error if api version used isn't sufficient for project archive parameters [Issue #407](https://github.com/rundeck/rundeck-cli/issues/407)
* Many dependency updates
* [Issue #394](https://github.com/rundeck/rundeck-cli/issues/394)
* Fix: Add arguments for project import till API 38

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.11...v1.4.0)

## 1.3.11

* Fix: empty content type causes exception
* Add Enterprise License APIs [#375](https://github.com/rundeck/rundeck-cli/pull/375) (for RD4.0 APIv41, yet to be released)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.10...v1.3.11)

## 1.3.10

* Fix [Issue #342](https://github.com/rundeck/rundeck-cli/issues/342) uninstall script does not work with sh

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.9...v1.3.10)

## 1.3.9

* Update OkHttp lib [#357](https://github.com/rundeck/rundeck-cli/pull/357)
* Fix [Issue #352](https://github.com/rundeck/rundeck-cli/issues/352) node attribute expansion not working for ssh-xyz attributes
* Fix: allow using java 11 to build
* Fix [Issue #331](https://github.com/rundeck/rundeck-cli/issues/331) java 11 illegal access warnings
* Fix: extension jars not loadable running as shadow dist

Note: 1.3.5 - 1.3.8 changes were related to packaging and release scripts. Publishing of artifacts was moved
from bintray to packagecloud and java libs are now published to maven central. See [install docs](https://github.com/rundeck/rundeck-cli/blob/main/docs/install.md).

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.4...v1.3.9)

## 1.3.4

* Fix [Issue #302](https://github.com/rundeck/rundeck-cli/issues/302) jvm exit delayed by lingering threads

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.3...v1.3.4)

## 1.3.3

* Revert "Fix [Issue #323](https://github.com/rundeck/rundeck-cli/issues/323) indentation in verbose data output"
* fix: snapshot build version is not shown in banner
* fix [Issue #262](https://github.com/rundeck/rundeck-cli/issues/262) increase default read/connect timeouts (10m/2m)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.2...v1.3.3)

## 1.3.2

* fix [Issue #305](https://github.com/rundeck/rundeck-cli/issues/305) scm import perform should include deletedJobs in request
* fix [Issue #304](https://github.com/rundeck/rundeck-cli/issues/304) add scm import status and deleted fields to import items
* Fix [Issue #285](https://github.com/rundeck/rundeck-cli/issues/285) rd run output supports -v and -% and RD_FORMAT when not using follow mode
* Fix [Issue #323](https://github.com/rundeck/rundeck-cli/issues/323) indentation in verbose data output
* Fix [Issue #272](https://github.com/rundeck/rundeck-cli/issues/272) execution info includes adhoc flag
* fix: docs/Gemfile to reduce vulnerabilities

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.1...v1.3.2)

## 1.3.1

* Fix [Issue #307](https://github.com/rundeck/rundeck-cli/issues/307) acl extension not available in shadow packages (deb)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.3.0...v1.3.1)

## 1.3.0

* added `rd acl` subcommands for create,test,validate of aclpolicy files
* update CLI toolbelt, easier picocli in extensions/subcommands

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.2.4...v1.3.0)

## 1.2.4

* Fix [Issue #295](https://github.com/rundeck/rundeck-cli/issues/295) URI too large error for jobs bulk delete

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.2.3...v1.2.4)

## 1.2.3

* Fix [Issue #291](https://github.com/rundeck/rundeck-cli/issues/291) shadow jar should have all classifier
* add simple RundeckClient static builder methods

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.2.2...v1.2.3)

## 1.2.2

* (build and release changes)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.2.1...v1.2.2)

## 1.2.1

* (build and release changes)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.2.0-beta1...v1.2.1)

## 1.2.0-beta1

* refactor to allow extension libraries

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.8...v1.2.0-beta1)

## 1.1.8

* Update dependencies for Retrofit and Jackson #277
* Update help text of `rd run --quiet` for clarity

## 1.1.7

* Update gpg key fix [Issue #252](https://github.com/rundeck/rundeck-cli/issues/252)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.6...v1.1.7)

## 1.1.6

* Fix [Issue #250](https://github.com/rundeck/rundeck-cli/issues/250) update signing script build dir path
* Fix [Issue #245](https://github.com/rundeck/rundeck-cli/issues/245) skip upgrade test on tag build
* update user agent string, allow customizing it in api client lib
* add `rd version` command for version info
* fix: postUninstall should not remove symlink after upgrade

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.5...v1.1.6)

## 1.1.5

* Fix [Issue #242](https://github.com/rundeck/rundeck-cli/issues/242) overwrite symlink if present, and remove on uninstall, add tests
* fix: travis deploy for maven doesn't work
* Fix [Issue #180](https://github.com/rundeck/rundeck-cli/issues/180) prevent invalid project name

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.4...v1.1.5)

## 1.1.4

* fix [Issue #198](https://github.com/rundeck/rundeck-cli/issues/198) load jobs with verbose output shows error message
* fix [Issue #202](https://github.com/rundeck/rundeck-cli/issues/202) format exec list for adhoc should work
* Fix: deb/rpm install scripts to use correct build path

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.3...v1.1.4)

## 1.1.3

* relocatable rpm
* fixes for plugins commands
* add `jobs forecast` command (api v32)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.2...v1.1.3)

## 1.1.2

* Add `rd executions metrics` (API v29) Execution Metrics API [#210](https://github.com/rundeck/rundeck-cli/pull/210)
* Default API version used is 29
* Add `rd user roles` (API v30) [#211](https://github.com/rundeck/rundeck-cli/pull/211)
* new `RD_INSECURE_SSL_NO_WARN=true` to suppress insecure SSL warning [#215](https://github.com/rundeck/rundeck-cli/pull/215)
* support sha384 via upgraded okhttp [#219](https://github.com/rundeck/rundeck-cli/pull/219)
* `rd plugins` enabled by default [#220](https://github.com/rundeck/rundeck-cli/pull/220)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.1...v1.1.2)

## 1.1.1

* New commands
*     - get execution state (rd executions state)
*     - delete all job executions (rd executions deleteall)
*     - toggle job execution (rd jobs enablebulk and disablebulk)
*     - toggle job schedule (rd jobs reschedulebulk and unschedulebulk)
*     - server cluster scheduler takeover (rd scheduler takeover)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.1.0...v1.1.1)

## 1.1.0

* Fix [Issue #196](https://github.com/rundeck/rundeck-cli/issues/196) rd run -loglevel debug should work
* fix description of executions info command

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.29...v1.1.0)

## 1.0.29

* Fix [Issue #186](https://github.com/rundeck/rundeck-cli/issues/186) rd run --raw allows -opt @value literal

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.28...v1.0.29)

## 1.0.28

* correct rd run loglevel option fixes [Issue #185](https://github.com/rundeck/rundeck-cli/issues/185) fixes [Issue #187](https://github.com/rundeck/rundeck-cli/issues/187)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.27...v1.0.28)

## 1.0.27

* fix exception with rd keys list when no keys are present [Issue #177](https://github.com/rundeck/rundeck-cli/issues/177)
* fix rd script issue `rd: source: not found` [Issue #173](https://github.com/rundeck/rundeck-cli/issues/173)
* fix [Issue #177](https://github.com/rundeck/rundeck-cli/issues/177)
* Fix [Issue #173](https://github.com/rundeck/rundeck-cli/issues/173) source with .

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.26...v1.0.27)

## 1.0.26

* Disabled interactive query paging for `executions deletebulk`
* Fix [Issue #169](https://github.com/rundeck/rundeck-cli/issues/169) project create does not require config input
* Disabled interactive query paging for
* Fix [Issue #167](https://github.com/rundeck/rundeck-cli/issues/167) query/deletebulk -m 0 throws exception
* Fix [Issue #168](https://github.com/rundeck/rundeck-cli/issues/168) add --require to executions deletebulk
* Fix [Issue #150](https://github.com/rundeck/rundeck-cli/issues/150) require --path/-p arg to keys subcommands

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.25...v1.0.26)

## 1.0.25

* `rd executions query` now supports `%job.*` format options
* `rd executions query` now does interactive paging if there are more results, and `--autopage` can be used in non-interactive mode to load all available pages of data
* Fix [Issue #163](https://github.com/rundeck/rundeck-cli/issues/163) enhance rd executions query paging
* Fix [Issue #158](https://github.com/rundeck/rundeck-cli/issues/158) rd executions query does not format job info

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.24...v1.0.25)

## 1.0.24

* Fix [Issue #156](https://github.com/rundeck/rundeck-cli/issues/156) npe if no stdin for project delete without --confirm
* Fix [Issue #157](https://github.com/rundeck/rundeck-cli/issues/157) project create can use config file
* support outformat option for tokens create/list/reveal

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.23...v1.0.24)

## 1.0.23

* Fix usage text for jbos reschedule/unschedule
* Fix [Issue #147](https://github.com/rundeck/rundeck-cli/issues/147) `jobs unschedule` disables execution
* Fix [Issue #136](https://github.com/rundeck/rundeck-cli/issues/136) parse error with 500 response for project delete

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.22...v1.0.23)

## 1.0.22

* Fix description of --alldeleted flag
* Fix [Issue #62](https://github.com/rundeck/rundeck-cli/issues/62) add flags to scm perform to include items automatically
* improve scripting of scm actions with -v flag
* fix [Issue #132](https://github.com/rundeck/rundeck-cli/issues/132) restore ACL/SCM validation error responses
* fix [Issue #72](https://github.com/rundeck/rundeck-cli/issues/72) Add RD_CONF to specify conf file

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.21...v1.0.22)

## 1.0.21

* Fix test
* fix [Issue #128](https://github.com/rundeck/rundeck-cli/issues/128) api calls not auto downgrading

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.20...v1.0.21)

## 1.0.20

* Add `rd users [list,info,edit]` (Rundeck 2.10+)
* fix tests
* Fix [Issue #126](https://github.com/rundeck/rundeck-cli/issues/126) Add API version downgrading
* Updated default API version to 21 (rundeck 2.10)
* Fix changelog for 1.0.18/1.0.19

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.19...v1.0.20)

## 1.0.19

* (no changes: Reissue to correctly release build artifacts)

## 1.0.18

* [Issue #124](https://github.com/rundeck/rundeck-cli/issues/124) fix error when unexpected chars in job output if formatter is used

## 1.0.17

* document RD_DATE_FORMAT
* fix [Issue #117](https://github.com/rundeck/rundeck-cli/issues/117) correctly parse datetime for execution data
* Fix [Issue #115](https://github.com/rundeck/rundeck-cli/issues/115) add `rd system mode info/active/passive`
* fix [Issue #102](https://github.com/rundeck/rundeck-cli/issues/102) Username/Password auth: improve auth flow

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.16...v1.0.17)

## 1.0.16

* fix [Issue #113](https://github.com/rundeck/rundeck-cli/issues/113) show load jobs output results correctly

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.15...v1.0.16)

## 1.0.15

* Fix [Issue #94](https://github.com/rundeck/rundeck-cli/issues/94) nodes command does not yet manage nodes
* fix [Issue #105](https://github.com/rundeck/rundeck-cli/issues/105) keys create error states incorrect prompt parameter
* fix [Issue #106](https://github.com/rundeck/rundeck-cli/issues/106) Cannot use the option jobs purge [--jobxact -J value]

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.14...v1.0.15)

## 1.0.14

* Fix [Issue #99](https://github.com/rundeck/rundeck-cli/issues/99): npe when scm import inputs tracked item job is null

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.13...v1.0.14)

## 1.0.13

* add `--charset` option for `rd keys create` password file contents
* read password file contents correctly fixes [Issue #97](https://github.com/rundeck/rundeck-cli/issues/97)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.12...v1.0.13)

## 1.0.12

* Add API v19 support to `rd tokens`
* `rd tokens create` can specify roles, duration
* Add `rd tokens reveal` to reveal token given ID (v19+)
* Handle buggy create token response fix [Issue #95](https://github.com/rundeck/rundeck-cli/issues/95)

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.11...v1.0.12)

## 1.0.11

* Fix [Issue #37](https://github.com/rundeck/rundeck-cli/issues/37) add `-%/--outformat` to `rd run/adhoc/executions follow`

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.10...v1.0.11)

## 1.0.10

* Fix [Issue #89](https://github.com/rundeck/rundeck-cli/issues/89) run command is broken

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.9...v1.0.10)

## 1.0.9

* [Issue #79](https://github.com/rundeck/rundeck-cli/issues/79) Add `job run` support for file upload, using `-opt@ file` or `-opt @file` syntax
* Fix [Issue #73](https://github.com/rundeck/rundeck-cli/issues/73) Add `rd projects archives export/import`
* Fix [Issue #80](https://github.com/rundeck/rundeck-cli/issues/80) Add RD_INSECURE_SSL_HOSTNAME and RD_ALT_SSL_HOSTNAME
* Fix [Issue #83](https://github.com/rundeck/rundeck-cli/issues/83) verify `rd run` uses RD_PROJECT
* Fix [Issue #81](https://github.com/rundeck/rundeck-cli/issues/81) Add job schedule/exec enable/disable
* Fix link to insecure ssl config page
* APIv19: `rd run` file upload via -opt @file
* APIv19: new `rd jobs files` command

[Changes](https://github.com/rundeck/rundeck-cli/compare/v1.0.8...v1.0.9)

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
