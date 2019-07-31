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

package com.tle.web.api.item.interfaces.beans;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RelationBean extends AbstractExtendableBean {
  private Long id;
  private String relation;
  private ItemBean from;
  private ItemBean to;
  private String fromResource;
  private String toResource;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRelation() {
    return relation;
  }

  public void setRelation(String relation) {
    this.relation = relation;
  }

  public ItemBean getFrom() {
    return from;
  }

  public void setFrom(ItemBean from) {
    this.from = from;
  }

  public ItemBean getTo() {
    return to;
  }

  public void setTo(ItemBean to) {
    this.to = to;
  }

  public String getFromResource() {
    return fromResource;
  }

  public void setFromResource(String fromResource) {
    this.fromResource = fromResource;
  }

  public String getToResource() {
    return toResource;
  }

  public void setToResource(String toResource) {
    this.toResource = toResource;
  }
}
