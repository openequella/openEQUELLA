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

import java.time.Instant;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import org.hibernate.annotations.AttributeAccessor;
import org.hibernate.annotations.NamedQuery;

/**
 * This entity represents table 'viewcount_attachment' which provides the view count of an
 * Attachment.
 */
@NamedQuery(
    name = "getAttachmentViewCountForCollection",
    query =
        "SELECT sum(vca.count) FROM ViewcountAttachment vca inner join"
            + " com.tle.beans.item.attachments.Attachment a on vca.id.attachment = a.uuid inner"
            + " join com.tle.beans.item.Item i on a.item.id = i.id inner join"
            + " com.tle.beans.entity.BaseEntity be on be.id = i.itemDefinition.id WHERE be.id="
            + " :collectionId and vca.id.inst = :institutionId")
@NamedQuery(
    name = "deleteAttachmentViewCountForItem",
    query =
        "DELETE FROM ViewcountAttachment Where id.itemVersion = :itemVersion and id.itemUuid ="
            + " :itemUuid and id.inst = :institutionId")
@Entity
@AttributeAccessor("field")
public class ViewcountAttachment extends AbstractViewcount {

  @EmbeddedId private ViewcountAttachmentId id;

  public ViewcountAttachment() {
    super();
  }

  public ViewcountAttachment(ViewcountAttachmentId id, int count, Instant lastViewed) {
    this.id = id;
    setCount(count);
    setLastViewed(lastViewed);
  }

  public ViewcountAttachmentId getId() {
    return id;
  }

  public void setId(ViewcountAttachmentId id) {
    this.id = id;
  }
}
