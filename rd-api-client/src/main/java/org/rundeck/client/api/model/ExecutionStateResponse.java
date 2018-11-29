/*
 * Copyright 2017 Rundeck, Inc. (http://rundeck.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rundeck.client.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.rundeck.client.util.RdClientConfig;
import org.simpleframework.xml.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionStateResponse {

  private String   executionId;
  private String   serverNode;
  private String   executionState;
  private Boolean  completed;
  private Integer  stepCount;
  private DateInfo updateTime;
  private DateInfo startTime;
  private DateInfo endTime;

  private List<String> targetNodes;
  private List<String> allNodes;

  private Map<String, List<StepState>> nodes;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StepState {
    private String stepctx;
    private String executionState;

    public String getStepctx() {
      return stepctx;
    }

    public void setStepctx(String stepctx) {
      this.stepctx = stepctx;
    }

    public String getExecutionState() {
      return executionState;
    }

    public void setExecutionState(String executionState) {
      this.executionState = executionState;
    }

    @Override
    public String toString() {
      return "{" +
          "stepctx='" + stepctx + '\'' +
          ", executionState='" + executionState + '\'' +
          '}';
    }
  }

  public String execInfoString(RdClientConfig config) {
    return String.format(
        "%s %s %s %s %s",
        executionId,
        executionState,
        null != startTime ? startTime.format(config.getDateFormat()) : "-",
        null != updateTime ? updateTime.format(config.getDateFormat()) : "-",
        null != endTime ? endTime.format(config.getDateFormat()) : "-"
    );
  }

  public String nodeStatusString() {
    if (nodes == null) {
      return "No node status.";
    }

    return nodes.entrySet().stream()
        .map(nodeEntry -> Optional.ofNullable(nodeEntry.getValue())
            .map(steps -> steps.stream()
                .map(step -> String.format("%s:%-10s", step.getStepctx(), step.getExecutionState()))
                .collect(Collectors.joining(
                    " ",
                    String.format("%-15s ",nodeEntry.getKey() + ":"),
                    ""
                )))
            .orElse(nodeEntry.getKey() + ": No Status."))
        .collect(Collectors.joining(
            "\n",
            "Node Step Status: \n",
            ""
        ));
  }

  @Override
  public String toString() {
    return "ExecutionStateResponse{" +
        "executionId='" + executionId + '\'' +
        ", serverNode='" + serverNode + '\'' +
        ", executionState='" + executionState + '\'' +
        ", completed=" + completed +
        ", stepCount=" + stepCount +
        ", updateTime=" + updateTime +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", targetNodes=" + targetNodes +
        ", allNodes=" + allNodes +
        ", nodes=" + nodes +
        '}';
  }


  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getServerNode() {
    return serverNode;
  }

  public void setServerNode(String serverNode) {
    this.serverNode = serverNode;
  }

  public String getExecutionState() {
    return executionState;
  }

  public void setExecutionState(String executionState) {
    this.executionState = executionState;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  public Integer getStepCount() {
    return stepCount;
  }

  public void setStepCount(Integer stepCount) {
    this.stepCount = stepCount;
  }

  public DateInfo getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(DateInfo updateTime) {
    this.updateTime = updateTime;
  }

  public DateInfo getStartTime() {
    return startTime;
  }

  public void setStartTime(DateInfo startTime) {
    this.startTime = startTime;
  }

  public DateInfo getEndTime() {
    return endTime;
  }

  public void setEndTime(DateInfo endTime) {
    this.endTime = endTime;
  }

  public List<String> getTargetNodes() {
    return targetNodes;
  }

  public void setTargetNodes(List<String> targetNodes) {
    this.targetNodes = targetNodes;
  }

  public List<String> getAllNodes() {
    return allNodes;
  }

  public void setAllNodes(List<String> allNodes) {
    this.allNodes = allNodes;
  }

  public Map<String, List<StepState>> getNodes() {
    return nodes;
  }

  public void setNodes(Map<String, List<StepState>> nodes) {
    this.nodes = nodes;
  }
}
