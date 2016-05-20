# Commandline Tools

## dispatch

run adhoc

-v : Run verbosely.

-V : Turn on debug messages.

-q, --quiet : Show only error messages.

-C, --threadcount *COUNT* : Dispatch execution to Nodes using COUNT threads.

-K, --keepgoing : Keep going when an error occurs on multiple dispatch.

-F, --filter *FILTER* : A node filter string

-filter-exclude-precedence *true|false* : Set the exclusion filter to have precedence or not.

-p *NAME* : Project name

-- *COMMAND_STRING* : Dispatch specified command string

-s, --script *SCRIPT* : Dispatch specified script file

-u, --url *URL* : Download a URL and dispatch it as a script

-S, --stdin : Execute input read from STDIN

-f, --follow : Follow queued execution output

-r, --progress : In follow mode, print progress indicator chars

## rd-acl

acl generation/validation

## rd-jobs

jobs

actions

*list*

`-g, --group` group or subgroup

`-i, --idlist` ids

`-n, --name` job name

`-f, --file` write to file

`-F, --format` use format

*load*

`-d, --duplicate` update|skip|create (update)

`-r, --remove-uuids` remove uuids (false)

`-f, --file` write to file

`-F, --format` use format

*purge*

same as list, deletes jobs

## rd-project

projects

*list* list projects

*create* create project

`-p, --project` project name

`--property=value` config props

*delete* delete project


## rd-queue

executions

*list*
list running executions
`-p, --project`
`-m, --max`
`-o, --offset`
*follow*
follow output
`-q, --quiet`
`-r, --progress`
`-t, --restart`
*kill*
kill executions

`-e, --eid` exec id

## rd-setup

setup

## run

run jobs

    run [-h] [-v] [-l level] [-F nodefilters] [-i id] [-j group/name][-- arguments]

-h, --help : Print usage message.

-v : Run verbosely.

-l, --loglevel *LEVEL* : Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.

-F, --filter *FILTER* : A node filter string

-C *COUNT* : Threadcount, defaults to 1.

-K : Keep going on node failure.

-N : Do not keep going on node failures.

-j, --job *NAME* : Job job (group and name). Run a Job specified by Job name and group. eg: 'group/name'.

-i, --id *IDENTIFIER* : Run the Job with this IDENTIFIER

-- *ARGUMENTS* : Pass the specified ARGUMENTS as options to the job

-f, --follow : Follow queued execution output

-r, --progress : In follow mode, print progress indicator chars

-q, --quiet : In follow mode, do not show output from the execution, but wait until it completes.
