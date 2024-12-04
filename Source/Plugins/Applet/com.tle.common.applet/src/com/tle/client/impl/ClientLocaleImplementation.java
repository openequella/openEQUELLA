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

package com.tle.client.impl;

import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale.AbstractCurrentLocale;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class ClientLocaleImplementation extends AbstractCurrentLocale {
  private final Locale locale;
  private final Properties text;
  private final String keyPrefix;
  private final boolean rtl;

  public ClientLocaleImplementation(
      URL serverUrl, String[] bundleGroups, Locale locale, boolean rtl) throws IOException {
    this(serverUrl, bundleGroups, locale, "", rtl);
  }

  public ClientLocaleImplementation(
      URL serverUrl, String[] bundleGroups, Locale locale, String keyPrefix, boolean rtl)
      throws IOException {
    this.locale = locale;
    this.keyPrefix = keyPrefix;
    this.rtl = rtl;

    final Logger logger = LoggerFactory.getLogger(getClass().getName());

    text = new Properties();
    for (String bundleGroup : bundleGroups) {
      Properties props = new Properties();
      URL textUrl =
          new URL(serverUrl, "language/download/" + locale + '/' + bundleGroup + ".properties");

      logger.info("Reading " + textUrl);
      InputStream in = null;
      try {
        in = textUrl.openStream();
        props.load(in);
      } finally {
        if (in != null) {
          in.close();
        }
      }
      text.putAll(props);
    }

    logger.info("Read " + text.keySet().size() + " strings");
  }

  public ClientLocaleImplementation(Locale locale, Properties text, String keyPrefix, boolean rtl) {
    this.locale = locale;
    this.keyPrefix = keyPrefix;
    this.text = text;
    this.rtl = rtl;
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  protected Pair<Locale, String> resolveKey(final String key) {
    final String k = keyPrefix + key;
    String property = text.getProperty(k);
    if (property == null) {
      throw new MissingResourceException(k, k, k);
    }
    return new Pair<Locale, String>(locale, property);
  }

  @Override
  public boolean isRightToLeft() {

    return rtl;
  }

  @Override
  public ResourceBundle getResourceBundle() {
    throw new UnsupportedOperationException();
  }
}
