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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;
import java.util.Optional;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class AttachmentBean extends AbstractExtendableBean {
  private String uuid;
  private String description;
  private String viewer;
  private boolean preview;
  private boolean erroredIndexing;
  private boolean restricted;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getViewer() {
    return viewer;
  }

  public void setViewer(String viewer) {
    this.viewer = viewer;
  }

  @JsonIgnore
  public abstract String getRawAttachmentType();

  public boolean isPreview() {
    return preview;
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }

  public void setRestricted(boolean restricted) {
    this.restricted = restricted;
  }

  public boolean isRestricted() {
    return restricted;
  }

  /**
   * @see com.tle.beans.item.attachments.IAttachment#isErroredIndexing
   * @return The value of erroredIndexing
   */
  public boolean isErroredIndexing() {
    return erroredIndexing;
  }

  /**
   * @see com.tle.beans.item.attachments.IAttachment#setErroredIndexing
   * @param erroredIndexing The value to set erroredIndexing to
   */
  public void setErroredIndexing(boolean erroredIndexing) {
    this.erroredIndexing = erroredIndexing;
  }

  /**
   * An ID for an attachment which is located on an external platform. Encoding of this value is
   * specific to the attachment type. For internal oEQ attachments it will be absent.
   *
   * @return a possibly encoded reference to an attachment in an external system, or 'empty' if an
   *     attachment local to this oEQ institution.
   */
  public Optional<String> getExternalId() {
    return Optional.empty();
  }
}
