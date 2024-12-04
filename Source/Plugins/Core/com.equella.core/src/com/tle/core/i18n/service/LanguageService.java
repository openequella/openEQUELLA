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

package com.tle.core.i18n.service;

import com.tle.beans.Language;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.filesystem.LanguageFile;
import com.tle.core.remoting.RemoteLanguageService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public interface LanguageService extends RemoteLanguageService {
  boolean isRightToLeft(Locale locale);

  ResourceBundle getResourceBundle(Locale locale, String bundleGroup);

  void refreshBundles();

  void deleteLanguagePack(Locale locale);

  void importLanguagePack(String stagingId, String filename) throws IOException;

  LanguageFile importLanguagePack(TemporaryFileHandle staging, InputStream zipIn)
      throws IOException;

  void exportLanguagePack(Locale locale, OutputStream out) throws IOException;

  List<Locale> listAvailableResourceBundles();

  void setLanguages(Collection<Language> languages);
}
