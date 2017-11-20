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

package com.tle.core.entity.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.InUseException;
import com.dytech.edge.exceptions.ModifyingSystemTypeException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.dytech.edge.wizard.WizardTimeoutException;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.thoughtworks.xstream.XStream;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.ImportExportPack;
import com.tle.common.Pair;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.dao.AbstractEntityDao;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.entity.service.EntityLockingService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.events.services.EventService;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.XmlHelper;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.util.archive.ArchiveType;

/*
 * @author Nicholas Read
 */
@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractEntityServiceImpl<B extends EntityEditingBean, T extends BaseEntity, S extends AbstractEntityService<B, T>>
	implements
		AbstractEntityService<B, T>,
		DeleteHandler,
		UserChangeListener
{
	private static final Logger LOGGER = Logger.getLogger(AbstractEntityService.class);
	private static final String ENTITY_XML = "_entity.xml";

	private final AbstractEntityDao<T> entityDao;
	@Nullable
	private final PrivilegeTree.Node privilegeNode;
	private final String privilegeType;

	@Inject
	private EventService eventService;
	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected EntityLockingService lockingService;
	@Inject
	protected StagingService stagingService;
	@Inject
	protected TLEAclManager aclManager;
	@Inject
	protected AuditLogService auditLogService;
	@Inject
	protected InitialiserService initialiserService;
	@Inject
	protected InstitutionImportService institutionImportService;
	@Inject
	protected InstitutionService institutionService;
	@Inject
	private EntityRegistry registry;
	@Inject
	protected XmlHelper xmlHelper;
	@Inject
	private UserSessionService sessionService;

	// X-treme!
	@Nullable
	private XStream xstream;

	public AbstractEntityServiceImpl(PrivilegeTree.Node privilegeNode, AbstractEntityDao<T> entityDao)
	{
		this.privilegeNode = privilegeNode;
		this.entityDao = entityDao;
		privilegeType = getClass().getAnnotation(SecureEntity.class).value();
	}

	@Override
	public Class<T> getEntityClass()
	{
		return entityDao.getPersistentClass();
	}

	@Nullable
	@Override
	public List<Class<? extends T>> getAdditionalEntityClasses()
	{
		return null;
	}

	@Override
	public String getEditPrivilege()
	{
		return "EDIT_" + privilegeType;
	}

	@Override
	public T get(long id)
	{
		T entity = entityDao.findById(id);
		if( entity == null )
		{
			throw new NotFoundException("Couldn't find entity '" + id + "' : " + getClass().getName());
		}
		else
		{
			return entity;
		}
	}

	@Override
	public T getWithNoSecurity(long id)
	{
		return get(id);
	}

	@Override
	public T getForComparison(long id, @Nullable ComparisonEntityInitialiser<T> init)
	{
		T base = get(id);
		if( init != null )
		{
			init.preUnlink(base);
		}
		entityDao.unlinkFromSession(base);
		if( init != null )
		{
			init.postUnlink(base);
		}
		return base;
	}

	@Nullable
	@Override
	// @Transactional
	public T getByUuid(String uuid)
	{
		return entityDao.findByCriteria(Restrictions.eq("uuid", uuid), getInstitutionCriterion());
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional
	public T getForRestEdit(String uuid)
	{
		final T entity = getByUuid(uuid);
		if( entity == null )
		{
			throw new NotFoundException(uuid);
		}
		// initialiserService.initialise(entity);
		// getEntityDao().unlinkFromSession(entity);
		// getEntityDao().clear();
		return entity;
	}

	@Override
	public long identifyByUuid(String uuid)
	{
		T entity = getByUuid(uuid);
		return entity != null ? entity.getId() : 0;
	}

	@Override
	public String getUuidForId(long id)
	{
		return entityDao.getUuidForId(id);
	}

	@Override
	@SecureOnCall(priv = SecurityConstants.CREATE_VIRTUAL_BASE)
	@Transactional
	public BaseEntityLabel add(EntityPack<T> pack, boolean lockAfterwards)
	{
		return doAdd(pack, null, lockAfterwards);
	}

	/**
	 * @param session May be null
	 * @param pack
	 * @param lockAfterwards
	 * @return
	 */
	@SecureOnCall(priv = SecurityConstants.CREATE_VIRTUAL_BASE)
	@Transactional
	public BaseEntityLabel doAdd(EntityPack<T> pack, @Nullable EntityEditingSession<B, T> session,
		boolean lockAfterwards)
	{
		BaseEntityLabel label = addInternal(session, pack, lockAfterwards);
		afterAdd(pack);
		return label;
	}

	@Override
	public void afterAdd(EntityPack<T> pack)
	{
		// For subclasses
	}

	/**
	 * @param session May be null (e.g. admin console)
	 * @param pack
	 * @param lockAfterwards
	 * @return
	 */
	protected BaseEntityLabel addInternal(@Nullable EntityEditingSession<B, T> session, EntityPack<T> pack,
		boolean lockAfterwards)
	{
		T entity = pack.getEntity();

		// TODO: remove this check when they all do it
		B bean = null;
		if( isUseEditingBean() && session != null )
		{
			bean = session.getBean();
			validateBean(bean, true);
			populateEntity(bean, entity);
		}

		validate(session, entity, true);

		entity.setInstitution(CurrentInstitution.get());
		entity.setDateCreated(new Date());
		entity.setDateModified(entity.getDateCreated());
		if( Check.isEmpty(entity.getOwner()) )
		{
			entity.setOwner(CurrentUser.getUserID());
		}

		beforeAdd(pack, lockAfterwards);
		long id = entityDao.save(entity);

		String stagingID = pack.getStagingID();
		StagingFile stagingFile = null;
		if( stagingID != null )
		{
			stagingFile = new StagingFile(stagingID);
		}
		doAfterImport(stagingFile, bean, entity,
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

		saveTargetLists(session, pack);
		saveFiles(pack, !lockAfterwards, false, lockAfterwards, entity);

		auditLogService.logEntityCreated(id);

		BaseEntityLabel label = new BaseEntityLabel(id, entity.getUuid(), entity.getName().getId(), entity.getOwner(),
			entity.isSystemType());
		label.setPrivType(privilegeType);
		return label;
	}

	protected void beforeAdd(EntityPack<T> pack, boolean lockAfterwards)
	{
		// nothing by default
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(long id, boolean checkReferences)
	{
		delete(get(id), checkReferences);
	}

	@Override
	@SecureOnCall(priv = SecurityConstants.DELETE_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(T entity, boolean checkReferences)
	{
		ensureNonSystem(entity);

		if( checkReferences )
		{
			List<Class<?>> references = getReferencingClasses(entity.getId());
			if( references.size() != 0 )
			{
				StringBuilder refs = new StringBuilder();
				boolean first = true;
				for( Class<?> ref : references )
				{
					if( !first )
					{
						refs.append(", ");
					}
					refs.append(CurrentLocale.get(ref.getName()));
					first = false;
				}
				throw new InUseException(refs.toString());
			}
		}

		lockingService.getLock(entity);

		deleteReferences(entity);

		// Delete from database
		entityDao.delete(entity);
		deleteFiles(entity);

		// Delete all security things
		aclManager.setTargetList(privilegeNode, entity, null);

		for( Pair<Object, Node> other : getOtherTargetListObjects(entity) )
		{
			aclManager.setTargetList(other.getSecond(), other.getFirst(), null);
		}

		auditLogService.logEntityDeleted(entity.getId());
		afterDelete(entity);
	}

	/**
	 * Delete file attachments
	 * 
	 * @param entity
	 */
	protected void deleteFiles(T entity)
	{
		final EntityFile file = new EntityFile(entity);
		fileSystemService.removeFile(file, null);
	}

	protected void beforeDeleteFiles(EntityFile files)
	{
		// nothing by default
	}

	protected void afterDelete(T entity)
	{
		// nothing by default
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void archive(long id)
	{
		archive(get(id));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void archive(List<Long> ids)
	{
		for( long id : ids )
		{
			archive(get(id));
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void archive(T entity)
	{
		lockingService.getLock(entity);
		entity.setDisabled(true);
		entityDao.update(entity);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unarchive(long id)
	{
		unarchive(get(id));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unarchive(List<Long> ids)
	{
		for( long id : ids )
		{
			unarchive(get(id));
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void unarchive(T entity)
	{
		lockingService.getLock(entity);
		entity.setDisabled(false);
		entityDao.update(entity);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void toggleEnabled(String uuid)
	{
		final T ent = entityDao.getByUuid(uuid);
		ent.setDisabled(!ent.isDisabled());
		entityDao.save(ent);
	}

	@Override
	public final void prepareDelete(T entity, ConverterParams params)
	{
		deleteReferences(entity);
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		// None by default
		return new ArrayList<Class<?>>();
	}

	protected void deleteReferences(T entity)
	{
		// Nothing to do here
	}

	protected List<T> findAllWithCriterion(Criterion... criterion)
	{
		return entityDao.findAllByCriteria(addInstitutionCriterion(criterion));
	}

	@Override
	public List<T> enumerate()
	{
		return entityDao.enumerateAll();
	}

	@Override
	public List<T> enumerateEnabled()
	{
		return entityDao.enumerateEnabled();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public List<T> enumerateEditable()
	{
		return entityDao.enumerateAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.DELETE_VIRTUAL_BASE)
	public List<T> enumerateDeletable()
	{
		return entityDao.enumerateAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.LIST_VIRTUAL_BASE)
	public List<T> enumerateListable()
	{
		return entityDao.enumerateAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.LIST_VIRTUAL_BASE)
	public List<T> enumerateListableIncludingSystem()
	{
		return entityDao.enumerateAllIncludingSystem();
	}

	@Override
	public List<BaseEntityLabel> listAll()
	{
		return entityDao.listAll(privilegeType);
	}

	@Override
	public List<BaseEntityLabel> listEnabled()
	{
		return entityDao.listEnabled(privilegeType);
	}

	@Override
	public List<BaseEntityLabel> listAllIncludingSystem()
	{
		return entityDao.listAllIncludingSystem(privilegeType);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public List<BaseEntityLabel> listEditable()
	{
		return listAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public EntityPack<T> getReadOnlyPack(long id)
	{
		EntityPack<T> result = new EntityPack<T>();
		result.setEntity(get(id));

		fillTargetLists(result);

		return result;
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public EntityPack<T> startEdit(long id)
	{
		T entity = get(id);
		return startEdit(entity);
	}

	@Override
	@SecureOnCall(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public EntityPack<T> startEdit(T entity)
	{
		return startEditInternal(entity);
	}

	protected EntityPack<T> startEditInternal(T entity)
	{
		ensureNonSystem(entity);

		// FIXME: locking handling in Web
		if( lockingService.isEntityLocked(entity, CurrentUser.getUserID(), null) )
		{
			// It's our lock, just kill it
			lockingService.unlockEntity(entity, true);
		}
		lockingService.lockEntity(entity);

		// Prepare staging
		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(from) )
		{
			fileSystemService.copy(from, staging);
		}

		// Put together the entity pack
		EntityPack<T> result = new EntityPack<T>();
		result.setEntity(entity);
		result.setStagingID(staging.getUuid());

		fillTargetLists(result);
		return result;
	}

	/**
	 * Fills an entity pack with the required target lists.
	 */
	protected void fillTargetLists(ImportExportPack<T> pack)
	{
		pack.setTargetList(aclManager.getTargetList(privilegeNode, pack.getEntity()));

		Map<Object, TargetList> otherTargetLists = new HashMap<Object, TargetList>();
		for( Pair<Object, Node> other : getOtherTargetListObjects(pack.getEntity()) )
		{
			otherTargetLists.put(other.getFirst(), aclManager.getTargetList(other.getSecond(), other.getFirst()));
		}

		if( !otherTargetLists.isEmpty() )
		{
			pack.setOtherTargetLists(otherTargetLists);
		}
	}

	/**
	 * Fills an entity pack with the required target lists.
	 * 
	 * @param session may be null (e.g. admin console)
	 * @param pack
	 */
	protected void saveTargetLists(EntityEditingSession<B, T> session, EntityPack<T> pack)
	{
		aclManager.setTargetList(privilegeNode, pack.getEntity(), pack.getTargetList());

		Map<Object, TargetList> givenTargetLists = pack.getOtherTargetLists();
		if( givenTargetLists == null )
		{
			givenTargetLists = Collections.emptyMap();
		}

		for( Pair<Object, Node> other : getOtherTargetListObjects(pack.getEntity()) )
		{
			TargetList list = givenTargetLists.get(other.getFirst());
			aclManager.setTargetList(other.getSecond(), other.getFirst(), list);
		}
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public T stopEdit(EntityPack<T> pack, boolean unlock)
	{
		return doStopEdit(pack, null, unlock);
	}

	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public T doStopEdit(EntityPack<T> pack, @Nullable EntityEditingSession<B, T> session, boolean unlock)
	{
		T oldEntity = getForComparison(pack.getEntity().getId(), getComparisonInitialiserForStopEdit());

		return stopEdit(session, pack, oldEntity, false, null, unlock);
	}

	protected ComparisonEntityInitialiser<T> getComparisonInitialiserForStopEdit()
	{
		return new ComparisonEntityInitialiser<T>()
		{
			@Override
			public void preUnlink(T t)
			{
				initBundle(t.getName());
				initBundle(t.getDescription());
			}
		};
	}

	/**
	 * @param session May be null (from admin console)
	 * @param pack
	 * @param persistedEntity
	 * @param useLock Use the lockId supplied, even if null
	 * @param lockId
	 * @param unlock
	 * @return
	 */
	protected T stopEdit(@Nullable EntityEditingSession<B, T> session, EntityPack<T> pack, T persistedEntity,
		boolean useLock, @Nullable String lockId, boolean unlock)
	{
		T newEntity = pack.getEntity();

		// TODO: remove this check when they all do it
		B bean = null;
		if( isUseEditingBean() && session != null )
		{
			bean = session.getBean();
			validateBean(bean, false);
			populateEntity(bean, newEntity);
		}

		// FIXME: need to validate on the BEAN and move populateEntity below
		// this
		validate(session, newEntity);

		newEntity.setDateModified(new Date());
		newEntity.setDateCreated(persistedEntity.getDateCreated());
		newEntity.setInstitution(CurrentInstitution.get());

		try
		{
			if( useLock )
			{
				lockingService.getLock(newEntity, lockId);
			}
			else
			{
				lockingService.getLock(newEntity);
			}

			// Ensure we call this *after* we know we have the entity lock.
			beforeStopEdit(pack, persistedEntity, unlock);

			entityDao.clear();

			newEntity.setName(LanguageBundle.edit(persistedEntity.getName(), newEntity.getName(), this));
			newEntity.setDescription(
				LanguageBundle.edit(persistedEntity.getDescription(), newEntity.getDescription(), this));

			// Save the entity
			entityDao.update(newEntity);
			entityDao.flush();

			String stagingID = pack.getStagingID();
			StagingFile stagingFile = null;
			if( stagingID != null )
			{
				stagingFile = new StagingFile(stagingID);
			}
			doAfterImport(stagingFile, bean, newEntity,
				new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

			saveTargetLists(session, pack);

			entityDao.flush();

			saveFiles(pack, unlock, unlock, false, newEntity);
		}
		catch( LockedException e )
		{
			throw new RuntimeApplicationException(e);
		}

		auditLogService.logEntityModified(newEntity.getId());
		afterStopEdit(pack, persistedEntity);
		return newEntity;
	}

	protected void editCommonFields(T oldEntity, T newEntity)
	{
		oldEntity.setDateModified(new Date());
		oldEntity.setName(editBundle(oldEntity.getName(), newEntity.getName()));
		oldEntity.setDescription(editBundle(oldEntity.getDescription(), newEntity.getDescription()));
	}

	protected LanguageBundle editBundle(LanguageBundle oldBundle, LanguageBundle newBundle)
	{
		return LanguageBundle.edit(oldBundle, newBundle, this);
	}

	protected void saveFiles(EntityPack<T> pack, boolean commit, boolean unlock, boolean lockAfterwards, T newEntity)
	{
		final String stagingID = pack.getStagingID();

		final StagingFile staging = (stagingID == null ? null : new StagingFile(stagingID));
		final EntityFile files = new EntityFile(newEntity);
		try
		{
			beforeSaveFiles(staging, files, unlock, lockAfterwards, newEntity);
			if( staging != null )
			{
				if( commit )
				{
					fileSystemService.commitFiles(staging, files);
				}
				else
				{
					fileSystemService.saveFiles(staging, files);
				}
			}
			afterSaveFiles(staging, files, commit, lockAfterwards, newEntity);
		}
		catch( IOException ioe )
		{
			throw new RuntimeApplicationException(ioe);
		}
	}

	protected void beforeSaveFiles(@Nullable StagingFile staging, EntityFile files, boolean unlock,
		boolean lockAfterwards, T newEntity)
	{
		if( unlock )
		{
			lockingService.unlockEntity(newEntity, false);
		}
	}

	protected void afterSaveFiles(@Nullable StagingFile staging, EntityFile files, boolean commit,
		boolean lockAfterwards, T newEntity)
	{
		if( commit && staging != null )
		{
			stagingService.removeStagingArea(staging, false);
		}
		if( lockAfterwards )
		{
			lockingService.lockEntity(newEntity);
		}
	}

	protected void beforeStopEdit(EntityPack<T> pack, T oldEntity, boolean unlock)
	{
		// nothing by default
	}

	protected void afterStopEdit(EntityPack<T> pack, T oldEntity)
	{
		// nothing by default
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void cancelEdit(long id, boolean force)
	{
		lockingService.unlockEntity(get(id), force);
	}

	/**
	 * Adds the institution criterion to an existing array of criterion.
	 */
	protected Criterion[] addInstitutionCriterion(Criterion... criterions)
	{
		Criterion[] result = new Criterion[criterions.length + 1];
		result[0] = getInstitutionCriterion();
		System.arraycopy(criterions, 0, result, 1, criterions.length);
		return result;
	}

	protected Criterion getInstitutionCriterion()
	{
		Institution institution = CurrentInstitution.get();
		return Restrictions.eq("institution", institution);
	}

	@Override
	public <SESSION extends EntityEditingSession<B, T>> void validate(@Nullable SESSION session, T entity)
		throws InvalidDataException
	{
		if( isUseEditingBean() && session != null )
		{
			final B bean = session.getBean();
			if( bean != null )
			{
				validateBean(bean, false);
			}
		}
		else
		{
			validate(session, entity, false);
		}
	}

	protected final void validateBean(B bean, boolean overrideSystem) throws InvalidDataException
	{
		if( !overrideSystem && bean.isSystemType() )
		{
			throw new ModifyingSystemTypeException();
		}

		List<ValidationError> errors = new ArrayList<ValidationError>();

		// Ask the full implementation to do any checking
		doValidationBean(bean, errors);

		// Only one uuid per institution
		Criterion c4 = Restrictions.eq("uuid", bean.getUuid());
		Criterion c5 = Restrictions.eq("institution", CurrentInstitution.get());
		Criterion c6 = Restrictions.ne("id", bean.getId());

		if( entityDao.countByCriteria(c4, c5, c6) > 0 )
		{
			errors.add(new ValidationError("uuid",
				CurrentLocale.get("com.tle.core.services.entity.generic.validation.unique.uuid")));
		}

		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	protected final void validate(@Nullable EntityEditingSession<B, T> session, T entity, boolean overrideSystem)
		throws InvalidDataException
	{
		if( !overrideSystem )
		{
			ensureNonSystem(entity);
		}

		List<ValidationError> errors = new ArrayList<ValidationError>();

		// Ask the full implementation to do any checking
		doValidation(session, entity, errors);

		// Only one uuid per institution
		Criterion c4 = Restrictions.eq("uuid", entity.getUuid());
		Criterion c5 = Restrictions.eq("institution", CurrentInstitution.get());
		Criterion c6 = Restrictions.ne("id", entity.getId());

		if( entityDao.countByCriteria(c4, c5, c6) > 0 )
		{
			errors.add(new ValidationError("uuid",
				CurrentLocale.get("com.tle.core.services.entity.generic.validation.unique.uuid")));
		}

		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	/**
	 * Specifies other target lists that should be included in the entity pack.
	 */
	protected Collection<Pair<Object, Node>> getOtherTargetListObjects(T entity)
	{
		return Collections.emptyList();
	}

	protected abstract void doValidation(@Nullable EntityEditingSession<B, T> session, T entity,
		List<ValidationError> errors);

	protected void doValidationBean(B bean, List<ValidationError> errors)
	{
		// Nothing by default
	}

	protected final AbstractEntityDao<T> getDao()
	{
		return entityDao;
	}

	protected final EntityLockingService getLockingService()
	{
		return lockingService;
	}

	protected FileSystemService getFileSystemService()
	{
		return fileSystemService;
	}

	protected TLEAclManager getAclManager()
	{
		return aclManager;
	}

	@Override
	public EntityPack<T> importEntity(byte[] xml)
	{
		ByteArrayInputStream in = new ByteArrayInputStream(xml);
		StagingFile staging = stagingService.createStagingArea();
		try
		{
			fileSystemService.unzipFile(staging, in, ArchiveType.ZIP);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error unzipping file", ex);
			throw new RuntimeApplicationException("Error attempting to import entity on the server");
		}

		final ImportExportPack<T> importedEntity = xmlHelper.readXmlFile(staging, ENTITY_XML, getXStream());

		fileSystemService.removeFile(staging, ENTITY_XML);

		T entity = importedEntity.getEntity();
		entity.setOwner(CurrentUser.getUserID());
		initialiserService.initialiseClones(entity);

		prepareImport(staging, entity, new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

		EntityPack<T> pack = new EntityPack<T>(entity, staging.getUuid());
		pack.setOtherTargetLists(importedEntity.getOtherTargetLists());
		pack.setTargetList(importedEntity.getTargetList());

		return pack;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XStream getXStream()
	{
		if( xstream == null )
		{
			final Set<Class<? extends BaseEntity>> serviceClasses = new HashSet<Class<? extends BaseEntity>>();
			serviceClasses.add(getEntityClass());
			final List<?> moreServiceClasses = getAdditionalEntityClasses();
			if( moreServiceClasses != null )
			{
				serviceClasses.addAll((Collection<Class<? extends BaseEntity>>) moreServiceClasses);
			}
			xstream = xmlHelper.createXStream(getClass().getClassLoader());
			xstream.registerConverter(new BaseEntityXmlConverter(serviceClasses, registry));
			return xstream;
		}
		return xstream;
	}

	@Override
	public void prepareImport(TemporaryFileHandle importFolder, T entity, ConverterParams params)
	{
		// nothing by default
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public final void afterImport(TemporaryFileHandle importFolder, T entity, ConverterParams params)
	{
		doAfterImport(importFolder, null, entity, params);
	}

	protected void doAfterImport(TemporaryFileHandle importFolder, B bean, T entity, ConverterParams params)
	{
		// nothing by default
	}

	@Override
	public byte[] exportEntity(long id, boolean withSecurity)
	{
		T entity = get(id);
		return exportEntity(entity, withSecurity);
	}

	@Override
	@SecureOnCall(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	public byte[] exportEntity(T entity, boolean withSecurity)
	{
		ImportExportPack<T> pack = new ImportExportPack<T>();
		pack.setVersion(ApplicationVersion.get().getFull());
		pack.setEntity(entity);
		if( withSecurity && privilegeNode != null )
		{
			fillTargetLists(pack);
		}

		StagingFile staging = stagingService.createStagingArea();
		prepareExport(staging, entity, new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

		String xml = getXStream().toXML(pack);
		try
		{
			fileSystemService.copy(new EntityFile(entity), staging);
			fileSystemService.write(staging, ENTITY_XML, new StringReader(xml), false);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			fileSystemService.zipFile(staging, out, ArchiveType.ZIP);
			return out.toByteArray();
		}
		catch( IOException ex )
		{
			LOGGER.error("Error attempting to export entity", ex);
			throw new RuntimeApplicationException("Error exporting entity from the server");
		}
		finally
		{
			stagingService.removeStagingArea(staging, true);
		}
	}

	@Override
	public void prepareExport(TemporaryFileHandle staging, T entity, ConverterParams params)
	{
		initialiserService.initialise(entity, new EntityInitialiserCallback());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public BaseEntityLabel clone(long id)
	{
		T cloneEntity = getForClone(id);
		return clone(cloneEntity);
	}

	protected T getForClone(long id)
	{
		return getForComparison(id, new ComparisonEntityInitialiser<T>()
		{
			@Override
			public void preUnlink(T t)
			{
				initBundle(t.getName());
				initBundle(t.getDescription());
				t.getAttributes();
				preUnlinkForClone(t);
			}

			@Override
			public void postUnlink(T t)
			{
				t.setName(LanguageBundle.clone(t.getName()));
				t.setDescription(LanguageBundle.clone(t.getDescription()));
				t.setAttributes(new HashMap<String, String>(t.getAttributes()));
				postUnlinkForClone(t);
			}
		});
	}

	protected void initBundle(@Nullable LanguageBundle bundle)
	{
		if( bundle != null )
		{
			Hibernate.initialize(bundle.getStrings());
		}
	}

	@SecureOnCall(priv = SecurityConstants.CREATE_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public BaseEntityLabel clone(T entity)
	{
		return add(cloneIntoPack(entity), false);
	}

	/**
	 * Doesn't save anything to DB, hence no SecureOnCall
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public <SESSION extends EntityEditingSession<B, T>> SESSION cloneIntoSession(String entityUuid)
	{
		// Direct entity editing not supported!
		final B bean = createEditingBean();
		final T oldEntity = getByUuid(entityUuid);
		populateEditingBean(bean, oldEntity);

		bean.setId(0);
		bean.setUuid(UUID.randomUUID().toString());
		// Just add "Copy of " from the user's default Locale
		final String prefix = CurrentLocale.get("baseentity.clone.prefix");
		final LanguageBundleBean name = bean.getName();
		if( name != null )
		{
			name.setId(0);
			for( LanguageStringBean langstring : name.getStrings().values() )
			{
				langstring.setId(0);
				langstring.setText(prefix + ' ' + langstring.getText());
			}
		}
		final LanguageBundleBean description = bean.getDescription();
		if( description != null )
		{
			description.setId(0);
			for( LanguageStringBean langstring : description.getStrings().values() )
			{
				langstring.setId(0);
			}
		}
		cleanCloneBeans(bean);

		final EntityPack<T> pack = new EntityPack<T>();
		try
		{
			final T blankEnt = getEntityClass().newInstance();
			blankEnt.setUuid(bean.getUuid());
			pack.setEntity(blankEnt);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		final SESSION session = createSession(UUID.randomUUID().toString(), pack, bean);
		onStartNewSession(session, new EntityFile(oldEntity));
		sessionService.setAttribute(session.getSessionId(), session);

		return session;
	}

	/**
	 * You should remove all associated IDs here. If you load anything up via
	 * populateEditingBean you NEED to do this
	 * 
	 * @param bean
	 */
	protected void cleanCloneBeans(B bean)
	{
		// Nothing by default
	}

	private EntityPack<T> cloneIntoPack(T entity)
	{
		EntityPack<T> pack = new EntityPack<T>();
		pack.setEntity(entity);
		fillTargetLists(pack);

		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		fileSystemService.copy(from, staging);
		pack.setStagingID(staging.getUuid());

		beforeClone(staging, pack);

		entity.setUuid(UUID.randomUUID().toString());
		entity.setOwner(null);
		entity.setId(0);

		// Just add "Copy of " from the user's default Locale for now...
		final String prefix = CurrentLocale.get("baseentity.clone.prefix");
		final LanguageBundle names = entity.getName();
		for( LanguageString langstring : names.getStrings().values() )
		{
			langstring.setText(prefix + ' ' + langstring.getText());
		}

		processClone(pack);
		return pack;
	}

	protected void preUnlinkForClone(T entity)
	{
		// do nothing by default
	}

	protected void postUnlinkForClone(T entity)
	{
		// do nothing by default
	}

	protected void processClone(EntityPack<T> pack)
	{
		// do nothing by default
	}

	protected void beforeClone(TemporaryFileHandle staging, EntityPack<T> pack)
	{
		// do nothing by default
	}

	public void setInitialiserService(InitialiserService initialiserService)
	{
		this.initialiserService = initialiserService;
	}

	public void setRegistry(EntityRegistry registry)
	{
		this.registry = registry;
	}

	@Override
	public Set<String> getReferencedUsers()
	{
		return entityDao.getReferencedUsers();
	}

	protected void cloneListById(List<? extends IdCloneable> list)
	{
		for( IdCloneable cloneable : list )
		{
			cloneable.setId(0);
		}
	}

	@Override
	public AbstractEntityDao<T> getEntityDao()
	{
		return entityDao;
	}

	@Override
	public List<T> getByIds(Collection<Long> ids)
	{
		return getDao().getByIds(ids);
	}

	@Override
	public List<T> getByUuids(Collection<String> uuids)
	{
		return getDao().getByUuids(uuids);
	}

	@Override
	public List<T> getByStringIds(@Nullable Collection<String> ids)
	{
		List<Long> longIds = new ArrayList<Long>();
		if( ids != null )
		{
			for( String def : ids )
			{
				longIds.add(Long.parseLong(def));
			}
		}
		return getByIds(longIds);
	}

	@Override
	public List<Long> convertUuidsToIds(Collection<String> uuids)
	{
		List<T> objects = getByUuids(uuids);
		List<Long> longIds = new ArrayList<Long>();
		for( T obj : objects )
		{
			longIds.add(obj.getId());
		}
		return longIds;
	}

	@Override
	public Collection<String> convertToUuids(Collection<T> entities)
	{
		// Return a copy of the transformed list for serialization purposes
		return Lists.newArrayList(Collections2.transform(entities, new Function<T, String>()
		{
			@Override
			public String apply(T input)
			{
				return input.getUuid();
			}
		}));
	}

	@Override
	public FileEntry buildStagingTree(String stagingID, String path)
	{
		FileEntry stagingTree = null;

		try
		{
			stagingTree = fileSystemService.enumerateTree(new StagingFile(stagingID), path, null);
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		return stagingTree;
	}

	@Override
	public void uploadFile(String stagingID, String filename, byte[] bytes) throws IOException
	{
		StagingFile handle = new StagingFile(stagingID);
		fileSystemService.write(handle, filename, new ByteArrayInputStream(bytes), true);
	}

	@Override
	public byte[] downloadFile(String stagingID, String filename) throws IOException
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		InputStream inStream = fileSystemService.read(new StagingFile(stagingID), filename);
		try
		{
			ByteStreams.copy(inStream, outStream);
		}
		finally
		{
			inStream.close();
		}
		return outStream.toByteArray();
	}

	@Override
	public void deleteFileFolder(String stagingID, String path)
	{
		fileSystemService.removeFile(new StagingFile(stagingID), path);
	}

	@Override
	public void createFolder(String stagingID, String path, String name)
	{
		StagingFile handle = new StagingFile(stagingID);
		fileSystemService.mkdir(handle, path + name);
	}

	@Override
	public List<T> search(String query, boolean archived, int offset, int perPage)
	{
		return entityDao.search(query, archived, offset, perPage);
	}

	protected void ensureNonSystem(T entity)
	{
		if( entity.isSystemType() && !CurrentUser.getUserState().isSystem() )
		{
			throw new ModifyingSystemTypeException();
		}
	}

	@Override
	public String getExportImportFolder()
	{
		return null;
	}

	/**
	 * For REST calls or anything not using some sort of "session"
	 * 
	 * @param entity
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void save(T entity, @Nullable TargetList targetList, @Nullable Map<Object, TargetList> otherTargetLists,
		@Nullable String stagingUuid, @Nullable String lockId, boolean keepLocked) throws LockedException
	{
		if( entity.getId() == 0 )
		{
			final EntityPack<T> pack = new EntityPack<T>(entity, stagingUuid);
			pack.setTargetList(targetList);
			pack.setOtherTargetLists(otherTargetLists);
			doAdd(pack, null, keepLocked);
		}
		else
		{
			doStopEditWithLock(entity, targetList, otherTargetLists, stagingUuid, lockId, !keepLocked);
		}
	}

	/**
	 * Purely for the REST save
	 */
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public T doStopEditWithLock(T entity, @Nullable TargetList targetList,
		@Nullable Map<Object, TargetList> otherTargetLists, @Nullable String stagingUuid, @Nullable String lockId,
		boolean unlock)
	{
		entity.setDateModified(new Date());
		// FIXME: should not be edited via REST
		// newEntity.setDateCreated(persistedEntity.getDateCreated());
		entity.setInstitution(CurrentInstitution.get());

		validate(null, entity);

		final EntityPack<T> fakePack = new EntityPack<T>(entity, stagingUuid);

		if( lockId != null )
		{
			lockingService.getLock(entity, lockId);
		}
		else
		{
			lockingService.lockEntity(entity);
		}

		// Ensure we call this *after* we know we have the entity lock.
		beforeStopEdit(fakePack, entity, unlock);

		entityDao.clear();
		// newEntity.setName(LanguageBundle.edit(persistedEntity.getName(),
		// newEntity.getName(), this));
		// newEntity.setDescription(LanguageBundle.edit(persistedEntity.getDescription(),
		// newEntity.getDescription(),
		// this));

		// Save the entity
		entityDao.update(entity);
		entityDao.flush();

		StagingFile stagingFile = null;
		if( stagingUuid != null )
		{
			stagingFile = new StagingFile(stagingUuid);
		}
		doAfterImport(stagingFile, null, entity,
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));

		if( targetList != null )
		{
			aclManager.setTargetList(privilegeNode, entity, targetList);
		}
		if( otherTargetLists != null )
		{
			for( Pair<Object, Node> other : getOtherTargetListObjects(entity) )
			{
				final TargetList list = otherTargetLists.get(other.getFirst());
				aclManager.setTargetList(other.getSecond(), other.getFirst(), list);
			}
		}

		entityDao.flush();

		saveFiles(fakePack, unlock, unlock, false, entity);

		auditLogService.logEntityModified(entity.getId());
		afterStopEdit(fakePack, entity);
		return entity;
	}

	/**
	 * You must override this if you want use editing sessions
	 * 
	 * @param sessionId
	 * @param pack
	 * @return
	 */
	protected <SESSION extends EntityEditingSession<B, T>> SESSION createSession(String sessionId, EntityPack<T> pack,
		B bean)
	{
		throw new Error("You must override createSession on your entity service");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <SESSION extends EntityEditingSession<B, T>> SESSION loadSession(String sessionId)
	{
		SESSION session = (SESSION) sessionService.getAttribute(sessionId);
		if( session == null )
		{
			throw new WizardTimeoutException("No session");
		}
		return session;
	}

	@Override
	public void saveSession(EntityEditingSession<B, T> session)
	{
		sessionService.setAttribute(session.getSessionId(), session);
	}

	@Override
	public void commitSessionId(String sessionId)
	{
		commitSession(loadSession(sessionId));
	}

	@Override
	public void commitSession(EntityEditingSession<B, T> session)
	{
		final EntityPack<T> pack = session.getPack();
		if( session.isNew() )
		{
			doAdd(pack, session, false);
		}
		else
		{
			doStopEdit(pack, session, true);
		}
		sessionService.removeAttribute(session.getSessionId());
	}

	@Override
	public void cancelSessionId(String sessionId)
	{
		cancelSession(loadSession(sessionId));
	}

	@Override
	public void cancelSession(EntityEditingSession<B, T> session)
	{
		T ent = session.getEntity();
		if( ent.getId() != 0 )
		{
			cancelEdit(ent.getId(), true);
		}
		sessionService.removeAttribute(session.getSessionId());
	}

	// shouldn't this have a priv on it?
	@SuppressWarnings("unchecked")
	@Override
	public <SESSION extends EntityEditingSession<B, T>> SESSION startEditingSession(String entityUuid)
	{
		final T entity = getByUuid(entityUuid);
		final EntityPack<T> pack = startEdit(entity);

		B bean = null;
		if( isUseEditingBean() )
		{
			bean = createEditingBean();
			populateEditingBean(bean, entity);
		}

		// It's going in the session - we need to initialise all the
		// hibernation-isms otherwise we get, for example, lazy persistent bags
		// that can't be loaded in later requests.
		initialiserService.initialise(pack);

		final EntityEditingSession<B, T> session = createSession(UUID.randomUUID().toString(), pack, bean);

		// mergeEntityIntoSession(session, pack.getEntity());
		// loadLinkedData(session);

		sessionService.setAttribute(session.getSessionId(), session);
		return (SESSION) session;
	}

	protected boolean isUseEditingBean()
	{
		return false;
	}

	protected B createEditingBean()
	{
		throw new Error("Create a subclass EntityBean and return it");
	}

	protected void populateEditingBean(B bean, T entity)
	{
		bean.setAttributes(entity.getAttributes());
		bean.setDateCreated(entity.getDateCreated());
		bean.setDateModified(entity.getDateModified());
		bean.setDescription(LangUtils.convertBundleToBean(entity.getDescription()));
		bean.setId(entity.getId());
		bean.setInstitutionId(entity.getInstitution().getDatabaseId());
		bean.setName(LangUtils.convertBundleToBean(entity.getName()));
		bean.setOwner(entity.getOwner());
		bean.setSystemType(entity.isSystemType());
		bean.setUuid(entity.getUuid());
		bean.setEnabled(!entity.isDisabled());
	}

	protected void populateEntity(B bean, T entity)
	{
		entity.setAttributes(bean.getAttributes());
		entity.setDateModified(bean.getDateModified());
		entity.setDescription(LangUtils.convertBeanToBundle(bean.getDescription()));
		entity.setName(LangUtils.convertBeanToBundle(bean.getName()));
		entity.setDisabled(!bean.isEnabled());

		// Won't have changed
		entity.setId(bean.getId());
		entity.setDateCreated(bean.getDateCreated());
		entity.setOwner(bean.getOwner());
		entity.setSystemType(bean.isSystemType());
		entity.setUuid(bean.getUuid());
	}

	// shouldn't this have a priv on it?
	@Override
	@SuppressWarnings("unchecked")
	public <SESSION extends EntityEditingSession<B, T>> SESSION startNewSession(T entity)
	{
		entity.setInstitution(CurrentInstitution.get());
		entity.setUuid(UUID.randomUUID().toString());
		entity.setOwner(CurrentUser.getUserID());

		B bean = null;
		if( isUseEditingBean() )
		{
			bean = createEditingBean();
			populateEditingBean(bean, entity);
		}

		final EntityPack<T> pack = new EntityPack<T>();
		pack.setEntity(entity);

		final EntityEditingSession<B, T> session = createSession(UUID.randomUUID().toString(), pack, bean);
		onStartNewSession(session, new EntityFile(entity));
		sessionService.setAttribute(session.getSessionId(), session);

		return (SESSION) session;
	}

	/**
	 * Sets up a staging area. Override this method if you don't want one, or
	 * you want to do more
	 * 
	 * @param session
	 */
	protected void onStartNewSession(EntityEditingSession<B, T> session, EntityFile entFile)
	{
		// setup a staging area
		final EntityPack<T> pack = session.getPack();
		final StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(entFile) )
		{
			fileSystemService.copy(entFile, staging);
		}
		pack.setStagingID(staging.getUuid());
		session.setStagingId(staging.getUuid());
	}

	@Override
	public void deleteBundleObject(Object obj)
	{
		entityDao.deleteAny(obj);
	}

	protected void publishEvent(ApplicationEvent<?> event)
	{
		eventService.publishApplicationEvent(event);
	}

	@Override
	public boolean canCreate()
	{
		return !aclManager.filterNonGrantedPrivileges("CREATE_" + privilegeNode.toString()).isEmpty();
	}

	@Override
	public boolean canEdit(BaseEntityLabel entity)
	{
		return canEdit((Object) entity);
	}

	@Override
	public boolean canEdit(T entity)
	{
		return canEdit((Object) entity);
	}

	private boolean canEdit(Object entity)
	{
		Set<String> privs = new HashSet<String>();
		privs.add("EDIT_" + privilegeNode.toString());
		return !aclManager.filterNonGrantedPrivileges(entity, privs).isEmpty();
	}

	@Override
	public boolean canDelete(BaseEntityLabel entity)
	{
		return canDelete((Object) entity);
	}

	@Override
	public boolean canDelete(T entity)
	{
		return canDelete((Object) entity);
	}

	@Override
	public boolean canView(T entity)
	{
		return canView((Object) entity);
	}

	@Override
	public boolean canList()
	{
		return !aclManager.filterNonGrantedPrivileges("LIST_" + privilegeNode.toString()).isEmpty();
	}

	private boolean canDelete(Object entity)
	{
		Set<String> privs = new HashSet<String>();
		privs.add("DELETE_" + privilegeNode.toString());
		return !aclManager.filterNonGrantedPrivileges(entity, privs).isEmpty();
	}

	private boolean canView(Object entity)
	{
		Set<String> privs = new HashSet<String>();
		privs.add("VIEW_" + privilegeNode.toString());
		return !aclManager.filterNonGrantedPrivileges(entity, privs).isEmpty();
	}

	public String getPrivilegeType()
	{
		return privilegeType;
	}

	protected void publishEventAfterCommit(final ApplicationEvent<?> event)
	{
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				eventService.publishApplicationEvent(event);
			}
		});
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		entityDao.removeOrphanedOwners(event.getUserID());
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		entityDao.changeOwnerId(event.getFromUserId(), event.getToUserId());
	}
}
