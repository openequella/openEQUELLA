/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.workflow.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

/** Bean representing statistics for a single workflow task. */
@XmlRootElement
public class TaskTrendBean {
  /** The unique identifier of the workflow task (workflow item) this statistic row refers to. */
  private String taskId;

  /** The resolved, localised display name of the workflow task. */
  private String name;

  /** The number of items currently waiting at this workflow task. */
  private int waiting;

  /** The change in the waiting count over the requested trend period. */
  private int trend;

  public TaskTrendBean() {}

  public TaskTrendBean(String taskId, String name, int waiting, int trend) {
    this.taskId = taskId;
    this.name = name;
    this.waiting = waiting;
    this.trend = trend;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getWaiting() {
    return waiting;
  }

  public void setWaiting(int waiting) {
    this.waiting = waiting;
  }

  public int getTrend() {
    return trend;
  }

  public void setTrend(int trend) {
    this.trend = trend;
  }
}
