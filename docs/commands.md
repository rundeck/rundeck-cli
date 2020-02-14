---
layout: page
category: doc
title: Commands
permalink: /commands/
---

(Page is work in progress)

The `rd` command provides top level commands:

Available commands:

	   adhoc      - Dispatch adhoc COMMAND to matching nodes
	   executions - List running executions, attach and follow their output, or kill them
	   jobs       - List and manage Jobs
	   keys       - Manage Keys via the Key Storage Facility.
	   nodes      - List and manage node resources
	   projects   - List and manage projects
	   run        - Run a Job
	   scheduler  - View scheduler information
	   system     - View system information
	   tokens     - Create, and manage tokens
	   users      - Manage user information

	Use "rd [command] help" to get help on any command.

## adhoc

Dispatch adhoc COMMAND to matching nodes.

	Usage: adhoc options -- COMMAND...
		[--quoted -Q] : Use quoted args
		[--extension -x value] : File extension to use for temporary script
		[--filter -F value] : A node filter string
		[--follow -f] : Follow execution output as it runs
		[--keepgoing -K] : Keep going when an error occurs
		[--outformat -% value] : Output format specifier for execution logs. You can use "%key" where key is one of:time,level,log,user,command,node. E.g. "%user@%node/%level: %log"
		[--progress -r] : Do not echo log text, just an indicator that output is being received.
		[--project -p /^[-_a-zA-Z0-9+][-\._a-zA-Z0-9+]*$/] : Project name
		[--quiet -q] : Echo no output. Combine with -f/--follow to wait silently until the execution completes. Useful for non-interactive scripts.
		[--restart -t] : Restart from the beginning
		[--script -s value] : Dispatch specified script file
		[--interpreter -i value] : Script interpreter string
		[--stdin -S] : Execute input read from STDIN
		[--tail -T value] : Number of lines to tail from the end, default: 1
		[--threadcount -C value] : Execute using COUNT threads
		[--url -u value] : Download a URL and dispatch it as a script
		[--verbose -v] : Extended verbose output

## executions

List running executions, attach and follow their output, or kill them.


	Available commands:

	   delete     - Delete an execution by ID
	   deletebulk - Find and delete executions in a project
	   follow     - Follow the output of an execution
	   info       - Get info about a single execution by ID
	   kill       - Attempt to kill an execution by ID
	   list       - List all running executions for a project
	   query      - Query previous executions for a project
	   state      - Get detail about the node and step state of an execution by ID  
	   deleteall  - Delete all executions for a job
	   
### execution query

Query previous executions for a project.

	Usage: query options
		[--adhoconly -A] : Adhoc executions only
		[--confirm -y] : Force confirmation of delete request.
		[--xgroup value] : Group or partial group path to exclude, "-" means top-level jobs only
		[--xgroupexact value] : Exact group path to exclude, "-" means top-level jobs only
		[--xnameexact value] : Exclude Exact Job Name Filter, exclude any name that is equal to this value
		[--xname value] : Exclude Job Name Filter, exclude any name that matches this value
		[--xjobids -x value...] : Job ID list to exclude
		[--xjobs -X value...] : List of Full job group and name to exclude.
		[--group -g value] : Group or partial group path to include, "-" means top-level jobs only
		[--groupexact -G value] : Exact group path to include, "-" means top-level jobs only
		[--jobonly -J] : Job executions only
		[--nameexact -N value] : Exact Job Name Filter, include any name that is equal to this value
		[--name -n value] : Job Name Filter, include any name that matches this value
		[--jobids -i value...] : Job ID list to include
		[--jobs -j value...] : List of Full job group and name to include.
		[--max -m value] : Maximum number of results to retrieve at once.
		[--offset -o value] : First result offset to receive.
		[--older -O value] : Get executions older than specified time. e.g. "3m" (3 months).
	Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)
		[--outformat -% value] : Output format specifier for execution data. You can use "%key" where key is one of:id, project, description, argstring, permalink, href, status, job, user, serverUUID, dateStarted, dateEnded, successfulNodes, failedNodes. E.g. "%id %href"
		--project -p value : Project name
		[--recent -d value] : Get executions newer than specified time. e.g. "3m" (3 months).
	Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)
		[--status -s value] : Status filter, one of: running,succeeded,failed,aborted
		[--user -u value] : User filter
		[--verbose -v] : Extended verbose output

## jobs

List and manage Jobs.


	Available commands:

       disable        - Disable execution for a job
       enable         - Enable execution for a job
       info           - Get info about a Job by ID (API v18)
       list           - List jobs found in a project, or download Job definitions (-f)
       load           - Load Job definitions from a file in XML or YAML format
       purge          - Delete jobs matching the query parameters
       reschedule     - Enable schedule for a job
       unschedule     - Disable schedule for a job
       enablebulk     - Enable execution for a set of jobs
       disablebulk    - Disable execution for a set of jobs
       reschedulebulk - Enable schedule for a set of jobs
       unschedulebulk - Disable schedule for a set of jobs
       forecast   - Get Schedule Forecast for a Job by ID (API v31)

## keys

Manage Keys via the Key Storage Facility.
Specify the path using -p/--path, or as the last argument to the command.


	Available commands:

	   create - Create a new key entry
	   delete - Delete the key at the given path
	   get    - Get the contents of a public key
	   info   - Get metadata about the given path
	   list   - List the keys and directories at a given path, or at the root by default
	   update - Update an existing key entry

## nodes

List and manage node resources.

List all nodes for a project.  You can use the -F/--filter to specify a node filter, or simply add the filter on the end of the command

	Usage: list [options] NODE FILTER...
		[--filter -F value] : A node filter string
		[--outformat -% value] : Output format specifier for Node info. You can use "%key" where key is one of:nodename, hostname, osFamily, osVersion, osArch, description, username, tags, or any attribute. E.g. "%nodename %tags"
		[--project -p value] : Project name
		[--verbose -v] : Extended verbose output

## projects

List and manage projects.

    Available commands:

       acls      - Manage Project ACLs
       archives  - Project Archives import and export
       configure - Manage Project configuration
       create    - Create a project
       delete    - Delete a project
       info      - Get info about a project
       list      - List all projects
       readme    - Manage Project readme
       scm       - Manage Project SCM

### projects archives

Project Archives import and export

    Available commands:

       export - Export a project archive
       import - Import a project archive

### projects scm

Manage Project SCM

	Available commands:

	   config      - Get SCM Config for a Project
	   disable     - Disable plugin
	   enable      - Enable plugin
	   inputs      - Get SCM action inputs
	   perform     - Perform SCM action
	   plugins     - List SCM plugins
	   setup       - Setup SCM Config for a Project
	   setupinputs - Get SCM Setup inputs
	   status      - Get SCM Status for a Project

### projects configure

Manage Project configuration

    Available commands:

       delete - Remove configuration properties for a project
       get    - Get all configuration properties for a project
       set    - Overwrite all configuration properties for a project
       update - Modify configuration properties for a project

## run

Run a Job.

    Usage: run [options] -- -OPT VAL -OPT2 VAL...
         [--filter -F value] : A node filter string
         [--follow -f] : Follow execution output as it runs
         [--id -i value] : Run the Job with this IDENTIFIER
         [--job -j value] : Job job (group and name). Run a Job specified by Job name and group. eg: 'group/name'.
         [--loglevel -l /(verbose|info|warning|error)/] : Run the command using the specified LEVEL. LEVEL can be verbose, info, warning, error.
         [--progress -r] : Do not echo log text, just an indicator that output is being received.
         [--project -p value] : Project name
         [--quiet -q] : Echo no output. Combine with -f/--follow to wait silently until the execution completes. Useful for non-interactive scripts.
         [--restart -t] : Restart from the beginning
         [--at -@ value] : Run the job at the specified date/time. ISO8601 format (yyyy-MM-dd'T'HH:mm:ss'Z')
         [--delay -d /(\d+[smhdwMY]\s*)+/] : Run the job at a certain time from now. Format: ##[smhdwMY] where ## is an integer and the units are seconds, minutes, hours, days, weeks, Months, Years. Can combine units, e.g. "2h30m", "20m30s"
         [--tail -T value] : Number of lines to tail from the end, default: 1
         [--user -u value] : A username to run the job as, (runAs access required).

## scheduler

View scheduler information

    Available commands:

       jobs     - List jobs for the current target server, or a specified server
       takeover - Tell a Rundeck server in cluster mode to claim all scheduled jobs from another cluster server

### scheduler jobs

List jobs for the current target server, or a specified server.

	Usage: jobs [options]
		[--uuid -u value] : Server UUID to query, or blank to select the target server

## system

View system information


	Available commands:

	   acls - Manage System ACLs
	   info - Print system information and stats
	   mode - Manage Execution Mode

### system acls

Manage System ACLs


	Available commands:

	   create - Create a system ACL definition
	   delete - Delete a system ACL definition
	   get    - get a system ACL definition
	   list   - list system acls
	   upload - Upload a system ACL definition

### system info

Print system information and stats.

### system mode

Manage Execution Mode.


	Available commands:

	   active  - Set execution mode Active
	   info    - Show execution mode
	   passive - Set execution mode Passive

## tokens

Create, and manage tokens


    Available commands:

       create - Create a token for a user
       delete - Delete a token
       list   - List tokens for a user
       reveal - Reveal token value for an ID (API v19+)

## users

Manage user information


    Available commands:

       edit - Edit information of the same user or another if 'user' is specified
       info - Get information of the same user or from another if 'user' is specified
       list - Get the list of users
