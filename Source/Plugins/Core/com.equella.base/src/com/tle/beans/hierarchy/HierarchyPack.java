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

package com.tle.beans.hierarchy;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.common.security.TargetList;
import java.io.Serializable;
import java.util.List;

public class HierarchyPack implements Serializable {
  private static final long serialVersionUID = 1L;

  private HierarchyTopic topic;
  private List<Item> keyResources;
  private TargetList targetList;
  private List<ItemDefinition> inheritedItemDefinitions;
  private List<Schema> inheritedSchemas;

  public HierarchyPack() {
    super();
  }

  public List<ItemDefinition> getInheritedItemDefinitions() {
    return inheritedItemDefinitions;
  }

  public void setInheritedItemDefinitions(List<ItemDefinition> inheritedItemDefinitions) {
    this.inheritedItemDefinitions = inheritedItemDefinitions;
  }

  public List<Schema> getInheritedSchemas() {
    return inheritedSchemas;
  }

  public void setInheritedSchemas(List<Schema> inheritedSchemas) {
    this.inheritedSchemas = inheritedSchemas;
  }

  public HierarchyTopic getTopic() {
    return topic;
  }

  public void setTopic(HierarchyTopic topic) {
    this.topic = topic;
  }

  public void setKeyResources(List<Item> keyResources) {
    this.keyResources = keyResources;
  }

  public List<Item> getKeyResources() {
    return this.keyResources;
  }

  public TargetList getTargetList() {
    return targetList;
  }

  public void setTargetList(TargetList targetList) {
    this.targetList = targetList;
  }
}
