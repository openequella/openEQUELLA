/*
 * Created on 18/04/2006
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
