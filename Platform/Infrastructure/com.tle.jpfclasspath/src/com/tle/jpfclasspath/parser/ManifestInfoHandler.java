/**
 * *************************************************************************** Java Plug-in
 * Framework (JPF) Copyright (C) 2004-2007 Dmitry Olshansky This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * ***************************************************************************
 */
package com.tle.jpfclasspath.parser;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/** @version $Id$ */
final class ManifestInfoHandler extends BaseHandler {
  private ModelManifestInfo manifest = null;

  ManifestInfoHandler(final EntityResolver anEntityResolver) {
    super(anEntityResolver);
  }

  /**
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String,
   *     java.lang.String, org.xml.sax.Attributes)
   */
  @Override
  public void startElement(
      final String uri, final String localName, final String qName, final Attributes attributes)
      throws SAXException {
    if (log.isDebugEnabled()) {
      log.debug(
          "startElement - ["
              + uri
              + "]/[" //$NON-NLS-1$ //$NON-NLS-2$
              + localName
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    String name = qName;
    if ("plugin".equals(name)) { // $NON-NLS-1$
      if (manifest != null) {
        throw new SAXException(
            "unexpected ["
                + name //$NON-NLS-1$
                + "] element (manifest already defined)"); //$NON-NLS-1$
      }
      manifest = new ModelManifestInfo();
      manifest.setId(attributes.getValue("id")); // $NON-NLS-1$
      manifest.setVersion(attributes.getValue("version")); // $NON-NLS-1$
      manifest.setVendor(attributes.getValue("vendor")); // $NON-NLS-1$
    } else if ("plugin-fragment".equals(name)) { // $NON-NLS-1$
      if (manifest != null) {
        throw new SAXException(
            "unexpected ["
                + name //$NON-NLS-1$
                + "] element (manifest already defined)"); //$NON-NLS-1$
      }
      manifest = new ModelManifestInfo();
      manifest.setId(attributes.getValue("id")); // $NON-NLS-1$
      manifest.setVersion(attributes.getValue("version")); // $NON-NLS-1$
      manifest.setVendor(attributes.getValue("vendor")); // $NON-NLS-1$
      manifest.setPluginId(attributes.getValue("plugin-id")); // $NON-NLS-1$
      if (attributes.getValue("plugin-version") != null) { // $NON-NLS-1$
        manifest.setPluginVersion(attributes.getValue("plugin-version")); // $NON-NLS-1$
      }
      if (attributes.getValue("match") != null) { // $NON-NLS-1$
        manifest.setMatchingRule(
            MatchingRule.fromCode(attributes.getValue("match"))); // $NON-NLS-1$
      } else {
        manifest.setMatchingRule(MatchingRule.COMPATIBLE);
      }
    } else {
      // ignore all other elements
    }
  }

  /**
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String,
   *     java.lang.String)
   */
  @Override
  public void endElement(final String uri, final String localName, final String qName) {
    if (log.isDebugEnabled()) {
      log.debug(
          "endElement - ["
              + uri
              + "]/["
              + localName //$NON-NLS-1$ //$NON-NLS-2$
              + "]/["
              + qName
              + "]"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // no-op
  }

  /** @see org.xml.sax.ContentHandler#characters(char[], int, int) */
  @Override
  public void characters(final char[] ch, final int start, final int length) {
    // ignore all characters
  }

  ModelManifestInfo getResult() {
    return manifest;
  }
}
