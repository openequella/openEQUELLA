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

package com.tle.core.entity.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.common.LockedException;
import com.thoughtworks.xstream.XStream;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.EntityPack;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.security.TargetList;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.dao.AbstractEntityDao;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.remoting.RemoteAbstractEntityService;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public interface AbstractEntityService<B extends EntityEditingBean, T extends BaseEntity>
	extends
		RemoteAbstractEntityService<T>
{
	/**
	 * Only use this in the REST entity deserializer
	 * 
	 * @param uuid
	 * @return
	 */
	T getForRestEdit(String uuid);

	Class<T> getEntityClass();

	/**
	 * Used by BaseEntityXmlConverter via the EntityRegistry. Handles
	 * deserialiazation of classes (e.g. you have multiple subclasses of T)
	 * 
	 * @return
	 */
	@Nullable
	List<Class<? extends T>> getAdditionalEntityClasses();

	String getEditPrivilege();

	T getWithNoSecurity(long id);

	T getForComparison(long id, @Nullable ComparisonEntityInitialiser<T> init);

	void delete(T entity, boolean checkReferences);

	// No security??
	void archive(T entity);

	// No security??
	void unarchive(T entity);

	void toggleEnabled(String uuid);

	List<T> enumerate();

	List<T> enumerateListable();

	List<T> enumerateListableIncludingSystem();

	List<T> enumerateDeletable();

	EntityPack<T> startEdit(T entity);

	<S extends EntityEditingSession<B, T>> void validate(S session, T entity) throws InvalidDataException;

	<S extends EntityEditingSession<B, T>> S cloneIntoSession(String entityUuid);

	byte[] exportEntity(T entity, boolean withSecurity);

	Set<String> getReferencedUsers();

	AbstractEntityDao<T> getEntityDao();

	void prepareDelete(T entity, ConverterParams params);

	void prepareExport(TemporaryFileHandle staging, T entity, ConverterParams params);

	void prepareImport(TemporaryFileHandle importFolder, T entity, ConverterParams params);

	// @Transactional(propagation = Propagation.MANDATORY)
	void afterImport(TemporaryFileHandle importFolder, T entity, ConverterParams params);

	List<T> getByIds(Collection<Long> ids);

	List<T> getByStringIds(Collection<String> ids);

	List<T> getByUuids(Collection<String> uuids);

	List<Long> convertUuidsToIds(Collection<String> uuids);

	Collection<String> convertToUuids(Collection<T> entities);

	String getExportImportFolder();

	XStream getXStream();

	// No security
	List<T> search(String query, boolean allowArchived, int offset, int perPage);

	/**
	 * For REST calls or anything not using some sort of "session" May throw a
	 * LockedException
	 * 
	 * @param entity
	 */
	void save(T entity, @Nullable TargetList targetList, @Nullable Map<Object, TargetList> otherTargetLists,
		@Nullable String stagingUuid, @Nullable String lockId, boolean keepLocked) throws LockedException;

	/**
	 * @param entity A brand spanking new entity
	 * @return session
	 */
	<S extends EntityEditingSession<B, T>> S startNewSession(T entity);

	/**
	 * @param entityUuid The uuid of the entity to be loaded up for editing
	 * @return session
	 */
	<S extends EntityEditingSession<B, T>> S startEditingSession(String entityUuid);

	/**
	 * @param session
	 */
	<S extends EntityEditingSession<B, T>> S loadSession(String sessionId);

	/**
	 * @param session
	 */
	void saveSession(EntityEditingSession<B, T> session);

	void commitSessionId(String sessionId);

	void commitSession(EntityEditingSession<B, T> session);

	void cancelSessionId(String sessionId);

	void cancelSession(EntityEditingSession<B, T> session);

	boolean canCreate();

	boolean canList();

	boolean canEdit(BaseEntityLabel entity);

	boolean canEdit(T entity);

	boolean canDelete(BaseEntityLabel entity);

	boolean canDelete(T entity);

	boolean canView(T entity);

	class ComparisonEntityInitialiser<T>
	{
		public void preUnlink(T t)
		{
			// To be overridden
		}

		public void postUnlink(T t)
		{
			// To be overridden
		}
	}

	void afterAdd(EntityPack<T> pack);

}
