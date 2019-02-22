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

package com.tle.web.viewurl.resource;

import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;
import java.util.List;

public class SimpleUrlResource extends AbstractWrappedResource {
  private final String url;
  private final String description;
  private final boolean disabled;

  public SimpleUrlResource(
      ViewableResource resource, String url, String description, boolean disabled) {
    super(resource);
    this.url = url;
    this.description = description;
    this.disabled = disabled;
  }

  @Override
  public boolean hasContentStream() {
    return false;
  }

  @Override
  public String getMimeType() {
    return MimeTypeConstants.MIME_LINK;
  }

  @Override
  public boolean isExternalResource() {
    return true;
  }

  @Override
  public boolean isDisabled() {
    return disabled;
  }

  @Override
  public Bookmark createCanonicalUrl() {
    return new SimpleBookmark(url);
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public ViewAuditEntry getViewAuditEntry() {
    return new ViewAuditEntry("url", url); // $NON-NLS-1$
  }

  @Override
  public List<AttachmentDetail> getCommonAttachmentDetails() {
    // See DetailUrlResource
    return null;
  }
}
