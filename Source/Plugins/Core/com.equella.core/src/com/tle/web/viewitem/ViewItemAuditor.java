/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.viewitem;

import com.google.inject.name.Named;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.equella.SectionAuditable.AuditLevel;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewAuditEntry;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ViewItemAuditor {
  private static final Log LOGGER = LogFactory.getLog(ViewItemAuditor.class);
  private static final String KEY_VIEWED = "$VIEWED$-";
  private static final String KEY_SUMMARY = "S";
  private static final String KEY_CONTENT = "C";

  @Inject private UserSessionService sessionService;
  @Inject private AuditLogService auditService;
  @Inject private ItemService itemService;

  private AuditLevel auditLevel;

  @Inject
  public void setAuditLevelString(@Named("audit.level") String auditLevelString) {
    try {
      this.auditLevel = AuditLevel.valueOf(auditLevelString.toUpperCase());
    } catch (IllegalArgumentException e) {
      LOGGER.error("Could not parse audit.level property.  Setting to NONE");
      this.auditLevel = AuditLevel.NONE;
    }
  }

  public void audit(
      HttpServletRequest request,
      ViewAuditEntry auditEntry,
      ItemKey itemId,
      IAttachment attachment) {
    audit(request, auditEntry, itemId, () -> logViewed(itemId, attachment, auditEntry));
  }

  public void audit(
      HttpServletRequest request, ViewAuditEntry auditEntry, ViewableItem<Item> vitem) {
    audit(request, auditEntry, vitem.getItemId(), () -> logViewed(vitem, auditEntry));
  }

  public void audit(
      HttpServletRequest request, ViewAuditEntry auditEntry, ItemKey itemId, AuditCall doAudit) {
    if (auditLevel != AuditLevel.NONE && auditEntry != null) {
      try {
        if (auditLevel == AuditLevel.NORMAL) {
          doAudit.call();
        } else if (auditLevel == AuditLevel.SMART) {
          // log it if it hasn't been already
          if (!isAlreadyViewed(itemId, auditEntry)) {
            doAudit.call();
            // For HEAD and OPTION requests, don't record that we have already viewed.
            // That way, if a follow up GET request is issued, we will record that too, along with
            // the referrer header.
            final String method = request.getMethod();
            if (!"OPTIONS".equals(method) && !"HEAD".equals(method)) {
              registerViewed(itemId, auditEntry);
            }
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private boolean isAlreadyViewed(ItemKey itemId, ViewAuditEntry auditEntry) {
    Boolean val = sessionService.getAttribute(key(itemId, auditEntry));
    return (val != null && val);
  }

  private void registerViewed(ItemKey itemId, ViewAuditEntry auditEntry) {
    sessionService.setAttribute(key(itemId, auditEntry), Boolean.TRUE);
  }

  private String key(ItemKey itemId, ViewAuditEntry auditEntry) {
    final boolean summary = auditEntry.isSummary();
    return KEY_VIEWED
        + (summary ? KEY_SUMMARY : KEY_CONTENT)
        + (summary ? "" : auditEntry.getPath())
        + itemId;
  }

  private void logViewed(ItemKey itemId, IAttachment attachment, ViewAuditEntry entry) {
    auditService.logItemContentViewed(
        itemId,
        entry.getContentType(),
        entry.getPath(),
        attachment,
        sessionService.getAssociatedRequest());
    // This only applies to local items (and hence local attachments), not cloud ones
    if (attachment != null && attachment instanceof Attachment) {
      itemService.incrementViews((Attachment) attachment);
    }
  }

  private void logViewed(ViewableItem<Item> vitem, ViewAuditEntry entry) {
    auditService.logItemSummaryViewed(vitem.getItem(), sessionService.getAssociatedRequest());
    itemService.incrementViews(vitem.getItem());
  }

  @FunctionalInterface
  interface AuditCall {
    void call();
  }
}
