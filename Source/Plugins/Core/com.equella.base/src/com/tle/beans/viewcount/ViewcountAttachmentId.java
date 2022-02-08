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

package com.tle.beans.viewcount;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/** This entity represents the composite primary key of table 'viewcount_attachment'. */
@Embeddable
public class ViewcountAttachmentId implements Serializable {
  private static final long serialVersionUID = 1L;

  private long inst;

  @Column(length = 36)
  private String itemUuid;

  private int itemVersion;

  @Column(length = 36)
  private String attachment;

  public ViewcountAttachmentId() {
    super();
  }

  public ViewcountAttachmentId(long inst, String itemUuid, int itemVersion, String attachment) {
    this.inst = inst;
    this.itemUuid = itemUuid;
    this.itemVersion = itemVersion;
    this.attachment = attachment;
  }

  public long getInst() {
    return inst;
  }

  public void setInst(long inst) {
    this.inst = inst;
  }

  public String getItemUuid() {
    return itemUuid;
  }

  public void setItemUuid(String itemUuid) {
    this.itemUuid = itemUuid;
  }

  public int getItemVersion() {
    return itemVersion;
  }

  public void setItemVersion(int itemVersion) {
    this.itemVersion = itemVersion;
  }

  public String getAttachment() {
    return attachment;
  }

  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(inst).hashCode()
        + itemUuid.hashCode()
        + itemVersion
        + attachment.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ViewcountAttachmentId)) {
      return false;
    }

    ViewcountAttachmentId that = (ViewcountAttachmentId) obj;

    return inst == that.inst
        && itemVersion == that.itemVersion
        && itemUuid.equals(that.itemUuid)
        && attachment.equals(that.attachment);
  }
}
