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

package com.tle.core.util.ims;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.tle.annotation.Nullable;
import com.tle.common.Utils;
import com.tle.core.util.ims.beans.IMSManifest;
import io.github.xstream.mxparser.MXParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Provides various methods for dealing with IMS manifests.
 *
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public final class IMSUtilities {
  private static final Logger LOGGER = LoggerFactory.getLogger(IMSUtilities.class);

  public static final String KEY_EXPAND_IMS_PACKAGE = "EXPAND_IMS_PACKAGE";
  public static final String IMS_MANIFEST = "imsmanifest.xml";
  public static final String IMS_MANIFEST_COMBINED = "imsmanifest-combined.xml";

  private IMSUtilities() {
    throw new Error("Do not invoke");
  }

  /** Combines a split-up IMS manifest into a single entity. */
  public static void combine(FileManifestResolver resolver, Writer output)
      throws IOException, XmlPullParserException {
    @SuppressWarnings("unused")
    Combiner c = new Combiner(resolver, output);
  }

  /** Retrieves the title from an IMS manifest. */
  public static String getTitleFromManifest(Reader xml) throws XmlPullParserException, IOException {
    StringWriter sr = new StringWriter();
    CharStreams.copy(xml, sr);

    String bufXml = sr.getBuffer().toString();

    String v =
        getValueForPath(
            "manifest/metadata/lom/general/title/string|langstring", new StringReader(bufXml));
    if (v == null) {
      v =
          getValueForPath(
              "manifest/organizations/organization/item/metadata/lom/general/title/string|langstring",
              new StringReader(bufXml));
    }
    return v;
  }

  /** Retrieves the description from an IMS manifest. */
  public static String getDescriptionFromManifest(Reader xml)
      throws XmlPullParserException, IOException {
    StringWriter sr = new StringWriter();
    CharStreams.copy(xml, sr);

    String bufXml = sr.getBuffer().toString();

    String v =
        getValueForPath(
            "manifest/metadata/lom/general/description/string|langstring",
            new StringReader(bufXml));
    if (v == null) {
      v =
          getValueForPath(
              "manifest/organizations/organization/item/metadata/lom/general/description/string|langstring",
              new StringReader(bufXml));
    }
    return v;
  }

  /** Retrieves the title from an IMS manifest. */
  public static String getRightsDescriptionFromManifest(Reader xml)
      throws XmlPullParserException, IOException {
    return getValueForPath("manifest/metadata/lom/rights/description/string|langstring", xml);
  }

  /**
   * @param manifestStream Will not be closed.
   * @return
   */
  public static String getScormVersionFromStream(InputStream manifestStream) {
    String wellWhatWasIt = null;
    try {
      wellWhatWasIt = getScormVersion(new UnicodeReader(manifestStream, "UTF-8"), false);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (XmlPullParserException parsimonious) {
      throw new RuntimeException(parsimonious);
    }
    return wellWhatWasIt;
  }

  public static String getScormVersion(Reader xml) throws XmlPullParserException, IOException {
    return getScormVersion(xml, true);
  }

  public static String getScormVersion(Reader xml, boolean alwaysReturnDefaultVersion)
      throws XmlPullParserException, IOException {
    XmlPullParser parser = new MXParser();
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    parser.setInput(xml);

    parser.next();
    String namespace = parser.getNamespace("adlcp");
    String version = null;
    if (namespace != null) {
      if (namespace.equals("http://www.adlnet.org/xsd/adlcp_rootv1p2")) {
        version = "1.2";
      } else if (namespace.equals("http://www.adlnet.org/xsd/adlcp_v1p3")) {
        version = "1.3";
      } else if (alwaysReturnDefaultVersion) {
        version = "1.2";
      }
    }
    return version;
  }

  /** Retrieves the value of an XPath from an XML stream. */
  private static String getValueForPath(String xpath, Reader xml)
      throws XmlPullParserException, IOException {
    XmlPullParser parser = new MXParser();
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
    parser.setInput(xml);

    int depth = 1;
    String[] splits = xpath.split("/");

    int event = parser.getEventType();
    while (event != XmlPullParser.END_DOCUMENT) {
      if (event == XmlPullParser.START_TAG && parser.getDepth() == depth) {
        String[] elemNames = splits[depth - 1].split("\\|");
        for (int i = 0; i < elemNames.length; i++) {
          if (parser.getName().equals(elemNames[i])) {
            if (depth == splits.length) {
              if (parser.next() == XmlPullParser.TEXT) {
                return parser.getText();
              } else {
                return null;
              }
            } else {
              depth++;
            }
          }
        }
      }
      event = parser.next();
    }

    return null;
  }

  public static PropBagEx shrinkXML(IMSManifest ims) {
    StringBuilder sbuf = new StringBuilder("<xml>");
    ims.addToXMLString(sbuf);
    sbuf.append("</xml>");
    return new PropBagEx(sbuf.toString());
  }

  // ////////////////// HELPER CLASSES
  // /////////////////////////////////////////////////////////

  private static final class CollectionHashMap<K, V> extends HashMap<K, Collection<V>> {
    private static final long serialVersionUID = 1L;

    public void add(K key, V value) {
      Collection<V> col = get(key);
      if (col == null) {
        col = new ArrayList<V>();
        super.put(key, col);
      }
      col.add(value);
    }
  }

  /**
   * Helper class for combining manifests in to a single entity.
   *
   * @author Nicholas Read
   */
  private static class Combiner {
    private static final String ADLCP_LOCATION = "location";
    private static final Set<String> ADLCP_NAMESPACES = new HashSet<String>();

    static {
      ADLCP_NAMESPACES.add("http://www.adlnet.org/xsd/adlcp_rootv1p2");
      ADLCP_NAMESPACES.add("http://www.adlnet.org/xsd/adlcp_v1p3");
    }

    private final Set<String> declaredNamespaces = new HashSet<String>();
    private final CollectionHashMap<Integer, String> namespaceDepths =
        new CollectionHashMap<Integer, String>();
    private XmlPullParser parser;
    private final Writer output;
    private final FileManifestResolver resolver;

    public Combiner(FileManifestResolver resolver, Writer output)
        throws IOException, XmlPullParserException {
      this.output = output;
      this.resolver = resolver;

      Reader reader = null;
      try {
        reader = resolver.getStream();

        parser = new MXParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(reader);

        parseXml();
      } finally {
        Closeables.close(reader, true); // Quietly
      }
    }

    private void parseXml() throws IOException, XmlPullParserException {
      // Indicates whether the element being written has an open start
      // tag.
      boolean openElement = false;

      int eventType = parser.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {
          // Jira TLE-2307
          if (openElement) {
            output.write(">");
            openElement = false;
          }

          String namespace = parser.getNamespace();
          String name = parser.getName();

          if (ADLCP_NAMESPACES.contains(namespace) && name.equals(ADLCP_LOCATION)) {
            includeDocument();
          } else {
            output.write('<');
            addElementName();
            copyAttributes();
            openElement = true;
          }
        } else if (eventType == XmlPullParser.END_TAG) {
          if (openElement) {
            output.write(" />");
            openElement = false;
          } else {
            output.write("</");
            addElementName();
            output.write('>');
          }
          removeNamespaces();
        } else if (eventType == XmlPullParser.TEXT) {
          if (openElement) {
            output.write(">");
            openElement = false;
          }

          output.write(Utils.ent(parser.getText()));
        }
        eventType = parser.next();
      }
    }

    /**
     * Add the current elements name (and possible namespace prefix) to the current output stream.
     */
    private void addElementName() throws IOException {
      String prefix = parser.getPrefix();
      if (prefix != null) {
        output.write(prefix);
        output.write(':');
      }
      output.write(parser.getName());
    }

    private void removeNamespaces() {
      Collection<String> col = namespaceDepths.remove(parser.getDepth());
      if (col != null) {
        for (String ns : col) {
          declaredNamespaces.remove(ns);
        }
      }
    }

    /** Copies over attributes and namespace declarations. */
    private void copyAttributes() throws XmlPullParserException, IOException {
      // Add any required namespace declarations
      final int nsCount = parser.getNamespaceCount(parser.getDepth());
      if (nsCount > 0) {
        for (int i = 0; i < nsCount; i++) {
          String uri = parser.getNamespaceUri(i);
          String prefix = parser.getNamespacePrefix(i);
          String uriKey = prefix + "_" + uri;
          if (!declaredNamespaces.contains(uriKey)) {
            output.write(" xmlns");
            if (prefix != null) {
              output.write(':');
              output.write(prefix);
            }
            output.write("=\"");
            output.write(uri);
            output.write('"');

            declaredNamespaces.add(uriKey);

            namespaceDepths.add(parser.getDepth(), uriKey);
          }
        }
      }

      // Add any attributes
      final int attrCount = parser.getAttributeCount();
      for (int i = 0; i < attrCount; i++) {
        output.write(' ');

        String prefix = parser.getAttributePrefix(i);
        if (prefix != null && prefix.length() > 0) {
          output.write(prefix);
          output.write(':');
        }

        output.write(parser.getAttributeName(i));
        output.write("=\"");
        output.write(Utils.ent(parser.getAttributeValue(i)));
        output.write('"');
      }
    }

    /** Includes another XML document relative to the current manifest. */
    private void includeDocument() throws IOException, XmlPullParserException {
      int event = -1;
      while (event != XmlPullParser.TEXT) {
        event = parser.next();
      }

      FileManifestResolver newResolver = resolver.getResolverForPath(parser.getText());
      if (newResolver != null) {
        @SuppressWarnings("unused")
        Combiner c = new Combiner(newResolver, output);
      }

      while (event != XmlPullParser.END_TAG) {
        event = parser.next();
      }
    }
  }

  /**
   * Resolves manifests stored on disk.
   *
   * <p>Used by the manifest combiner to open manifests and resolve sub-manifest paths from {@code
   * <adlcp:location>} elements relative to the manifest that references them.
   *
   * <p>Manifest paths come from the uploaded package rather than from openEQUELLA, so every
   * resolved path must be validated to remain within the package root. The package root is the
   * directory containing the top-level manifest and is preserved across nested resolvers, allowing
   * subdirectories while preventing path traversal outside the package.
   */
  public static class FileManifestResolver {
    // The manifest file read by this resolver.
    private final File baseFile;

    // The canonical package root that all resolved paths must remain within.
    private final Path canonicalPackageRoot;

    /**
     * Creates a resolver for the top level manifest of a package. The directory containing the
     * manifest becomes the boundary that this resolver, and every resolver derived from it, must
     * stay within.
     */
    public FileManifestResolver(File baseFile) {
      this(baseFile, getCanonicalPackageRoot(baseFile));
    }

    private FileManifestResolver(File baseFile, Path canonicalPackageRoot) {
      this.baseFile = baseFile;
      this.canonicalPackageRoot = canonicalPackageRoot;
    }

    // Resolves a file to its canonical form.
    private static File canonicalise(File file) {
      try {
        return file.getCanonicalFile();
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to canonicalise " + file.getAbsolutePath(), e);
      }
    }

    /**
     * Returns the canonical directory containing the given manifest.
     *
     * <p>Canonicalising first makes the path absolute, so any manifest - even one named without a
     * directory - has a parent directory to use as the root.
     */
    private static Path getCanonicalPackageRoot(File manifest) {
      return canonicalise(manifest).getParentFile().toPath();
    }

    /** Returns the stream for the current manifest. */
    public Reader getStream() throws IOException {
      return new UnicodeReader(new FileInputStream(baseFile), "UTF-8");
    }

    /**
     * Resolves a manifest path relative to the current manifest.
     *
     * <p>The resolved path must remain within the package root. If the file exists, a resolver for
     * that manifest is returned; otherwise {@code null} is returned.
     *
     * @param path the manifest path to resolve
     * @return a resolver for the referenced manifest, or {@code null} if it does not exist
     */
    @Nullable
    public FileManifestResolver getResolverForPath(String path) {
      return resolveWithinRoot(path)
          .filter(File::exists)
          .map(f -> new FileManifestResolver(f, canonicalPackageRoot))
          .orElse(null);
    }

    /**
     * Resolves a manifest path relative to the current manifest, rejecting any path that would
     * escape the package root.
     *
     * @param path the manifest path to resolve
     * @return the resolved file, or empty if it would fall outside the package root
     */
    private Optional<File> resolveWithinRoot(String path) {
      File f = new File(baseFile.getParentFile(), path);
      if (!isFileWithinPackageRoot(f)) {
        LOGGER.warn("Ignoring manifest path that resolves outside the package root: {}", path);
        return Optional.empty();
      }
      return Optional.of(f);
    }

    /**
     * Checks whether the resolved file remains within the package root.
     *
     * @param file the file resolved from the manifest path
     * @return {@code true} if the file is within the package root; otherwise {@code false}
     */
    private boolean isFileWithinPackageRoot(File file) {
      return canonicalise(file).toPath().startsWith(canonicalPackageRoot);
    }
  }
}
