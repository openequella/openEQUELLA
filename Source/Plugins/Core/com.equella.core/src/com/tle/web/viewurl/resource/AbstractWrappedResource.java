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

package com.tle.web.viewurl.resource;

import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.NameValue;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWrappedResource implements ViewableResource {
  protected ViewableResource inner;
  protected ViewableResource top;

  public AbstractWrappedResource(ViewableResource inner) {
    this.inner = inner;
    wrappedBy(this);
  }

  @Override
  public boolean isExternalResource() {
    return inner.isExternalResource();
  }

  @Override
  public Bookmark createCanonicalUrl() {
    return inner.createCanonicalUrl();
  }

  @Override
  public ContentStream getContentStream() {
    return inner.getContentStream();
  }

  @Override
  public ViewItemUrl createDefaultViewerUrl() {
    return inner.createDefaultViewerUrl();
  }

  @Override
  public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery) {
    return inner.getThumbnailReference(info, gallery);
  }

  @Override
  public ImageRenderer createStandardThumbnailRenderer(Label label) {
    return inner.createStandardThumbnailRenderer(label);
  }

  @Override
  public String getGalleryUrl(boolean preview, boolean original) {
    return inner.getGalleryUrl(preview, original);
  }

  @Override
  public IAttachment getAttachment() {
    return inner.getAttachment();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAttribute(Object key) {
    return (T) inner.getAttribute(key);
  }

  @Override
  public boolean getBooleanAttribute(Object key) {
    return inner.getBooleanAttribute(key);
  }

  @Override
  public String getDescription() {
    return inner.getDescription();
  }

  @Override
  public String getFilepath() {
    return inner.getFilepath();
  }

  @Override
  public SectionInfo getInfo() {
    return inner.getInfo();
  }

  @Nullable
  @Override
  public String getMimeType() {
    return inner.getMimeType();
  }

  @Override
  public boolean isCustomThumb() {
    return inner.isCustomThumb();
  }

  @Override
  public ViewableItem<?> getViewableItem() {
    return inner.getViewableItem();
  }

  @Override
  public ViewAuditEntry getViewAuditEntry() {
    return inner.getViewAuditEntry();
  }

  @Override
  public boolean hasContentStream() {
    return inner.hasContentStream();
  }

  @Override
  public void setAttribute(Object key, Object value) {
    inner.setAttribute(key, value);
  }

  @Override
  public void wrappedBy(ViewableResource resource) {
    top = resource;
    inner.wrappedBy(resource);
  }

  @Nullable
  @Override
  public String getDefaultViewer() {
    return inner.getDefaultViewer();
  }

  @Override
  public boolean isDisabled() {
    return inner.isDisabled();
  }

  @Override
  public List<AttachmentDetail> getCommonAttachmentDetails() {
    return inner.getCommonAttachmentDetails();
  }

  @Override
  public List<AttachmentDetail> getExtraAttachmentDetails() {
    return inner.getExtraAttachmentDetails();
  }

  protected static AttachmentDetail makeDetail(Label label, SectionRenderable renderable) {
    return new AttachmentDetail(label, renderable);
  }

  protected static AttachmentDetail makeDetail(Label label, Label renderable) {
    return new AttachmentDetail(label, renderable);
  }

  @Override
  public ImageRenderer createGalleryThumbnailRenderer(Label label) {
    return inner.createGalleryThumbnailRenderer(label);
  }

  @Override
  public ImageRenderer createVideoThumbnailRenderer(Label label, TagState tag) {
    return inner.createVideoThumbnailRenderer(label, tag);
  }

  @Override
  public List<NameValue> getResourceSpecificViewers() {
    return Collections.emptyList();
  }

  @Override
  public ResourceViewer getResourceViewer(String viewerId) {
    return null;
  }
}
