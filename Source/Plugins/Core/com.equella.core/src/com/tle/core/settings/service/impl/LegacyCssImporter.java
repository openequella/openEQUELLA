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

package com.tle.core.settings.service.impl;

import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyCssImporter implements Importer {

  private static final Logger LOGGER = LoggerFactory.getLogger(LegacyCssImporter.class);

  @Override
  public Collection<Import> apply(String url, Import previous) {
    final String BASE_CSS_PATH = "/web/css/";
    URI legacyScss = null;

    try {
      legacyScss = getClass().getResource(BASE_CSS_PATH + url).toURI();

      return Collections.singleton(
          new Import(
              legacyScss,
              legacyScss,
              IOUtils.toString(getClass().getResource(BASE_CSS_PATH + url), "UTF-8")));
    } catch (Exception e) {
      LOGGER.error("Failed to import legacy css", e);
      return null;
    }
  }
}
