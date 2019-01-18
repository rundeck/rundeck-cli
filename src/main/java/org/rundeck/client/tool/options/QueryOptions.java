package org.rundeck.client.tool.options;

import com.lexicalscope.jewel.cli.Option;

import java.util.List;

public interface QueryOptions
    extends ProjectNameOptions{
  @Option(shortName = "d",
      longName = "recent",
      description = "Get executions newer than specified time. e.g. \"3m\" (3 months). \n" +
          "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
  String getRecentFilter();

  boolean isRecentFilter();

  @Option(shortName = "O", longName = "older",
      description = "Get executions older than specified time. e.g. \"3m\" (3 months). \n" +
          "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
  String getOlderFilter();

  boolean isOlderFilter();

  @Option(shortName = "s", longName = "status",
      description = "Status filter, one of: running,succeeded,failed,aborted")
  String getStatusFilter();

  boolean isStatusFilter();

  @Option(shortName = "u", longName = "user",
      description = "User filter")
  String getUserFilter();

  boolean isUserFilter();

  @Option(shortName = "A", longName = "adhoconly",
      description = "Adhoc executions only")
  boolean isAdhoc();

  @Option(shortName = "J", longName = "jobonly",
      description = "Job executions only")
  boolean isJob();

  @Option(shortName = "i", longName = "jobids",
      description = "Job ID list to include")
  List<String> getJobIdList();

  boolean isJobIdList();

  @Option(shortName = "j", longName = "jobs",
      description = "List of Full job group and name to include.")
  List<String> getJobList();

  boolean isJobList();

  @Option(shortName = "x", longName = "xjobids",
      description = "Job ID list to exclude")
  List<String> getExcludeJobIdList();

  boolean isExcludeJobIdList();

  @Option(shortName = "X", longName = "xjobs",
      description = "List of Full job group and name to exclude.")
  List<String> getExcludeJobList();

  boolean isExcludeJobList();


  @Option(shortName = "g", longName = "group",
      description = "Group or partial group path to include, \"-\" means top-level jobs only")
  String getGroupPath();

  boolean isGroupPath();

  @Option(longName = "xgroup",
      description = "Group or partial group path to exclude, \"-\" means top-level jobs only")
  String getExcludeGroupPath();

  boolean isExcludeGroupPath();

  @Option(shortName = "G", longName = "groupexact",
      description = "Exact group path to include, \"-\" means top-level jobs only")
  String getGroupPathExact();

  boolean isGroupPathExact();

  @Option(longName = "xgroupexact",
      description = "Exact group path to exclude, \"-\" means top-level jobs only")
  String getExcludeGroupPathExact();

  boolean isExcludeGroupPathExact();

  @Option(shortName = "n", longName = "name",
      description = "Job Name Filter, include any name that matches this value")
  String getJobFilter();

  boolean isJobFilter();

  @Option(longName = "xname",
      description = "Exclude Job Name Filter, exclude any name that matches this value")
  String getExcludeJobFilter();

  boolean isExcludeJobFilter();

  @Option(shortName = "N", longName = "nameexact",
      description = "Exact Job Name Filter, include any name that is equal to this value")
  String getJobExactFilter();

  boolean isJobExactFilter();

  @Option(longName = "xnameexact",
      description = "Exclude Exact Job Name Filter, exclude any name that is equal to this value")
  String getExcludeJobExactFilter();

  boolean isExcludeJobExactFilter();


}

