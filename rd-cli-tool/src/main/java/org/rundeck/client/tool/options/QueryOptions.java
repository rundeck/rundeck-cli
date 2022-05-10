package org.rundeck.client.tool.options;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

import java.util.List;
@Getter @Setter
public class QueryOptions
        extends ProjectNameOptions {
  @CommandLine.Option(names = {"-d", "--recent"},
          description = "Get executions newer than specified time. e.g. \"3m\" (3 months). \n" +
                  "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
  private String recentFilter;

  public boolean isRecentFilter() {
    return recentFilter != null;
  }

  @CommandLine.Option(names = {"-O", "--older"},
          description = "Get executions older than specified time. e.g. \"3m\" (3 months). \n" +
                  "Use: h,n,s,d,w,m,y (hour,minute,second,day,week,month,year)")
  private String olderFilter;

  public boolean isOlderFilter() {
    return olderFilter != null;
  }

  @CommandLine.Option(names = {"-s", "--status"},
          description = "Status filter, one of: running,succeeded,failed,aborted")
  private String statusFilter;

  public boolean isStatusFilter() {
    return statusFilter != null;
  }

  @CommandLine.Option(names = {"-u", "--user"},
          description = "User filter")
  private String userFilter;

  public boolean isUserFilter() {
    return userFilter != null;
  }

  @CommandLine.Option(names = {"-A", "--adhoconly"},
          description = "Adhoc executions only")
  private boolean adhoc;

  @CommandLine.Option(names = {"-J", "--jobonly"},
          description = "Job executions only")
  private boolean job;

  @CommandLine.Option(names = { "--jobids"},
          description = "Job ID list to include")
  private List<String> jobIdList;

  public boolean isJobIdList() {
    return jobIdList != null && jobIdList.size() > 0;
  }

  @CommandLine.Option(names = {"-j", "--jobs"},
          description = "List of Full job group and name to include.")
  private List<String> jobList;

  public boolean isJobList() {
    return jobList != null && jobList.size() > 0;
  }

  @CommandLine.Option(names = {"-x", "--xjobids"},
          description = "Job ID list to exclude")
  private List<String> excludeJobIdList;

  public boolean isExcludeJobIdList() {
    return excludeJobIdList != null && !excludeJobIdList.isEmpty();
  }

  @CommandLine.Option(names = {"-X", "--xjobs"},
          description = "List of Full job group and name to exclude.")
  private List<String> excludeJobList;

  public boolean isExcludeJobList() {
    return excludeJobList != null && !excludeJobList.isEmpty();
  }


  @CommandLine.Option(names = {"-g", "--group"},
          description = "Group or partial group path to include, \"-\" means top-level jobs only")
  private String groupPath;

  public boolean isGroupPath() {
    return groupPath != null;
  }

  @CommandLine.Option(names = {"--xgroup"},
          description = "Group or partial group path to exclude, \"-\" means top-level jobs only")
  private String excludeGroupPath;

  public boolean isExcludeGroupPath() {
    return excludeGroupPath != null;
  }

  @CommandLine.Option(names = {"-G", "--groupexact"},
          description = "Exact group path to include, \"-\" means top-level jobs only")
  private String groupPathExact;

  public boolean isGroupPathExact() {
    return groupPathExact != null;
  }

  @CommandLine.Option(names = {"--xgroupexact"},
          description = "Exact group path to exclude, \"-\" means top-level jobs only")
  private String excludeGroupPathExact;

  public boolean isExcludeGroupPathExact() {
    return excludeGroupPathExact != null;
  }

  @CommandLine.Option(names = {"-n", "--name"},
          description = "Job Name Filter, include any name that matches this value")
  private String jobFilter;

  public boolean isJobFilter() {
    return jobFilter != null;
  }

  @CommandLine.Option(names = {"--xname"},
          description = "Exclude Job Name Filter, exclude any name that matches this value")
  private String excludeJobFilter;

  public boolean isExcludeJobFilter() {
    return excludeJobFilter != null;
  }

  @CommandLine.Option(names = {"-N", "--nameexact"},
          description = "Exact Job Name Filter, include any name that is equal to this value")
  private String jobExactFilter;

  public boolean isJobExactFilter() {
    return jobExactFilter != null;
  }

  @CommandLine.Option(names = {"--xnameexact"},
          description = "Exclude Exact Job Name Filter, exclude any name that is equal to this value")
  private String excludeJobExactFilter;

  public boolean isExcludeJobExactFilter() {
    return excludeJobExactFilter != null;
  }


}

