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
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import java.net.URL;
import java.util.List;

/**
 * This is interface which drives viewing of item resources. It's primary responsibilities include:
 *
 * <ul>
 *   <li>Generating ViewItemUrl's for displaying the resource
 *   <li>Accessing the content stream / external link to stream
 * </ul>
 *
 * @author jolz
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface ViewableResource {
  String KEY_HIDDEN = "$HIDDEN$";
  String KEY_TARGETS_FRAME = "$TARGETSFRAME$";
  String PREFERRED_LINK_TARGET = "$PREFERREDLINKTARGET";
  String KEY_NO_FILE_PATHS = "$NOFILEPATHS";

  SectionInfo getInfo();

  ViewableItem getViewableItem();

  boolean isCustomThumb();

  ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery);

  @Nullable
  ImageRenderer createStandardThumbnailRenderer(Label label);

  ImageRenderer createGalleryThumbnailRenderer(Label label);

  String getGalleryUrl(boolean preview, boolean original);

  ImageRenderer createVideoThumbnailRenderer(Label label, TagState tag);

  boolean isExternalResource();

  Bookmark createCanonicalUrl();

  ViewItemUrl createDefaultViewerUrl();

  @Nullable
  <T> T getAttribute(Object key);

  boolean getBooleanAttribute(Object key);

  void setAttribute(Object key, @Nullable Object value);

  @Nullable
  IAttachment getAttachment();

  String getDescription();

  @Nullable
  ViewAuditEntry getViewAuditEntry();

  boolean hasContentStream();

  ContentStream getContentStream();

  /**
   * Generally not null, but it's possible
   *
   * @return
   */
  @Nullable
  String getMimeType();

  String getFilepath();

  void wrappedBy(ViewableResource resource);

  @Nullable
  String getDefaultViewer();

  boolean isDisabled();

  @Nullable
  List<AttachmentDetail> getCommonAttachmentDetails();

  @Nullable
  List<AttachmentDetail> getExtraAttachmentDetails();

  @NonNullByDefault(false)
  class ThumbRef {
    private final URL url;
    private final FileHandle handle;
    private final String localFile;
    private final boolean usePlaceholder;

    public ThumbRef(boolean usePlaceholder) {
      this.url = null;
      this.handle = null;
      this.localFile = null;
      this.usePlaceholder = usePlaceholder;
    }

    public ThumbRef(URL url) {
      this.url = url;
      this.handle = null;
      this.localFile = null;
      this.usePlaceholder = false;
    }

    public ThumbRef(FileHandle handle, String filepath) {
      this.url = null;
      this.handle = handle;
      this.localFile = filepath;
      this.usePlaceholder = false;
    }

    public boolean isUrl() {
      return url != null;
    }

    public URL getUrl() {
      return url;
    }

    public FileHandle getHandle() {
      return handle;
    }

    public String getLocalFile() {
      return localFile;
    }

    public boolean isUsePlaceholder() {
      return usePlaceholder;
    }
  }
}
