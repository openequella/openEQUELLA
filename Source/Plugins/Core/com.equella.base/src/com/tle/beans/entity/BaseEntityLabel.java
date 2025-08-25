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

package com.tle.beans.entity;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.i18n.BundleReference;
import java.io.Serial;
import java.io.Serializable;

public class BaseEntityLabel
    implements Serializable, FieldEquality<BaseEntityLabel>, BundleReference {
  @Serial private static final long serialVersionUID = 1L;

  private final long id;
  private final long bundleId;
  private final String uuid;
  private final String owner;

  private boolean forCollection;

  public BaseEntityLabel(long id, String uuid, long bundleId, String owner) {
    this.id = id;
    this.uuid = uuid;
    this.bundleId = bundleId;
    this.owner = owner;
  }

  public long getId() {
    return id;
  }

  public String getUuid() {
    return uuid;
  }

  @Override
  public long getBundleId() {
    return bundleId;
  }

  public String getOwner() {
    return owner;
  }

  /**
   * @return true if this entity is associated with a collection, false otherwise.
   */
  public boolean isForCollection() {
    return forCollection;
  }

  public void setForCollection(boolean forCollection) {
    this.forCollection = forCollection;
  }

  /**
   * Static helper to determine if forCollection should be true based on a String value.
   *
   * @param type the type string to check
   * @return true if type equals "COLLECTION", false otherwise
   */
  public static boolean isCollectionType(String type) {
    return "COLLECTION".equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    return Check.commonEquals(this, obj);
  }

  /*
   * (non-Javadoc)
   * @see com.tle.common.Check.FieldEquality#checkFields(java.lang.Object)
   */
  @Override
  public boolean checkFields(BaseEntityLabel rhs) {
    return id == rhs.getId();
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  @Override
  public long getIdValue() {
    return id;
  }

  @Override
  public String getValue() {
    return Long.toString(id);
  }
}
