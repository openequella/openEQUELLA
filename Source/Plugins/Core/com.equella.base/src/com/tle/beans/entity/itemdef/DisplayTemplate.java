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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

public class DisplayTemplate implements Serializable {
  private static final long serialVersionUID = 1;

  public enum DisplayType {
    DEFAULT,
    XSLT,
    DISPLAY_NODES;
  }

  // Done because of Java 1.5 <-> 1.4 serialisation.
  private transient DisplayType typeEnum;
  private String type;

  private String xsltFilename;
  private List<DisplayNode> displayNodes;

  public DisplayType getType() {
    if (typeEnum == null && type != null) {
      typeEnum = DisplayType.valueOf(type);
    }
    return typeEnum;
  }

  public void setType(DisplayType type) {
    typeEnum = type;
    this.type = typeEnum.name();
  }

  public String getXsltFilename() {
    return xsltFilename;
  }

  public void setXsltFilename(String xsltFilename) {
    this.xsltFilename = xsltFilename;
  }

  public List<DisplayNode> getDisplayNodes() {
    return displayNodes;
  }

  public void setDisplayNodes(List<DisplayNode> displayNodes) {
    this.displayNodes = displayNodes;
  }
}
