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

package com.tle.web.api.collection.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.DynamicMetadataRule;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.ItemMetadataRule;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.ItemStatus;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.remoting.RemoteFileSystemService;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.security.ItemMetadataTarget;
import com.tle.common.security.ItemStatusTarget;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.workflow.Workflow;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.event.ItemOperationBatchEvent;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.operations.BaseFilter;
import com.tle.core.item.standard.FilterFactory;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.collection.CollectionEditor;
import com.tle.web.api.collection.interfaces.beans.CollectionBean;
import com.tle.web.api.collection.interfaces.beans.CollectionSecurityBean;
import com.tle.web.api.collection.interfaces.beans.ItemMetadataSecurityBean;
import com.tle.web.api.interfaces.beans.security.DynamicRuleBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CollectionEditorImpl extends AbstractBaseEntityEditor<ItemDefinition, CollectionBean>
	implements
		CollectionEditor
{
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private FileSystemService fileSystemService;

	@Nullable
	private Collection<String> drmPageIds;
	private boolean workflowChanged;
	private boolean itemMetadataRulesChanged;
	private boolean dynamicMetadataRuleChanged;
	private boolean searchDisplayNodesChanged;

	@AssistedInject
	public CollectionEditorImpl(@Assisted ItemDefinition collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
		@Assisted("editing") boolean editing, @Assisted("importing") boolean importing)
	{
		super(collection, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public CollectionEditorImpl(@Assisted ItemDefinition collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing)
	{
		this(collection, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(CollectionBean bean)
	{
		super.copyCustomFields(bean);

		final ItemDefinition collection = entity;
		BaseEntityReference schemaBean = bean.getSchema();
		if( schemaBean != null )
		{
			Schema schema = schemaService.getByUuid(schemaBean.getUuid());
			collection.setSchema(schema);
		}

		final Workflow oldWorkflow = collection.getWorkflow();
		final Workflow newWorkflow;
		final BaseEntityReference workflowBean = bean.getWorkflow();
		if( workflowBean != null )
		{
			newWorkflow = workflowService.getByUuid(workflowBean.getUuid());
		}
		else
		{
			newWorkflow = null;
		}
		collection.setWorkflow(newWorkflow);
		workflowChanged = !Objects.equals(oldWorkflow, newWorkflow);

		// FIXME: need to implement
		itemMetadataRulesChanged = false;
		dynamicMetadataRuleChanged = false;
		searchDisplayNodesChanged = false;

		// FIXME: should come from the bean
		SummaryDisplayTemplate itemSummaryDisplayTemplate = new SummaryDisplayTemplate();
		itemSummaryDisplayTemplate.setConfigList(SummarySectionsConfig.createDefaultConfigs());
		entity.setItemSummaryDisplayTemplate(itemSummaryDisplayTemplate);

		if( fileSystemService.isAdvancedFilestore() )
		{
			collection.setAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_BUCKETS, true);
		}
		final String filestoreId = bean.getFilestoreId();
		if( !Strings.isNullOrEmpty(filestoreId) )
		{
			if( RemoteFileSystemService.DEFAULT_FILESTORE_ID.equals(filestoreId) )
			{
				collection.removeAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE);
			}
			else
			{
				collection.setAttribute(RemoteItemDefinitionService.ATTRIBUTE_KEY_FILESTORE, filestoreId);
			}
		}

		processDRM(collection);
	}

	@Override
	protected Map<Object, TargetList> getOtherTargetLists(CollectionBean bean)
	{
		final CollectionSecurityBean security = bean.getSecurity();
		if( security != null )
		{
			final Map<Object, TargetList> otherTargetLists = new HashMap<>();

			final Map<String, List<TargetListEntryBean>> statuses = security.getStatuses();
			if( statuses != null )
			{
				for( Entry<String, List<TargetListEntryBean>> status : statuses.entrySet() )
				{
					final String statusName = status.getKey();
					final ItemStatus statusEnum;
					try
					{
						statusEnum = ItemStatus.valueOf(statusName.toUpperCase());
					}
					catch( IllegalArgumentException ill )
					{
						throw new InvalidDataException(
							new ValidationError("security.statuses." + statusName, "Unrecognised status"));
					}
					final ItemStatusTarget target = new ItemStatusTarget(statusEnum, entity);

					final TargetList tl = new TargetList();
					final List<TargetListEntry> tles = new ArrayList<>();
					for( TargetListEntryBean rule : status.getValue() )
					{
						final TargetListEntry tle = new TargetListEntry(rule.isGranted(), rule.isOverride(),
							rule.getPrivilege(), rule.getWho());
						tles.add(tle);
					}
					tl.setEntries(tles);
					otherTargetLists.put(target, tl);
				}
			}

			final List<ItemMetadataRule> itemMetadataRules = new ArrayList<>();

			final Map<String, ItemMetadataSecurityBean> metadata = security.getMetadata();
			if( metadata != null )
			{
				for( Entry<String, ItemMetadataSecurityBean> meta : metadata.entrySet() )
				{
					final String id = meta.getKey();
					final ItemMetadataSecurityBean imrBean = meta.getValue();

					final ItemMetadataRule imr = new ItemMetadataRule();
					imr.setId(id);
					imr.setName(imrBean.getName());
					imr.setScript(imrBean.getScript());
					itemMetadataRules.add(imr);

					final ItemMetadataTarget target = new ItemMetadataTarget(id, entity);
					final TargetList tl = new TargetList();
					final List<TargetListEntry> tles = new ArrayList<>();
					for( TargetListEntryBean rule : imrBean.getEntries() )
					{
						final TargetListEntry tle = new TargetListEntry(rule.isGranted(), rule.isOverride(),
							rule.getPrivilege(), rule.getWho());
						tles.add(tle);
					}
					tl.setEntries(tles);
					otherTargetLists.put(target, tl);
				}
			}
			entity.setItemMetadataRules(itemMetadataRules);

			final List<DynamicMetadataRule> dynamicMetadataRules = new ArrayList<>();

			final List<DynamicRuleBean> dynamic = security.getDynamicRules();
			if( dynamic != null )
			{
				for( DynamicRuleBean dynamicRuleBean : dynamic )
				{
					final DynamicMetadataRule dmr = new DynamicMetadataRule();
					dmr.setId(UUID.randomUUID().toString());
					dmr.setName(dynamicRuleBean.getName());
					dmr.setPath(dynamicRuleBean.getPath());
					dmr.setType(dynamicRuleBean.getType());

					final TargetList tl = new TargetList();
					final List<TargetListEntry> tles = new ArrayList<>();
					for( TargetListEntryBean rule : dynamicRuleBean.getTargetList() )
					{
						final TargetListEntry tle = new TargetListEntry(rule.isGranted(), rule.isOverride(),
							rule.getPrivilege(), rule.getWho());
						tles.add(tle);
					}
					tl.setEntries(tles);

					dmr.setTargetList(tl);
				}
			}
			entity.setDynamicMetadataRules(dynamicMetadataRules);

			return otherTargetLists;
		}
		return null;
	}

	@Override
	protected void afterFinishedEditing()
	{
		super.afterFinishedEditing();

		//FIXME:  THIS IS PRETTY MUCH A COPY AND PASTE OF ItemStandardServiceImpl.collectionSaved

		if( editing && (drmPageIds != null && !drmPageIds.isEmpty()) )
		{
			publishEventAfterCommit(new ItemOperationEvent(new FactoryMethodLocator<BaseFilter>(FilterFactory.class,
				"drmUpdate", entity.getId(), (Serializable) drmPageIds)));
		}

		boolean fireEvent = false;
		final ItemOperationBatchEvent batchEvent = new ItemOperationBatchEvent();

		// Check if the workflow has changed
		if( editing && workflowChanged )
		{
			fireEvent = true;
			batchEvent.addEvent(new ItemOperationEvent(
				new FactoryMethodLocator<BaseFilter>(FilterFactory.class, "workflowChanged", entity.getId())));
		}

		if( editing && (itemMetadataRulesChanged || dynamicMetadataRuleChanged || searchDisplayNodesChanged) )
		{
			fireEvent = true;
			batchEvent.addEvent(new ItemOperationEvent(
				new FactoryMethodLocator<BaseFilter>(FilterFactory.class, "refreshCollectionItems", entity.getId())));
		}

		if( fireEvent )
		{
			publishEventAfterCommit(batchEvent);
		}
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
				drmPageIds = pageIds;
			}
		}
	}

	@Override
	protected AbstractEntityService<?, ItemDefinition> getEntityService()
	{
		return collectionService;
	}

	@BindFactory
	public interface CollectionEditorFactory
	{
		CollectionEditorImpl createExistingEditor(@Assisted ItemDefinition collection,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		CollectionEditorImpl createNewEditor(ItemDefinition collection,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
