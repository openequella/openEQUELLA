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

package com.tle.core.connectors.utils;

import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.core.connectors.blackboard.beans.Availability;
import com.tle.core.connectors.blackboard.beans.Content;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Thread safe, and currently leveraged for the generic LTI flows
// As/If this class becomes more widely used, the blackboard.beans.* should be generalized.
public class ConnectorEntityUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorEntityUtils.class);

  public static Optional<ConnectorFolder> parseFolder(
      Content currentRawFolder, ConnectorCourse course, Map<String, List<Content>> folderMap) {
    if (currentRawFolder == null) {
      LOGGER.error("currentRawFolder is null.");
      return Optional.empty();
    }
    final Content.ContentHandler handler = currentRawFolder.getContentHandler();
    if (handler != null
        && (Content.ContentHandler.RESOURCE_FOLDER.equals(handler.getId())
            || Content.ContentHandler.RESOURCE_LESSON.equals(handler.getId()))) {
      // Unavailable folders are inaccessible to students,
      // but should be available for instructors to push content to.
      final ConnectorFolder cc = new ConnectorFolder(currentRawFolder.getId(), course);
      if (currentRawFolder.getAvailability() != null) {
        cc.setAvailable(Availability.YES.equals(currentRawFolder.getAvailability().getAvailable()));
      } else {
        cc.setAvailable(false);
      }
      cc.setName(currentRawFolder.getTitle());
      cc.setLeaf(currentRawFolder.getHasChildren() != null && !currentRawFolder.getHasChildren());

      if (!cc.isLeaf()) {
        List<ConnectorFolder> children = new ArrayList<>();
        final List<Content> rawChildren = folderMap.get(cc.getId());
        if ((rawChildren != null) && (rawChildren.size() != 0)) {
          for (Content child : rawChildren) {
            final Optional<ConnectorFolder> cf = parseFolder(child, course, folderMap);
            if (cf.isPresent()) {
              children.add(cf.get());
            }
          }
          cc.setFolders(children);
        } else {
          LOGGER.debug(
              "Parent folder stated it was not a leaf, but there are no known children folders. "
                  + " Parent folder: "
                  + cc.getName()
                  + " - "
                  + cc.getId());
        }
      }
      return Optional.of(cc);
    }
    // Not a content folder.  Caller is responsible for checking null.
    return Optional.empty();
  }

  /**
   * Takes a list of folders from Bb API and organizes them into a hierarchy, returning a list of
   * the top level folders
   */
  public static List<ConnectorFolder> parseFolders(
      List<Content> rawFolders, ConnectorCourse course) {
    // Convert to a map for better access and sort by parent ID
    Map<String, List<Content>> folderMap = new HashMap<>();
    for (Content c : rawFolders) {
      // Assumption is null parent ID == base / root folder
      final String pid = c.getParentId() == null ? "" : c.getParentId();
      if (!folderMap.containsKey(pid)) {
        folderMap.put(pid, new ArrayList<>());
      }
      folderMap.get(pid).add(c);
    }

    String baseId = "";
    if (folderMap.get(baseId) == null) {
      // This is likely an Ultra view course - all content seems to have parentIDs.
      // There is one parent ID that will not have a corresponding content ID - that
      // parentID should be treated as the 'root'.

      // Pull out all the content IDs
      List<String> ids = new ArrayList<>();
      for (Content c : rawFolders) {
        ids.add(c.getId());
      }

      // See which parent ID is not in the content IDs
      for (String key : folderMap.keySet()) {
        if (!ids.contains(key)) {
          baseId = key;
        }
      }
    }

    List<ConnectorFolder> baseFolders = new ArrayList<>();
    // Starting with the 'empty' parent ID (ie top level folders),
    //  build out the connector folders.  There 'should' always be at
    //  least 1 folder without a parent ID, but to be sure...
    final List<Content> rawBaseFolders = folderMap.get(baseId);
    if ((rawBaseFolders != null) && (rawBaseFolders.size() != 0)) {
      for (Content rootFolder : rawBaseFolders) {
        final Optional<ConnectorFolder> baseFolder = parseFolder(rootFolder, course, folderMap);
        if (baseFolder.isPresent()) {
          baseFolders.add(baseFolder.get());
        }
      }
    } else {
      LOGGER.warn("Unable to find the base folders for the course " + course.getId());
    }
    return baseFolders;
  }

  /**
   * Recursively searches for a folder with the given ID
   *
   * @param folder
   * @param folderId
   * @return Optional
   */
  public static Optional<ConnectorFolder> findFolder(ConnectorFolder folder, String folderId) {
    if (folder.getId().equals(folderId)) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("With ID=" + folderId + ", found: " + folder);
      }
      return Optional.of(folder);
    }

    // Not this folder, so check it's sub folders
    for (ConnectorFolder subFolder : folder.getFolders()) {
      Optional<ConnectorFolder> possibleSubFolder = findFolder(subFolder, folderId);
      if (possibleSubFolder.isPresent()) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("With ID=" + folderId + ", found subfolder: " + possibleSubFolder.get());
        }
        return possibleSubFolder;
      }
    }

    // The folder and its subfolders do not contain the folderId
    return Optional.empty();
  }
}
