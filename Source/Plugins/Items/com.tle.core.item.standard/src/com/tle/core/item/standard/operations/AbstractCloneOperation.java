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

package com.tle.core.item.standard.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.CloneFileProcessingExtension;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractCloneOperation extends AbstractStandardWorkflowOperation
	implements
		DuringSaveOperationGenerator,
		MetadataTransformingOperation
{
	public static final String PRE_CLONE_EXTENSION = "preClone";
	public static final String POST_CLONE_EXTENSION = "postClone";

	private InitialiserService initialiserService;
	@Inject
	private StagingService stagingService;

	private PluginTracker<CloneFileProcessingExtension> fileProcessorTracker;
	private List<WorkflowOperation> ranOperations = new ArrayList<WorkflowOperation>();

	protected final boolean copyAttachments;
	protected String transform;
	private Function<CloningHelper, CloningHelper> cloner;

	protected AbstractCloneOperation(boolean copyAttachments)
	{
		this.copyAttachments = copyAttachments;
	}

	/**
	 * @param transform Xslt transform to perform when moving between schemas.
	 *            Optional
	 */
	@Override
	public void setTransform(String transform)
	{
		this.transform = transform;
	}

	@Override
	public Collection<DuringSaveOperation> getDuringSaveOperation()
	{
		List<DuringSaveOperation> output = new ArrayList<DuringSaveOperation>();
		for( WorkflowOperation op : ranOperations )
		{
			if( op instanceof DuringSaveOperationGenerator )
			{
				output.addAll(((DuringSaveOperationGenerator) op).getDuringSaveOperation());
			}
		}
		return output;
	}

	/**
	 * The common execution code, which will farm out to your overridden methods
	 */
	@Override
	public final boolean execute()
	{
		ranOperations.addAll(itemService.executeExtensionOperationsNow(params, PRE_CLONE_EXTENSION));

		ItemFile from = itemFileService.getItemFile(getItem());
		StagingFile staging = stagingService.createStagingArea();
		if( copyAttachments )
		{
			fileSystemService.copy(from, staging);
		}
		ItemPack<Item> pack = getItemPack();
		pack.setStagingID(staging.getUuid());

		params.setUpdate(false);

		Item origItem = pack.getItem();
		Item item = new Item();
		item.setId(0l);
		item.setNewItem(true);
		item.setItemDefinition(origItem.getItemDefinition());
		item.setInstitution(origItem.getInstitution());
		item.setHistory(new ArrayList<HistoryEvent>());
		item.setRating(-1);
		item.setOwner(CurrentUser.getUserID());
		item.setStatus(ItemStatus.DRAFT);
		item.setDateCreated(new Date());
		item.setName(LanguageBundle.clone(origItem.getName()));
		item.setDescription(LanguageBundle.clone(origItem.getDescription()));
		item.setNavigationSettings(origItem.getNavigationSettings());
		item.setThumb(origItem.getThumb());

		item = initItemUuidAndVersion(item, origItem);
		doCloning(origItem, item);

		DrmSettings drm = origItem.getDrmSettings();
		if( drm != null )
		{
			drm = drm.databaseClone();
			item.setDrmSettings(drm);
		}

		pack.setOriginalItem(origItem);
		pack.setItem(item);
		doHistory();
		finalProcessing(origItem, item);

		ranOperations.addAll(itemService.executeExtensionOperationsNow(params, POST_CLONE_EXTENSION));
		return true;
	}

	/**
	 * The actual cloning functionality. The CloningHelper object as returned by
	 * getCloningHelper determines the fields that are being cloned here.
	 * 
	 * @param origItem
	 * @param item
	 */
	protected final void doCloning(Item origItem, Item item)
	{
		CloningHelper forCloning = getCloningHelper();
		if( forCloning != null )
		{
			extractCloneData(origItem, forCloning);
			forCloning = cloner.apply(forCloning);
			initialiserService.initialiseClones(forCloning);
			pushCloneData(item, forCloning);
		}
	}

	/**
	 * Anything you want to perform *after* the actual cloning is done, but
	 * before post-clone operations are run. @
	 */
	protected void finalProcessing(Item origItem, Item item)
	{
		for( CloneFileProcessingExtension fileProcessor : fileProcessorTracker.getBeanList() )
		{
			fileProcessor.processFiles(origItem.getItemId(), itemFileService.getItemFile(origItem), item, getStaging());
		}
	}

	/**
	 * To override in any subclass
	 * 
	 * @param newItem
	 * @param oldItem
	 * @return @
	 */
	protected Item initItemUuidAndVersion(Item newItem, Item oldItem)
	{
		newItem.setUuid(UUID.randomUUID().toString());
		newItem.setVersion(1);
		return newItem;
	}

	/**
	 * This method determines the Item fields you want to clone, based on the
	 * CloningHelper object and the code inside extractCloneData and
	 * pushCloneData. To override in any subclass
	 * 
	 * @return
	 */
	protected CloningHelper getCloningHelper()
	{
		return new CloningHelper();
	}

	/**
	 * Here you will grab the item fields you require and put them into your
	 * CloningHelper object. To override in any subclass. Do not call this super
	 * method if not cloning attachments
	 * 
	 * @param originalItem
	 * @param cloning The cloning helper returned from getCloningHelper()
	 */
	protected void extractCloneData(Item originalItem, CloningHelper cloning)
	{
		if( copyAttachments )
		{
			cloning.setAttachments(new ArrayList<Attachment>(originalItem.getAttachments()));
			cloning.setNodes(new ArrayList<ItemNavigationNode>(originalItem.getTreeNodes()));
		}
	}

	/**
	 * Here you will grab the fields back out of your CloningHelper object and
	 * push them into the new item. To override in any subclass. Do not call
	 * this super method if not cloning attachments
	 * 
	 * @param newItem
	 * @param cloning
	 */
	protected void pushCloneData(Item newItem, CloningHelper cloning)
	{
		if( copyAttachments )
		{
			newItem.setAttachments(cloning.getAttachments());
			newItem.setTreeNodes(cloning.getNodes());
		}
	}

	/**
	 * You must put something in this method Eg.
	 * createHistory(HistoryEvent.Type.clone); The exact event will be dependant
	 * on the operation
	 */
	protected abstract void doHistory();

	/**
	 * The CloningHelper class should be extended by your operation if you want
	 * to clone additional Item fields. Return a new instance of your extended
	 * CloningHelper object in getCloningHelper()
	 */
	public static class CloningHelper
	{
		private List<Attachment> attachments;
		private List<ItemNavigationNode> nodes;

		public List<Attachment> getAttachments()
		{
			return attachments;
		}

		public void setAttachments(List<Attachment> attachments)
		{
			this.attachments = attachments;
		}

		public List<ItemNavigationNode> getNodes()
		{
			return nodes;
		}

		public void setNodes(List<ItemNavigationNode> nodes)
		{
			this.nodes = nodes;
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		fileProcessorTracker = new PluginTracker<CloneFileProcessingExtension>(pluginService,
			"com.tle.core.item.standard", "cloneFileProcessor", "id");
		fileProcessorTracker.setBeanKey("bean");
	}

	@Inject
	public void setInitialiserService(InitialiserService initialiserService) {
		this.initialiserService = initialiserService;
		this.cloner = initialiserService.createCloner(getClass().getClassLoader());
	}
}
