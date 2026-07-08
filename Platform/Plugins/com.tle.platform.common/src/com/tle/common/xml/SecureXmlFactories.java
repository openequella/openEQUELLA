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

package com.tle.common.xml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared helpers for configuring JAXP parser factories securely. */
public final class SecureXmlFactories {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecureXmlFactories.class);

  private SecureXmlFactories() {}

  /**
   * Hardens a {@link DocumentBuilderFactory} against XML External Entity (XXE) injection.
   * openEQUELLA never processes XML that legitimately contains a DOCTYPE, so rejecting DOCTYPE
   * declarations outright is the primary guard; disabling external entity resolution is
   * defence-in-depth.
   *
   * <p>Each feature is applied independently: if a parser implementation rejects one, the failure
   * is logged as an error (rather than silently swallowed) and the remaining features are still
   * applied.
   *
   * @param factory the factory to harden.
   */
  public static void hardenAgainstXxe(DocumentBuilderFactory factory) {
    setFeatureOrLog(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
    setFeatureOrLog(factory, "http://xml.org/sax/features/external-general-entities", false);
    setFeatureOrLog(factory, "http://xml.org/sax/features/external-parameter-entities", false);
  }

  private static void setFeatureOrLog(
      DocumentBuilderFactory factory, String feature, boolean value) {
    try {
      factory.setFeature(feature, value);
    } catch (ParserConfigurationException e) {
      LOGGER.error(
          "Unable to set XML parser security feature '{}'; the parser may be vulnerable to XXE"
              + " injection",
          feature,
          e);
    }
  }
}
