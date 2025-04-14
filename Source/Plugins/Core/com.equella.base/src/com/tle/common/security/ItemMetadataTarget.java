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

package com.tle.common.security;

import com.tle.beans.entity.itemdef.ItemDefinition;
import java.io.Serializable;
import java.util.Objects;

public class ItemMetadataTarget implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String id;
  private ItemDefinition itemDefinition;

  public ItemMetadataTarget(String id, ItemDefinition itemDefinition) {
    this.id = id;
    this.itemDefinition = itemDefinition;
  }

  public String getId() {
    return id;
  }

  public ItemDefinition getItemDefinition() {
    return itemDefinition;
  }

  public void setItemDefinition(ItemDefinition itemDefinition) {
    this.itemDefinition = itemDefinition;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ItemMetadataTarget)) {
      return false;
    }

    ItemMetadataTarget rhs = (ItemMetadataTarget) obj;
    return Objects.equals(id, rhs.id) && itemDefinition.equals(rhs.itemDefinition);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
