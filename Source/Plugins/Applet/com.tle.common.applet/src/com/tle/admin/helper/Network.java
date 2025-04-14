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

package com.tle.admin.helper;

public class Network {
  private String name;
  private String min;
  private String max;

  public Network() {
    super();
  }

  public Network(String name, String min, String max) {
    this.name = name;
    this.min = min;
    this.max = max;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getMax() {
    return max;
  }

  public String getMin() {
    return min;
  }

  public String getName() {
    return name;
  }

  public void setMax(String max) {
    this.max = max;
  }

  public void setMin(String min) {
    this.min = min;
  }

  public void setName(String name) {
    this.name = name;
  }
}
