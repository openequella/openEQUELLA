/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.IOException;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.filesystem.FileEntry;

@NonNullByDefault
public interface RemoteAbstractEntityService<T extends BaseEntity>
{
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

	EntityPack<T> startEdit(long id);

	void cancelEdit(long id, boolean force);

	T stopEdit(EntityPack<T> pack, boolean unlock);

	List<Class<?>> getReferencingClasses(long id);

	byte[] exportEntity(long id, boolean withSecurity);

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
