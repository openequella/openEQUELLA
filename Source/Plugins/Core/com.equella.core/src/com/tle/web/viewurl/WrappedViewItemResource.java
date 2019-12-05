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

import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.Bookmark;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;
import java.util.Set;

public abstract class WrappedViewItemResource implements ViewItemResource {
  protected final ViewItemResource inner;
  protected ViewItemResource topLevel;

  public WrappedViewItemResource(ViewItemResource inner) {
    this.inner = inner;
    wrappedBy(this);
  }

  @Override
  public ViewAuditEntry getViewAuditEntry() {
    return inner.getViewAuditEntry();
  }

  @Override
  public Bookmark createCanonicalURL() {
    return inner.createCanonicalURL();
  }

  @Override
  public ContentStream getContentStream() {
    return inner.getContentStream();
  }

  @Override
  public ViewItemViewer getViewer() {
    return inner.getViewer();
  }

  @Override
  public String getFileDirectoryPath() {
    return inner.getFileDirectoryPath();
  }

  @Override
  public String getFilenameWithoutPath() {
    return inner.getFilenameWithoutPath();
  }

  @Override
  public String getFilepath() {
    return inner.getFilepath();
  }

  @Nullable
  @Override
  public IAttachment getAttachment() {
    return inner.getAttachment();
  }

  @Override
  public int getForwardCode() {
    return inner.getForwardCode();
  }

  @Override
  public Set<String> getPrivileges() {
    return inner.getPrivileges();
  }

  @Override
  public ViewableItem getViewableItem() {
    return inner.getViewableItem();
  }

  @Override
  public String getMimeType() {
    return inner.getMimeType();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(Object key) {
    return (T) inner.getAttribute(key);
  }

  @Override
  public void setAttribute(Object key, Object value) {
    inner.setAttribute(key, value);
  }

  @Override
  public boolean getBooleanAttribute(Object key) {
    return inner.getBooleanAttribute(key);
  }

  @Override
  public void wrappedBy(ViewItemResource resource) {
    topLevel = resource;
    inner.wrappedBy(resource);
  }

  @Override
  public boolean isPathMapped() {
    return inner.isPathMapped();
  }

  @Override
  public String getDefaultViewerId() {
    return inner.getDefaultViewerId();
  }

  @Override
  public boolean isRestrictedResource() {
    return inner.isRestrictedResource();
  }
}
