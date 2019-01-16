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

package com.tle.web.scripting.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.item.AttachmentUtils;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.security.SecurityConstants;
import com.tle.core.item.ViewCountJavaDao;
import com.tle.core.security.TLEAclManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;

public class AttachmentScriptTypeImpl implements AttachmentScriptType {
  private static final long serialVersionUID = 1L;

  @Inject private TLEAclManager aclService;

  private final Attachment wrapped;
  private final FileHandle staging;

  // lazy
  protected Integer viewCount;
  // because viewCount can legitimately be null,
  // we need to set this to stop repeatedly trying to calculate it
  protected boolean viewCountRetrieved;

  @Inject
  protected AttachmentScriptTypeImpl(
      @Assisted("attachment") Attachment attachment,
      @Nullable @Assisted("staging") FileHandle staging) {
    this.wrapped = attachment;
    this.staging = staging;
  }

  @Override
  public String getFilename() {
    return wrapped.getUrl();
  }

  @Override
  public String getUrl() {
    return wrapped.getUrl();
  }

  @Override
  public void setUrl(String url) {
    wrapped.setUrl(url);
  }

  @Override
  public String getUuid() {
    return wrapped.getUuid();
  }

  @Override
  public String getDescription() {
    return wrapped.getDescription();
  }

  @Override
  public void setDescription(String description) {
    wrapped.setDescription(description);
  }

  @Override
  public String getType() {
    return wrapped.getAttachmentType().toString();
  }

  @Override
  public long getSize() {
    if (wrapped instanceof FileAttachment) {
      return ((FileAttachment) wrapped).getSize();
    }
    return 0;
  }

  @Override
  public String getThumbnail() {
    return wrapped.getThumbnail();
  }

  @Override
  public void setThumbnail(String path) {
    wrapped.setThumbnail(path);
  }

  @Override
  public String getCustomType() {
    CustomAttachment custom = ensureCustomAttachment();
    return custom.getType();
  }

  @Override
  public Object getCustomProperty(String propertyName) {
    CustomAttachment custom = ensureCustomAttachment();
    return custom.getData(propertyName);
  }

  @Override
  public void setCustomIntegerProperty(String propertyName, int propertyValue) {
    setCustomProperty(propertyName, propertyValue);
  }

  @Override
  public void setCustomProperty(String propertyName, Object propertyValue) {
    CustomAttachment custom = ensureCustomAttachment();
    custom.setData(propertyName, propertyValue);
  }

  @Override
  public void setCustomDisplayProperty(String key, Object value) {
    LinkedHashMap<String, Object> map = getCustomDisplayMap();
    if (value != null) {
      map.put(key, value);
    } else {
      map.remove(key);
    }
    wrapped.setData(AttachmentUtils.CUSTOM_DISPLAY_KEY, map);
  }

  @Override
  public Object getCustomDisplayProperty(String key) {
    return getCustomDisplayMap().get(key);
  }

  @Override
  public List<String> getAllCustomDisplayProperties() {
    return ImmutableList.copyOf(getCustomDisplayMap().keySet());
  }

  @Override
  public Integer getViewCount() {
    if (viewCountRetrieved) {
      return viewCount;
    }
    if (!aclService
        .filterNonGrantedPrivileges(wrapped.getItem(), SecurityConstants.VIEW_VIEWCOUNT)
        .isEmpty()) {
      viewCount =
          ViewCountJavaDao.getAttachmentViewCount(wrapped.getItem().getItemId(), wrapped.getUuid());
    }
    viewCountRetrieved = true;
    return viewCount;
  }

  /**
   * Internal use only! Do not use in scripts
   *
   * @return
   */
  public Attachment getWrapped() {
    return wrapped;
  }

  /**
   * Internal use only! Do not use in scripts
   *
   * @return
   */
  public FileHandle getStagingFile() {
    return this.staging;
  }

  private CustomAttachment ensureCustomAttachment() {
    AttachmentType type = wrapped.getAttachmentType();
    if (type != AttachmentType.CUSTOM) {
      throw new RuntimeException(
          "This method requires an attachment of type CUSTOM, but it is of type "
              + type.toString());
    }
    return (CustomAttachment) wrapped;
  }

  @SuppressWarnings("unchecked")
  private LinkedHashMap<String, Object> getCustomDisplayMap() {
    Object map = wrapped.getData(AttachmentUtils.CUSTOM_DISPLAY_KEY);
    return map != null
        ? Maps.newLinkedHashMap((LinkedHashMap<String, Object>) map)
        : new LinkedHashMap<String, Object>();
  }
}
