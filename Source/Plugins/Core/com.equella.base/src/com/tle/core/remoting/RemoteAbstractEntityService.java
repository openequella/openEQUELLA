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

package com.tle.core.remoting;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.filesystem.FileEntry;
import java.io.IOException;
import java.util.List;

@NonNullByDefault
public interface RemoteAbstractEntityService<T extends BaseEntity> {
  T get(long id);

  @Nullable
  T getByUuid(String uuid);

  long identifyByUuid(String uuid);

  String getUuidForId(long id);

  BaseEntityLabel add(EntityPack<T> pack, boolean lockAfterwards);

  /**
   * @param entityid
   * @param checkReferences Disallow deletion if entity is in use
   */
  void delete(long entityid, boolean checkReferences);

  void archive(long entityid);

  void archive(List<Long> ids);

  void unarchive(long entityid);

  void unarchive(List<Long> ids);

  List<BaseEntityLabel> listEditable();

  List<BaseEntityLabel> listAll();

  List<BaseEntityLabel> listEnabled();

  List<BaseEntityLabel> listAllIncludingSystem();

  List<T> enumerateEditable();

  List<T> enumerateEnabled();

  EntityPack<T> getReadOnlyPack(long id);

  /**
   * Starts the editing of an existing entity, with staging area.
   *
   * @param id the ID of the entity to edit
   * @return an EntityPack with the entity ready for editing.
   */
  EntityPack<T> startEdit(long id);

  /**
   * Starts the creation of a new entity, with staging area.
   *
   * @return an EntityPack with a new entity ready for editing.
   */
  default EntityPack<T> startCreate() {
    throw new UnsupportedOperationException("Creation of new entities is not supported");
  }

  /**
   * Used to check if the service provides managed creation of new entities. If not, the UI needs to
   * handle creation in some other way.
   *
   * @return whether creating new entities is supported by the service.
   */
  default boolean isStartCreateSupported() {
    return false;
  }

  /**
   * Cancels the editing session for an entity and removes its lock.
   *
   * @param id the ID of the entity being edited
   * @param force if true, removes the lock regardless of which session owns it (forced unlock); if
   *     false, only removes the lock if the current session owns it, otherwise throws a
   *     LockedException
   */
  void cancelEdit(long id, boolean force);

  T stopEdit(EntityPack<T> pack, boolean unlock);

  /**
   * Checks if the entity with the given ID has any classes that reference it. Useful for
   * determining if an entity can be deleted, as well as for displaying a warning to the user before
   * editing.
   *
   * @param id the ID of the entity to check
   * @return true if there are classes that reference the entity, false otherwise
   */
  boolean hasReferencingClasses(long id);

  byte[] exportEntity(long id, boolean withSecurity);

  /**
   * Starts the process of importing an entity defined in a zip file, by extracting the zip file and
   * storing the contents in the staging area. <strong>The entity is not yet imported.</strong>
   *
   * <p>It is expected that after this call, the client will typically use startEdit() and
   * stopEdit() to complete the import process.
   *
   * @param zip the zip file to import
   * @return the entity pack containing the entity prepared for import
   */
  EntityPack<T> importEntity(byte[] zip);

  /**
   * @return a pair containing the entity ID, and the name bundle ID.
   */
  BaseEntityLabel clone(long id);

  // TODO flesh out

  void uploadFile(String stagingID, String filename, byte[] bytes) throws IOException;

  byte[] downloadFile(String stagingID, String filename) throws IOException;

  void deleteFileFolder(String stagingID, String path);

  void createFolder(String stagingID, String path, String name);

  FileEntry buildStagingTree(String stagingID, String path);
}
