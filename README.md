# Rundeck CLI Tool

This is a new CLI tool for [Rundeck](https://github.com/rundeck/rundeck).

Its goal is to replace the old CLI Tools currently included with Rundeck with a modernized,
extensible, and nicer implementation.

(This project is currently a work-in-progress, and development is ongoing.)

See [todos.md](https://github.com/rundeck/rundeck-cli/blob/master/todo.md)

##Download

[github releases](https://github.com/rundeck/rundeck-cli/releases)

* `rd-x.y.zip`/`rd-x.y.tar` zip distribution
* `rundeck-cli-x.y-all.jar` standalone executable jar
* `rundeck-cli-x.y.noarch.rpm` rpm
* `rundeck-cli-x.y_all.deb` debian

## Usage

Define access credentials as user/password or Token value:

	export RUNDECK_URL=http://rundeck:4440

	export RUNDECK_TOKEN=....

	# or

	export RUNDECK_USER=username
	export RUNDECK_PASSWORD=password


Install `rd-0.x.y.zip`

	rd
	├── bin
	│   ├── rd
	│   └── rd.bat
	└── lib
	    ├── ....jar

The `rd` binary provides top level commands:

	$ rd help
	Available commands: [projects, executions, jobs, run, adhoc]
	...

The functionality of previous "rd-*" tools (from Rundeck) is no split into subcommands.

## Commands

### projects

	rd projects help
	List and manage projects.
	Available commands: [create, list, delete]
	--------------------
	+ Command: create
	Create a project.
	Usage: options -- -configkey=value...
		[--help -h] : Print this help
		--project -p value : Project name
	--------------------
	+ Command: list
	--------------------
	+ Command: delete
	Delete a project
	The options available are:
		[--help -h] : Print this help
		--project -p value : Project name

### executions

	rd executions help
	List running executions, attach and follow their output, or kill them.
	Available commands: [list, follow, kill, query]
	--------------------
	+ Command: list
	List all running executions for a project.
	Usage: list [options]
		[--help -h] : Print this help
		[--eid -e value] : Execution ID
		[--max -m value] : Maximum number of results to retrieve at once.
		[--offset -o value] : First result offset to receive.
		[--project -p value] : Project name
	--------------------
	+ Command: follow
	Follow the output of an execution. Restart from the beginning, or begin tailing as it runs.
	Usage: follow options
		[--follow -f] : Follow execution output as it runs
		[--help -h] : Print this help
		--eid -e value : Execution ID
		[--progress -r] : Do not echo log text, just an indicator that output is being received.
		[--quiet -q] : Echo no output, just wait until the execution completes.
		[--restart -t] : Restart from the beginning
		[--tail -T value] : Number of lines to tail from the end, default: 1
	--------------------
	+ Command: kill
	Attempt to kill an execution by ID.
	Usage: kill [options]
		[--help -h] : Print this help
		[--eid -e value] : Execution ID
		[--max -m value] : Maximum number of results to retrieve at once.
		[--offset -o value] : First result offset to receive.
		[--project -p value] : Project name
	--------------------
	+ Command: query
	Query previous executions for a project.
	Usage: query [options]
		[--help -h] : Print this help
		[--eid -e value] : Execution ID
		[--max -m value] : Maximum number of results to retrieve at once.
		[--offset -o value] : First result offset to receive.
		[--older -O value] : Get executions older than specified time. e.g. "3m" (3 months). 
	Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)
		[--project -p value] : Project name
		[--recent -d value] : Get executions newer than specified time. e.g. "3m" (3 months). 
	Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)

### jobs

	rd jobs help
	List and manage Jobs.
	Available commands: [purge, load, list]
	--------------------
	+ Command: purge
	Delete jobs matching the query parameters. Optionally save the definitions to a file before deleting from the server.
	Usage: purge [options]
		[--file -f value] : File path of the file to upload (load command) or destination for storing the jobs (list command)
		[--format -F /^(xml|yaml)$/] : Format for the Job definition file, either xml or yaml
		[--group -g value] : Job Group
		[--help -h] : Print this help
		[--idlist -i value] : Comma separated list of Job IDs
		[--job -j value] : Job name
		[--project -p value] : Project name
	--------------------
	+ Command: load
	Load Job definitions from a file in XML or YAML format.
	The options available are:
		[--duplicate -d /^(update|skip|create)$/] : Behavior when uploading a Job matching a name+group that already exists, either: update, skip, create
		[--file -f value] : File path of the file to upload (load command) or destination for storing the jobs (list command)
		[--format -F /^(xml|yaml)$/] : Format for the Job definition file, either xml or yaml
		[--help -h] : Print this help
		--project -p value : Project name
		[--remove-uuids -r] : Remove UUIDs when uploading
	--------------------
	+ Command: list
	List jobs found in a project, or download Job definitions (-f).
	The options available are:
		[--file -f value] : File path of the file to upload (load command) or destination for storing the jobs (list command)
		[--format -F /^(xml|yaml)$/] : Format for the Job definition file, either xml or yaml
		[--group -g value] : Job Group
		[--help -h] : Print this help
		[--idlist -i value] : Comma separated list of Job IDs
		[--job -j value] : Job name
		--project -p value : Project name


### run

	rd run help
	Run a Job.
	Usage: run [options] -- -ARG VAL -ARG2 VAL...
		[--filter -F value] : A node filter string
		[--follow -f] : Follow execution output as it runs
		[--help -h] : Print this help
		[--id -i value] : Run the Job with this IDENTIFIER
		[--job -j value] : Job job (group and name). Run a Job specified by Job name and group. eg: 'group/name'.
		[--logevel -l /(verbose|info|warning|error)/] : Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.
		[--progress -r] : Do not echo log text, just an indicator that output is being received.
		[--project -p value] : Project name
		[--quiet -q] : Echo no output, just wait until the execution completes.
		[--restart -t] : Restart from the beginning
		[--tail -T value] : Number of lines to tail from the end, default: 1

### adhoc

	rd adhoc help
	Dispatch and adhoc COMMAND to matching nodes.
	Usage: adhoc options -- COMMAND...
		[--filter -F value] : A node filter string
		[--follow -f] : Follow execution output as it runs
		[--help -h] : Print this help
		[--keepgoing -K] : Keep going when an error occurs on multiple dispatch
		[--progress -r] : Do not echo log text, just an indicator that output is being received.
		--project -p value : Project name
		[--quiet -q] : Echo no output, just wait until the execution completes.
		[--restart -t] : Restart from the beginning
		[--script -s value] : Dispatch specified script file
		[--stdin -S] : Execute input read from STDIN
		[--tail -T value] : Number of lines to tail from the end, default: 1
		[--threadcount -C value] : Dispatch execution to Nodes using COUNT threads
		[--url -u value] : Download a URL and dispatch it as a script
