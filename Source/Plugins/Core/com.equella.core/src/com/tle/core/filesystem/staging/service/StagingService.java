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

package com.tle.core.filesystem.staging.service;

import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.handle.StagingFile;

public interface StagingService {
  StagingFile createStagingArea();

  void removeStagingArea(StagingFile staging, boolean deleteFiles);

  void removeAllStagingAreas(String sessionId);

  boolean stagingExists(String stagingId);

  void removeUnusedStagingAreas();

  /**
   * Deletes the file from the staging area. If the file does not exist, it will return false.
   *
   * @param stagingUuid The UUID of the staging area
   * @param filepath The path of the file to delete
   * @return true if the file was successfully deleted, false if it did not exist
   * @throws NotFoundException if no staging area with the given UUID exists or if the file path is
   *     invalid
   */
  boolean deleteFile(String stagingUuid, String filepath);

  /**
   * Retrieves a staging file by its UUID.
   *
   * @param stagingUuid The UUID of the staging area
   * @return The StagingFile object corresponding to the given UUID
   * @throws NotFoundException if no staging area with the given UUID exists
   */
  StagingFile getStagingFile(String stagingUuid);

  /**
   * Ensures that a staging area exists for the given UUID. If it does not exist, it will throw an
   * exception.
   *
   * @param stagingUuid The UUID of the staging area to ensure
   * @throws NotFoundException if no staging area with the given UUID exists
   */
  void ensureStaging(String stagingUuid);

  /**
   * Ensures that a file exists in the staging area. If the file does not exist, it will throw an
   * exception.
   *
   * @param stagingUuid The UUID of the staging area
   * @param filepath The path of the file to ensure exists
   * @throws NotFoundException if no staging area with the given UUID exists or if the file path is
   *     invalid
   */
  void ensureFileExists(String stagingUuid, String filepath);

  /**
   * Ensures that a file exists in the staging area. If the file does not exist, it will throw an
   * exception.
   *
   * @param staging The StagingFile object representing the staging area
   * @param filepath The path of the file to ensure exists
   * @throws NotFoundException if no staging area with the given UUID exists or if the file path is
   *     invalid
   */
  void ensureFileExists(StagingFile staging, String filepath);
}
