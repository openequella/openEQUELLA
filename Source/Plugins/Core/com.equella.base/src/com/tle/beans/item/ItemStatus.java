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

package com.tle.beans.item;

public enum ItemStatus {
  DRAFT,
  LIVE,
  REJECTED,
  MODERATING,
  ARCHIVED,
  SUSPENDED,
  DELETED,
  REVIEW,
  PERSONAL;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

  /**
   * Returns the exact string representation of the enum value. Unfortunately {@code toString()} has
   * been changed to return a lowercase version. Why? That's lost to history. But exactString() is
   * here to provide the original functionality for those places where the exact enum name is needed
   * - such as APIs.
   */
  public String exactString() {
    return super.toString();
  }
}
