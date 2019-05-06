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

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowBean extends BaseEntityBean {
  private WorkflowNodeBean root;
  private boolean moveLive;

  public WorkflowNodeBean getRoot() {
    return root;
  }

  public void setRoot(WorkflowNodeBean root) {
    this.root = root;
  }

  public boolean isMoveLive() {
    return moveLive;
  }

  public void setMoveLive(boolean moveLive) {
    this.moveLive = moveLive;
  }
}
