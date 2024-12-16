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

package com.tle.web.core.servlet.webdav;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Pair;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.mimetypes.MimeTypeService;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** @author aholland */
@SuppressWarnings("nls")
public class WebdavProps {
  private final MimeTypeService mimeTypeService;

  private final Set<String> requestedProperties;
  private final Set<String> notFoundProperties;
  private final List<Pair<String, String>> props;

  /** @param requestedProperties If null then "allprop" */
  public WebdavProps(final MimeTypeService mimeTypeService, final Set<String> requestedProperties) {
    this.requestedProperties = requestedProperties;
    notFoundProperties = new HashSet<String>();
    if (requestedProperties != null) {
      notFoundProperties.addAll(requestedProperties);
    }

    this.props = new ArrayList<Pair<String, String>>();
    this.mimeTypeService = mimeTypeService;
  }

  public void addProp(String key, String value) {
    // Remove from not found props
    String keyRoot = key;
    int slashIndex = key.indexOf('/');
    if (slashIndex >= 0) {
      keyRoot = key.substring(0, slashIndex);
    }

    if (requestedProperties == null || requestedProperties.contains(keyRoot)) {
      props.add(new Pair<String, String>(key, value));
      notFoundProperties.remove(keyRoot);
    }
  }

  public PropBagEx createResponsePropstats(final PropBagEx responseNode) {
    final PropBagEx okPropstat = responseNode.newSubtree("propstat");
    okPropstat.createNode("status", "HTTP/1.1 200 OK");
    final PropBagEx okProps = okPropstat.newSubtree("prop");

    for (Pair<String, String> property : props) {
      final String key = property.getFirst();
      okProps.createNode(key, property.getSecond());
    }

    if (notFoundProperties.size() > 0) {
      final PropBagEx notFoundPropstat = responseNode.newSubtree("propstat");
      notFoundPropstat.createNode("status", "HTTP/1.1 404 Not Found");
      final PropBagEx notFoundProps = notFoundPropstat.newSubtree("prop");
      for (String notFound : notFoundProperties) {
        notFoundProps.createNode(notFound, "");
      }
    }

    return responseNode;
  }

  public void addFileProps(String parentName, FileEntry file) {
    final String filename = file.getName();
    addProp("resourcetype", "");
    addProp("getcontenttype", mimeTypeService.getMimeTypeForFilename(filename));
    addProp("getcontentlength", Long.toString(file.getLength()));
    addProp("getlastmodified", getFileRFC1123Date(file));
    addProp("iscollection", "0");
    addProp("isfolder", "0");
    addCommonProps(parentName, filename);
  }

  public void addFolderProps(String parentName, String filename, boolean isRoot) {
    addProp("resourcetype/collection", "");
    addProp("iscollection", "1");
    addProp("isfolder", "1");

    if (isRoot) {
      addProp("isroot", "1");
      addProp("name", "root");
      addProp("displayname", "root");
    }

    addCommonProps(parentName, filename);
  }

  private void addCommonProps(String parentName, String filename) {
    addProp("displayname", filename);
    addProp("name", filename);
    addProp("parentname", parentName);

    addProp("href", URLUtils.urlEncode(filename));

    addProp("ishidden", "0");
    addProp("isreadonly", "0");
  }

  /**
   * Get the RFC 1123 formatted date for the last modified date of the file entry as this is the
   * format required by WebDAV.
   *
   * @param file The file entry to get the last modified date for
   * @return The RFC 1123 formatted date for the last modified date of the file entry
   */
  private static String getFileRFC1123Date(FileEntry file) {
    final long lastModified = new File(file.getSystemPath()).lastModified();
    Instant lastModifiedInstant = Instant.ofEpochMilli(lastModified);
    ZonedDateTime lastModifiedGMT = ZonedDateTime.ofInstant(lastModifiedInstant, ZoneId.of("GMT"));

    // Create a formatter for RFC 1123 format
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            .withZone(ZoneId.of("GMT"));

    // Format the date and return
    return lastModifiedGMT.format(formatter);
  }
}
