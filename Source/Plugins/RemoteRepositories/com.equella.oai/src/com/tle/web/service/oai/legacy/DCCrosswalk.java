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

package com.tle.web.service.oai.legacy;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.server.crosswalk.XML2oai_dc;
import ORG.oclc.oai.server.verb.CannotDisseminateFormatException;
import com.tle.beans.item.Item;
import com.tle.common.Format;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Deprecated
@Bind(Crosswalk.class)
@Singleton
public class DCCrosswalk extends XML2oai_dc {
  @Inject private UserService userService;

  /**
   * The constructor assigns the schemaLocation associated with this crosswalk. Since the crosswalk
   * is trivial in this case, no properties are utilized.
   *
   * @param properties properties that are needed to configure the crosswalk.
   */
  @Inject
  public DCCrosswalk(OAIProperties properties) {
    super(properties.getProperties());
  }

  /**
   * Can this nativeItem be represented in DC format?
   *
   * @param nativeItem a record in native format
   * @return true if DC format is possible, false otherwise.
   */
  @Override
  public boolean isAvailableFor(Object nativeItem) {
    return true;
  }

  /**
   * Perform the actual crosswalk.
   *
   * @param nativeItem the native "item". In this case, it is already formatted as an OAI <record>
   *     element, with the possible exception that multiple metadataFormats are present in the
   *     <metadata> element.
   * @return a String containing the XML to be stored within the <metadata> element.
   * @exception CannotDisseminateFormatException nativeItem doesn't support this format.
   */
  @Override
  public String createMetadata(Object nativeItem) throws CannotDisseminateFormatException {
    Item fullItem = (Item) nativeItem;

    XMLWriter xml = new XMLWriter("oai_dc:dc"); // $NON-NLS-1$
    xml.addNamespace("xmlns:dc", "http://purl.org/dc/elements/1.1/"); // $NON-NLS-1$ //$NON-NLS-2$
    xml.addNamespace(
        "xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/"); // $NON-NLS-1$ //$NON-NLS-2$
    xml.addNamespace(
        "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); // $NON-NLS-1$ //$NON-NLS-2$
    xml.addNamespace(
        "xsi:schemaLocation",
        "http://www.openarchives.org/OAI/2.0/" //$NON-NLS-1$ //$NON-NLS-2$
            + "oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd"); //$NON-NLS-1$

    UserBean owner = userService.getInformationForUser(fullItem.getOwner());

    xml.addValue(
        "dc:title", CurrentLocale.get(fullItem.getName(), fullItem.getUuid())); // $NON-NLS-1$
    xml.addValue(
        "dc:description",
        CurrentLocale.get(fullItem.getDescription(), "")); // $NON-NLS-1$ //$NON-NLS-2$
    xml.addValue("dc:identifier", fullItem.getIdString()); // $NON-NLS-1$
    xml.addValue(
        "dc:date",
        new UtcDate(
                fullItem //$NON-NLS-1$
                    .getDateCreated())
            .toString());
    xml.addValue("dc:creator", Format.format(owner)); // $NON-NLS-1$

    return xml.toString();
  }

  private static class XMLWriter {
    private final Map<String, String> namespaces = new HashMap<String, String>();
    private final List<String> values = new ArrayList<String>();
    private final String rootnode;

    public XMLWriter(String rootnode) {
      this.rootnode = rootnode;
    }

    public void addNamespace(String name, String value) {
      namespaces.put(name, value);
    }

    public void addValue(String node, String value) {
      StringBuilder xml = new StringBuilder();
      xml.append('<');
      xml.append(node);
      xml.append('>');

      xml.append(Utils.ent(value));

      xml.append("</"); // $NON-NLS-1$
      xml.append(node);
      xml.append('>');
      values.add(xml.toString());
    }

    @Override
    public String toString() {
      StringBuilder xml = new StringBuilder();
      xml.append('<');
      xml.append(rootnode);

      for (String name : namespaces.keySet()) {
        String value = namespaces.get(name);
        xml.append(' ');
        xml.append(name);
        xml.append("=\""); // $NON-NLS-1$
        xml.append(Utils.ent(value));
        xml.append('"');
      }
      xml.append('>');

      for (String name : values) {
        xml.append(name);
      }

      xml.append("</"); // $NON-NLS-1$
      xml.append(rootnode);
      xml.append('>');
      return xml.toString();
    }
  }
}
