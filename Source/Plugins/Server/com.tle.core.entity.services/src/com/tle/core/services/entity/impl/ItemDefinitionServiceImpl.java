package com.tle.core.services.entity.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.ejb.helpers.ValidationHelper;
import com.dytech.edge.exceptions.NotFoundException;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.Format;
import com.tle.common.Pair;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.TargetList;
import com.tle.core.dao.ItemDefinitionDao;
import com.tle.core.events.ItemDefinitionDeletionEvent;
import com.tle.core.events.ItemOperationBatchEvent;
import com.tle.core.events.ItemOperationEvent;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.util.archive.ArchiveType;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.filters.FilterFactory;

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
		ItemDefinitionService
{
	private static final String[] BLANKS = {"name"}; //$NON-NLS-1$
	private static final String CONTROL_XML = "_control.xml"; //$NON-NLS-1$

	private final ItemDefinitionDao itemDefinitionDao;

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
				results.add(new Pair<Object, Node>(new ItemMetadataTarget(rule.getId(), itemDefinition),
					Node.ITEM_METADATA));
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
		processDRM(newItemDef);

		boolean fireEvent = false;
		final ItemOperationBatchEvent batchEvent = new ItemOperationBatchEvent();

		// Check if the workflow has changed
		if( !Objects.equals(oldItemDef.getWorkflow(), newItemDef.getWorkflow()) )
		{
			fireEvent = true;
			batchEvent.addEvent(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
				"workflowChanged", newItemDef.getId())));
		}

		if( metadataRuleChanged(oldItemDef.getItemMetadataRules(), newItemDef)
			|| dynamicMetadataRuleChanged(oldItemDef.getDynamicMetadataRules(), newItemDef)
			|| searchDisplayNodesChanged(oldItemDef.getSearchDetails(), newItemDef.getSearchDetails()) )
		{
			fireEvent = true;
			batchEvent.addEvent(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
				"refreshCollectionItems", newItemDef.getId())));
		}

		if( fireEvent )
		{
			publishEventAfterCommit(batchEvent);
		}
	}

	private boolean metadataRuleChanged(List<ItemMetadataRule> oldRules, ItemDefinition newItemDef)
	{
		List<ItemMetadataRule> newRules = newItemDef.getItemMetadataRules();
		return !listContentsIdentical(oldRules, newRules, new Comparator<ItemMetadataRule>()
		{
			@Override
			public int compare(ItemMetadataRule o1, ItemMetadataRule o2)
			{
				// Id and Script. Don't care about name changes
				if( !Objects.equals(o1.getId(), o2.getId()) || !Objects.equals(o1.getScript(), o2.getScript()) )
				{
					return 1;
				}

				return 0;
			}
		});
	}

	private boolean dynamicMetadataRuleChanged(List<DynamicMetadataRule> oldRules, ItemDefinition newItemDef)
	{
		List<DynamicMetadataRule> newRules = newItemDef.getDynamicMetadataRules();
		return !listContentsIdentical(oldRules, newRules, new Comparator<DynamicMetadataRule>()
		{
			@Override
			public int compare(DynamicMetadataRule o1, DynamicMetadataRule o2)
			{
				// Id, Path, Type, Privileges. Don't care about name changes
				if( !Objects.equals(o1.getId(), o2.getId()) || !Objects.equals(o1.getPath(), o2.getPath())
					|| !Objects.equals(o1.getType(), o2.getType())
					|| !Check.bothNullOrDeepEqual(o1.getTargetList(), o2.getTargetList()) )
				{
					return 1;
				}

				return 0;
			}
		});
	}

	private boolean searchDisplayNodesChanged(SearchDetails oldDetails, SearchDetails newDetails)
	{
		List<DisplayNode> oldNodes = oldDetails == null ? null : oldDetails.getDisplayNodes();
		List<DisplayNode> newNodes = newDetails == null ? null : newDetails.getDisplayNodes();
		return !listContentsIdentical(oldNodes, newNodes, new Comparator<DisplayNode>()
		{
			@Override
			public int compare(DisplayNode node1, DisplayNode node2)
			{
				return Check.bothNullOrDeepEqual(node1, node2) ? 0 : 1;
			}
		});
	}

	private <T> boolean listContentsIdentical(List<T> oldRules, List<T> newRules, Comparator<T> comparator)
	{
		if( oldRules == null && newRules == null || (oldRules == null && newRules != null && newRules.size() == 0) )
		{
			return true;
		}
		else if( oldRules == null || newRules == null || oldRules.size() != newRules.size() )
		{
			return false;
		}
		else
		{
			Iterator<T> ai = oldRules.iterator();
			Iterator<T> bi = newRules.iterator();

			while( ai.hasNext() )
			{
				if( comparator.compare(ai.next(), bi.next()) != 0 )
				{
					return false;
				}
			}

			return true;
		}
	}

	@Override
	protected void beforeAdd(EntityPack<ItemDefinition> pack, boolean lockAfterwards)
	{
		processDRM(pack.getEntity());
	}

	private void processDRM(ItemDefinition itemdef)
	{
		Set<String> pageIds = Sets.newHashSet();
		Wizard wizard = itemdef.getWizard();
		if( wizard != null )
		{
			List<WizardPage> pages = wizard.getPages();
			for( WizardPage page : pages )
			{
				if( page instanceof DRMPage )
				{
					pageIds.add(((DRMPage) page).getUuid());
				}
			}
			if( !pageIds.isEmpty() )
			{
				publishEventAfterCommit(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(
					FilterFactory.class, "drmUpdate", itemdef.getId(), (Serializable) pageIds)));
			}
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
}
