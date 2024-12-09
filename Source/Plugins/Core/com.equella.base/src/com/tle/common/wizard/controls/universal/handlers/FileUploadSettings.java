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

package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import java.util.List;

@SuppressWarnings("nls")
public class FileUploadSettings extends UniversalSettings {
  private static final String NO_UNZIP = "FILE_NOUNZIP";
  private static final String PACKAGE_ONLY = "FILE_PACKAGEONLY";
  private static final String QTI_PACKAGE = "FILE_QTI_PACKAGE";
  private static final String SCORM_PACKAGE = "FILE_SCORM_PACKAGE";
  private static final String SUPPRESS_THUMBS = "FILE_SUPPRESS_THUMBS";
  private static final String SHOW_THUMB_OPTION = "FILE_SHOW_THUMB_OPT";
  private static final String RESTRICT_MIME = "FILE_RESTRICTMIME";
  private static final String KEY_MIMETYPES = "MIMETYPES";
  private static final String RESTRICT_FILE_SIZE = "FILE_RESTRICTSIZE";
  private static final String MAX_FILE_SIZE = "FILE_MAXFILESIZE";
  private static final String DUPLICATION_CHECK = "FILE_DUPLICATION_CHECK";

  public FileUploadSettings(CustomControl wrapped) {
    super(wrapped);
  }

  public FileUploadSettings(UniversalSettings settings) {
    super(settings.getWrapped());
  }

  public boolean isNoUnzip() {
    return wrapped.getBooleanAttribute(NO_UNZIP, false);
  }

  public void setNoUnzip(boolean noUnzip) {
    wrapped.getAttributes().put(NO_UNZIP, noUnzip);
  }

  public boolean isDuplicationCheck() {
    return wrapped.getBooleanAttribute(DUPLICATION_CHECK, false);
  }

  public void setDuplicationCheck(boolean duplicationCheck) {
    wrapped.getAttributes().put(DUPLICATION_CHECK, duplicationCheck);
  }

  public boolean isPackagesOnly() {
    return wrapped.getBooleanAttribute(PACKAGE_ONLY, false);
  }

  public void setPackagesOnly(boolean packagesOnly) {
    wrapped.getAttributes().put(PACKAGE_ONLY, packagesOnly);
  }

  public boolean isQtiPackagesOnly() {
    return wrapped.getBooleanAttribute(QTI_PACKAGE, false);
  }

  public void setQtiPackagesOnly(boolean qtiOnly) {
    wrapped.getAttributes().put(QTI_PACKAGE, qtiOnly);
  }

  public boolean isScormPackagesOnly() {
    return wrapped.getBooleanAttribute(SCORM_PACKAGE, false);
  }

  public void setScormPackagesOnly(boolean scormOnly) {
    wrapped.getAttributes().put(SCORM_PACKAGE, scormOnly);
  }

  public boolean isSuppressThumbnails() {
    return wrapped.getBooleanAttribute(SUPPRESS_THUMBS, false);
  }

  public void setSuppressThumbnails(boolean suppress) {
    wrapped.getAttributes().put(SUPPRESS_THUMBS, suppress);
  }

  public boolean isShowThumbOption() {
    return wrapped.getBooleanAttribute(SHOW_THUMB_OPTION, false);
  }

  public void setShowThumbOption(boolean showOption) {
    wrapped.getAttributes().put(SHOW_THUMB_OPTION, showOption);
  }

  public boolean isRestrictByMime() {
    return wrapped.getBooleanAttribute(RESTRICT_MIME, false);
  }

  public void setRestrictByMime(boolean restrictMime) {
    wrapped.getAttributes().put(RESTRICT_MIME, restrictMime);
  }

  public List<String> getMimeTypes() {
    return wrapped.ensureListAttribute(KEY_MIMETYPES);
  }

  public void setMimeTypes(List<String> mimeTypes) {
    wrapped.getAttributes().put(KEY_MIMETYPES, mimeTypes);
  }

  public boolean isRestrictFileSize() {
    return wrapped.getBooleanAttribute(RESTRICT_FILE_SIZE, false);
  }

  public void setRestrictFileSize(boolean restrictFileSize) {
    wrapped.getAttributes().put(RESTRICT_FILE_SIZE, restrictFileSize);
  }

  public int getMaxFileSize() {
    Integer maxFileSize = (Integer) wrapped.getAttributes().get(MAX_FILE_SIZE);
    if (maxFileSize != null) {
      return maxFileSize;
    }
    return 0;
  }

  public void setMaxFileSize(int maxFileSize) {
    wrapped.getAttributes().put(MAX_FILE_SIZE, maxFileSize);
  }

  // public boolean isNoScrapbook()
  // {
  // return wrapped.getBooleanAttribute(NOSCRAPBOOK, false);
  // }
  //
  // public void setNoScrapbook(boolean noScrapbook)
  // {
  // wrapped.getAttributes().put(NO_UNZIP, noScrapbook);
  // }
}
