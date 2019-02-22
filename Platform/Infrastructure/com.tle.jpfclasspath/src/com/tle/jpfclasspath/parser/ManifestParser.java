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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** @version $Id: ManifestParser.java,v 1.4 2007/03/03 17:16:26 ddimon Exp $ */
public final class ManifestParser {
  static Log log = LogFactory.getLog(ManifestParser.class);

  static final String PLUGIN_DTD_1_0 = loadPluginDtd("1_0"); // $NON-NLS-1$

  private static String loadPluginDtd(final String version) {
    try {
      Reader in =
          new InputStreamReader(
              ManifestParser.class.getResourceAsStream(
                  "plugin_" //$NON-NLS-1$
                      + version
                      + ".dtd"),
              "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
      try {
        StringBuilder sBuf = new StringBuilder();
        char[] cBuf = new char[64];
        int read;
        while ((read = in.read(cBuf)) != -1) {
          sBuf.append(cBuf, 0, read);
        }
        return sBuf.toString();
      } finally {
        in.close();
      }
    } catch (IOException ioe) {
      log.error("can't read plug-in DTD file of version " + version, ioe); // $NON-NLS-1$
    }
    return null;
  }

  private static EntityResolver getDtdEntityResolver() {
    return new EntityResolver() {
      @Override
      public InputSource resolveEntity(final String publicId, final String systemId) {
        if (publicId == null) {
          log.debug("can't resolve entity, public ID is NULL, systemId=" + systemId); // $NON-NLS-1$
          return null;
        }
        if (PLUGIN_DTD_1_0 == null) {
          return null;
        }
        if (publicId.equals("-//JPF//Java Plug-in Manifest 1.0") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.7") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.6") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.5") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.4") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.3") // $NON-NLS-1$
            || publicId.equals("-//JPF//Java Plug-in Manifest 0.2")) { // $NON-NLS-1$
          if (log.isDebugEnabled()) {
            log.debug(
                "entity resolved to plug-in manifest DTD, publicId=" //$NON-NLS-1$
                    + publicId
                    + ", systemId="
                    + systemId); //$NON-NLS-1$
          }
          return new InputSource(new StringReader(PLUGIN_DTD_1_0));
        }
        if (log.isDebugEnabled()) {
          log.debug(
              "entity not resolved, publicId=" //$NON-NLS-1$
                  + publicId
                  + ", systemId="
                  + systemId); //$NON-NLS-1$
        }
        return null;
      }
    };
  }

  private final SAXParserFactory parserFactory;
  private final EntityResolver entityResolver;

  public ManifestParser() {
    parserFactory = SAXParserFactory.newInstance();
    entityResolver = getDtdEntityResolver();
    log.info("got SAX parser factory - " + parserFactory); // $NON-NLS-1$
  }

  public ModelPluginManifest parseManifest(InputStream strm)
      throws ParserConfigurationException, SAXException, IOException {
    ManifestHandler handler = new ManifestHandler(entityResolver);
    try {
      parserFactory.newSAXParser().parse(strm, handler);
    } finally {
      strm.close();
    }
    ModelPluginManifest result = handler.getResult();
    return result;
  }
}
