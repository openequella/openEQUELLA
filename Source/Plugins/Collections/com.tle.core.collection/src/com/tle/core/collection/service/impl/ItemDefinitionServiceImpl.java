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

package com.tle.core.collection.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.EntityPack;
import com.tle.common.Format;
import com.tle.common.Pair;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.core.collection.dao.ItemDefinitionDao;
import com.tle.core.collection.event.ItemDefinitionDeletionEvent;
import com.tle.core.collection.extension.CollectionSaveExtension;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.schema.SchemaReferences;
import com.tle.core.schema.event.listener.SchemaReferencesListener;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.ValidationHelper;
import com.tle.core.util.archive.ArchiveType;

/**
 * @author Nicholas Read
 */
@Bind(ItemDefinitionService.class)
@Singleton
@SecureEntity(RemoteItemDefinitionService.ENTITY_TYPE)
@SuppressWarnings("nls")
public class ItemDefinitionServiceImpl
	extends
		AbstractEntityServiceImpl<EntityEditingBean, ItemDefinition, ItemDefinitionService>
	implements
		ItemDefinitionService,
		SchemaReferences,
		SchemaReferencesListener
{
	private static final String[] BLANKS = {"name"}; //$NON-NLS-1$
	private static final String CONTROL_XML = "_control.xml"; //$NON-NLS-1$

	private final ItemDefinitionDao itemDefinitionDao;

	@Inject
	private PluginTracker<CollectionSaveExtension> saveExtensions;

	@Inject
	public ItemDefinitionServiceImpl(ItemDefinitionDao itemDefinitionDao)
	{
		super(Node.COLLECTION, itemDefinitionDao);
		this.itemDefinitionDao = itemDefinitionDao;
	}

	@Override
	protected Collection<Pair<Object, Node>> getOtherTargetListObjects(ItemDefinition itemDefinition)
	{
		Collection<Pair<Object, Node>> results = new ArrayList<Pair<Object, Node>>();

		for( ItemStatus status : ItemStatus.values() )
		{
			results.add(new Pair<Object, Node>(new ItemStatusTarget(status, itemDefinition), Node.ITEM_STATUS));
		}

		if( itemDefinition.getItemMetadataRules() != null )
		{
			for( ItemMetadataRule rule : itemDefinition.getItemMetadataRules() )
			{
				results.add(
					new Pair<Object, Node>(new ItemMetadataTarget(rule.getId(), itemDefinition), Node.ITEM_METADATA));
			}
		}

		return results;
	}

	@Override
	protected void saveTargetLists(EntityEditingSession<EntityEditingBean, ItemDefinition> session,
		EntityPack<ItemDefinition> pack)
	{
		// Ensure that we start with no old ACLs
		getAclManager().deleteAllEntityChildren(Node.ITEM_METADATA, pack.getEntity().getId());

		Map<Object, TargetList> otherTargetLists = pack.getOtherTargetLists();
		if( otherTargetLists != null )
		{
			for( Object obj : otherTargetLists.keySet() )
			{
				if( obj instanceof ItemMetadataTarget )
				{
					ItemMetadataTarget imt = (ItemMetadataTarget) obj;
					imt.setItemDefinition(pack.getEntity());
				}
				else if( obj instanceof ItemStatusTarget )
				{
					ItemStatusTarget ist = (ItemStatusTarget) obj;
					ist.setItemDefinition(pack.getEntity());
				}
				else
				{
					throw new IllegalStateException("Type '" + obj.getClass().getName() + "' is not being handled");
				}
			}
		}

		super.saveTargetLists(session, pack);
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> referencingClasses = itemDefinitionDao.getReferencingClasses(id);
		super.getReferencingClasses(id);
		return referencingClasses;
	}

	@Override
	protected void deleteReferences(ItemDefinition entity)
	{
		publishEvent(new ItemDefinitionDeletionEvent(entity));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.ItemDefinitionService#enumerateCategories(com.dytech
	 * .edge.user.UserState)
	 */
	@Override
	public Set<String> enumerateCategories()
	{
		Criterion crit = getInstitutionCriterion();

		Set<String> results = new TreeSet<String>(Format.STRING_COMPARATOR);
		for( ItemDefinition itemdef : getDao().findAllByCriteria(crit) )
		{
			String category = itemdef.getWizardcategory();
			if( category != null )
			{
				results.add(category);
			}
		}

		return results;
	}

	@Override
	public List<ItemDefinition> enumerateForType(String type)
	{
		return itemDefinitionDao.findByType(type);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.entity.ItemDefinitionService#listAllForSchema(java
	 * .lang.String)
	 */
	@Override
	public List<BaseEntityLabel> listAllForSchema(long schemaID)
	{
		return itemDefinitionDao.listAllForSchema(schemaID);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.services.entity.ItemDefinitionService#
	 * listUsableItemDefinitionsForSchema(java.lang.String)
	 */
	@Override
	public List<BaseEntityLabel> listUsableItemDefinitionsForSchema(long schemaID)
	{
		return listAllForSchema(schemaID);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.ItemDefinitionService#enumerateForWorkflow(com.
	 * dytech.edge.user.UserState, long)
	 */
	@Override
	public List<ItemDefinition> enumerateForWorkflow(long workflowID)
	{
		return findAllWithCriterion(Restrictions.eq("workflow.id", workflowID));
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public List<ItemDefinition> enumerateWithWorkflow()
	{
		return findAllWithCriterion(Restrictions.isNotNull("workflow.id"));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.ItemDefinitionService#getByIdentifier(java.lang
	 * .String)
	 */
	public ItemDefinition getByIdentifier(String identifier)
	{
		return getDao().findByCriteria(Restrictions.eq("identifier", identifier),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	public long getSchemaIdForCollectionUuid(String uuid)
	{
		return getDao()
			.findByCriteria(Restrictions.eq("uuid", uuid), Restrictions.eq("institution", CurrentInstitution.get()))
			.getSchema().getId();
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, ItemDefinition> session, ItemDefinition entity,
		List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, BLANKS, errors);
	}

	@Override
	protected ComparisonEntityInitialiser<ItemDefinition> getComparisonInitialiserForStopEdit()
	{
		return new ComparisonEntityInitialiser<ItemDefinition>()
		{
			@Override
			public void preUnlink(ItemDefinition t)
			{
				// We suck the big one for misusing hibernate.
				initBundle(t.getName());
				initBundle(t.getDescription());
				t.getItemMetadataRules();
				Hibernate.initialize(t.getWorkflow());
			}
		};
	}

	@Override
	protected void beforeStopEdit(EntityPack<ItemDefinition> pack, ItemDefinition oldItemDef, boolean unlock)
	{
		// Ensure we have initialised these - may not required, but can't hurt.
		oldItemDef.getWorkflow();
		oldItemDef.getItemMetadataRules();
		oldItemDef.getSearchDetails();

		// We need to do this, otherwise new copies are created in the database
		// without removing the old ones first
		pack.getEntity().getSlow().setId(oldItemDef.getSlow().getId());
	}

	@Override
	protected void afterStopEdit(EntityPack<ItemDefinition> pack, ItemDefinition oldItemDef)
	{
		final ItemDefinition newItemDef = pack.getEntity();
		for( CollectionSaveExtension ext : saveExtensions.getBeanList() )
		{
			ext.collectionSaved(oldItemDef, newItemDef);
		}
	}

	@Override
	protected void beforeAdd(EntityPack<ItemDefinition> pack, boolean lockAfterwards)
	{
		ItemDefinition collection = pack.getEntity();
		if( fileSystemService.isAdvancedFilestore() )
		{
			collection.setAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS, true);
		}
		for( CollectionSaveExtension ext : saveExtensions.getBeanList() )
		{
			ext.collectionSaved(null, collection);
		}
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public List<ItemDefinition> enumerateSearchable()
	{
		return getDao().enumerateAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.CREATE_ITEM)
	public List<ItemDefinition> enumerateCreateable()
	{
		return getDao().enumerateAll();
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.CREATE_ITEM)
	public List<BaseEntityLabel> listCreateable()
	{
		return listAll();
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public List<BaseEntityLabel> listSearchable()
	{
		return listAll();
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public Collection<ItemDefinition> filterSearchable(Collection<ItemDefinition> collections)
	{
		return collections;
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public List<ItemDefinition> getMatchingSearchable(Collection<Long> itemdefs)
	{
		return itemDefinitionDao.getByIds(itemdefs);
	}

	@Override
	@SecureOnReturn(priv = "SEARCH_COLLECTION")
	public List<ItemDefinition> getMatchingSearchableUuid(Collection<String> itemdefs)
	{
		return itemDefinitionDao.getByUuids(itemdefs);
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.CREATE_ITEM)
	public List<ItemDefinition> getMatchingCreatableUuid(Collection<String> itemdefs)
	{
		return itemDefinitionDao.getByUuids(itemdefs);
	}

	@Override
	protected void preUnlinkForClone(ItemDefinition definition)
	{
		Hibernate.initialize(definition.getSlow());
	}

	@Override
	protected void processClone(EntityPack<ItemDefinition> pack)
	{
		pack.getEntity().getSlow().setId(0);
	}

	@Override
	public byte[] exportControl(String controlXml)
	{
		StagingFile file = stagingService.createStagingArea();
		try
		{
			fileSystemService.write(file, CONTROL_XML, new StringReader(controlXml), false);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			fileSystemService.zipFile(file, out, ArchiveType.ZIP);
			return out.toByteArray();
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
		finally
		{
			stagingService.removeStagingArea(file, true);
		}
	}

	@Override
	public String importControl(byte[] zipFileData) throws IOException
	{
		StagingFile staging = stagingService.createStagingArea();
		try
		{
			fileSystemService.unzipFile(staging, new ByteArrayInputStream(zipFileData), ArchiveType.ZIP);
			Reader reader = new InputStreamReader(fileSystemService.read(staging, CONTROL_XML));
			StringWriter writer = new StringWriter();
			CharStreams.copy(reader, writer);
			return writer.toString();
		}
		catch( IOException io )
		{
			Throwables.propagate(io);
			return null; // unreachable
		}
		finally
		{
			stagingService.removeStagingArea(staging, true);
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	@SecureOnReturn(priv = "CREATE_ITEM")
	@Override
	public ItemDefinition getForItemCreate(String uuid)
	{
		ItemDefinition collection = getByUuid(uuid);
		if( collection == null )
		{
			throw new NotFoundException("Collection '" + uuid + "' doesn't exist");
		}
		return collection;
	}

	@Transactional
	@Override
	public ItemDefinition getByItemIdUnsecure(ItemKey itemId)
	{
		ItemDefinition collection = itemDefinitionDao.getByItemId(itemId);
		if( collection == null )
		{
			throw new NotFoundException("No collection found for item ID " + itemId.toString());
		}
		return collection;
	}

	@Override
	public List<BaseEntityLabel> getSchemaUses(long id)
	{
		return listAllForSchema(id);
	}

	@Override
	public void addSchemaReferencingClasses(Schema schema, List<Class<?>> referencingClasses)
	{
		if( itemDefinitionDao.listAllForSchema(schema.getId()).size() > 0 )
		{
			referencingClasses.add(ItemDefinition.class);
		}
	}
}
