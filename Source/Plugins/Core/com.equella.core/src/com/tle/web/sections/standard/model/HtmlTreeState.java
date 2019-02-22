/*
 * Copyright 2017 Apereo
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

package com.tle.web.sections.standard.model;

public class HtmlTreeState extends HtmlComponentState {
  private HtmlTreeModel model;
  private HtmlTreeServer treeServer;
  private String nodeId;
  private boolean lazyLoad;
  private boolean allowMultipleOpenBranches;

  public HtmlTreeModel getModel() {
    return model;
  }

  public void setModel(HtmlTreeModel model) {
    this.model = model;
  }

  public boolean isLazyLoad() {
    return lazyLoad;
  }

  public void setLazyLoad(boolean lazyLoad) {
    this.lazyLoad = lazyLoad;
  }

  public boolean isAllowMultipleOpenBranches() {
    return allowMultipleOpenBranches;
  }

  public void setAllowMultipleOpenBranches(boolean allowMultipleOpenBranches) {
    this.allowMultipleOpenBranches = allowMultipleOpenBranches;
  }

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public HtmlTreeServer getTreeServer() {
    return treeServer;
  }

  public void setTreeServer(HtmlTreeServer treeServer) {
    this.treeServer = treeServer;
  }
}
