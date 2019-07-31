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

package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@NonNullByDefault
@SuppressWarnings("nls")
public interface ViewItemViewer {
  Collection<String> VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV =
      Arrays.asList("VIEW_ITEM", "VIEW_ATTACHMENTS");
  Collection<String> DISCOVER_AND_VIEW_PRIVS = Arrays.asList("DISCOVER_ITEM", "VIEW_ITEM");
  Collection<String> VIEW_RESTRICTED_ATTACHMENTS =
      Collections.singleton("VIEW_RESTRICTED_ATTACHMENTS");

  @Nullable
  Collection<String> ensureOnePrivilege();

  /**
   * You might return null if you doing a forward
   *
   * @param info
   * @param resource
   * @return
   * @throws IOException
   */
  @Nullable
  SectionResult view(RenderContext info, ViewItemResource resource) throws IOException;

  @Nullable
  ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource);

  @Nullable
  IAttachment getAttachment(SectionInfo info, ViewItemResource resource);
}
