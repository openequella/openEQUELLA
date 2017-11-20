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

package com.tle.web.wizard;

import java.io.ObjectStreamException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.DRMPage;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ViewableItemType;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.workflow.SecurityStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.standard.operations.DuringSaveOperation;
import com.tle.web.viewable.PreviewableItem;
import com.tle.web.wizard.impl.UnsavedEditOperation;
import com.tle.web.wizard.page.WebWizardPageState;
import com.tle.web.wizard.section.model.DuplicateData;

public class WizardState implements WizardStateInterface
{
	private static final long serialVersionUID = 1;

	@Inject
	private static ItemFileService itemFileService;

	public enum Operation
	{
		CREATING, EDITING, MOVING, CLONING
	}

	private Item item;
	private ItemKey itemKey;

	private String xml;
	private String stagingId;
	private String saveMessage;
	private String referrer;
	private String wizid;
	private String thumbnail;

	private boolean showTaskStatus;
	private boolean showReview;
	private boolean showUrls;

	private boolean editable = true;
	private boolean redraftAfterSave = false;
	private boolean lockedForEditing = false;
	private boolean mergeDRMDefaults = false;
	private boolean entryThroughEdit = false;
	private boolean noCancel = false;
	private boolean newItem;
	private boolean movedItem; // changed collection
	private boolean deleted = false;

	private final Operation operation;
	// Can't be final due to clone
	private/* final */List<DRMPage> drm = new ArrayList<DRMPage>();
	private/* final */Map<String, DuplicateData> duplicateData = new HashMap<String, DuplicateData>();
	private/* final */List<UnsavedEditOperation> unsavedEdits = new ArrayList<UnsavedEditOperation>();
	private/* final */Map<String, DuringSaveOperation> saveOperations = new HashMap<String, DuringSaveOperation>();

	private WizardMetadataMapper wizardMetadataMapper = new WizardMetadataMapper();
	private List<WebWizardPageState> pageStates;
	private QuotaExceededException quotaExceededException;

	private transient volatile PropBagEx xmlBag;
	private transient Deque<Pair<String, Integer>> pathOverrides;
	private transient List<WebWizardPage> pages;
	private transient WorkflowStatus workflowStatus;
	private BiMap<UUID, String> registeredFilenames = HashBiMap.create();

	public WizardState(Operation operation)
	{
		wizid = UUID.randomUUID().toString();
		this.operation = operation;
	}

	public void setReferrer(String refer)
	{
		referrer = refer;
	}

	public String getReferrer()
	{
		return referrer;
	}

	@Override
	public ModifiableAttachments getAttachments()
	{
		return new ModifiableAttachments(getItem());
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}

	public List<DRMPage> getDrm()
	{
		return drm;
	}

	public Map<String, DuplicateData> getDuplicateData()
	{
		return duplicateData;
	}

	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public ItemDefinition getItemDefinition()
	{
		return getItem().getItemDefinition();
	}

	@Override
	public Item getItem()
	{
		return item;
	}

	@Override
	public PropBagEx getItemxml()
	{
		if( xmlBag == null )
		{
			xmlBag = new PropBagEx(xml);
		}
		return xmlBag;
	}

	public void update(ItemPack pack, WorkflowStatus status)
	{
		throw new UnsupportedOperationException();
	}

	public boolean isLockedForEditing()
	{
		return lockedForEditing;
	}

	public void setLockedForEditing(boolean locked)
	{
		this.lockedForEditing = locked;
	}

	public boolean isMergeDRMDefaults()
	{
		return mergeDRMDefaults;
	}

	/**
	 * This isn't used anywhere...?
	 * 
	 * @param mergeDRMDefaults
	 */
	public void setMergeDRMDefaults(boolean mergeDRMDefaults)
	{
		this.mergeDRMDefaults = mergeDRMDefaults;
	}

	public boolean isEntryThroughEdit()
	{
		return entryThroughEdit;
	}

	public void setEntryThroughEdit(boolean entryThroughEdit)
	{
		this.entryThroughEdit = entryThroughEdit;
	}

	public boolean isNewItem()
	{
		return newItem;
	}

	public void setNewItem(boolean newItem)
	{
		this.newItem = newItem;
	}

	public boolean isMovedItem()
	{
		return movedItem;
	}

	public void setMovedItem(boolean movedItem)
	{
		this.movedItem = movedItem;
	}

	public String getSaveMessage()
	{
		return saveMessage;
	}

	public void setSaveMessage(String saveMessage)
	{
		this.saveMessage = saveMessage;
	}

	public Schema getSchema()
	{
		return getItem().getItemDefinition().getSchema();
	}

	@Override
	public String getStagingId()
	{
		return stagingId;
	}

	public Wizard getWizard()
	{
		return getItem().getItemDefinition().getWizard();
	}

	@Override
	public String getWizid()
	{
		return wizid;
	}

	public void setWizid(String wizid)
	{
		this.wizid = wizid;
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return workflowStatus;
	}

	public void setWorkflowStatus(WorkflowStatus workflowStatus)
	{
		this.workflowStatus = workflowStatus;
	}

	public List<WebWizardPage> getPages()
	{
		return pages;
	}

	public void setPages(List<WebWizardPage> pages)
	{
		this.pages = pages;
	}

	public boolean isShowReview()
	{
		return showReview;
	}

	public void setShowReview(boolean showReview)
	{
		this.showReview = showReview;
	}

	public boolean isShowTaskStatus()
	{
		return showTaskStatus;
	}

	public void setShowTaskStatus(boolean showTaskStatus)
	{
		this.showTaskStatus = showTaskStatus;
	}

	public boolean isShowUrls()
	{
		return showUrls;
	}

	public void setShowUrls(boolean showUrls)
	{
		this.showUrls = showUrls;
	}

	@Override
	public ItemKey getItemId()
	{
		return itemKey;
	}

	public void setItemId(ItemKey itemId)
	{
		this.itemKey = itemId;
	}

	public String getTaskId()
	{
		ItemKey itemId = getItemId();
		if( itemId instanceof ItemTaskId )
		{
			return ((ItemTaskId) itemId).getTaskId();
		}
		return null;
	}

	public boolean isRedraftAfterSave()
	{
		return redraftAfterSave;
	}

	public void setRedraftAfterSave(boolean redraftAfterSave)
	{
		this.redraftAfterSave = redraftAfterSave;
	}

	@Override
	public FileHandle getFileHandle()
	{
		String staging = getStagingId();
		if( !Check.isEmpty(staging) )
		{
			return new StagingFile(staging);
		}
		return itemFileService.getItemFile(getItem());
	}

	public String getOriginalUrl()
	{
		return null;
	}

	public Set<String> getPrivileges()
	{
		return workflowStatus.getSecurityStatus().getAllowedPrivileges();
	}

	public ItemStatus getItemStatus()
	{
		return getItem().getStatus();
	}

	public List<WebWizardPageState> getPageStates()
	{
		return pageStates;
	}

	public void setPageStates(List<WebWizardPageState> pageStates)
	{
		this.pageStates = pageStates;
	}

	public SecurityStatus getSecurityStatus()
	{
		return workflowStatus.getSecurityStatus();
	}

	public ViewableItemType getItemType()
	{
		return ViewableItemType.ITEMS;
	}

	public boolean isInDraft()
	{
		return isRedraftAfterSave() || getItem().getStatus().equals(ItemStatus.DRAFT);
	}

	public QuotaExceededException getQuotaExceededException()
	{
		return quotaExceededException;
	}

	public void setQuotaExceededException(QuotaExceededException quotaExceededException)
	{
		this.quotaExceededException = quotaExceededException;
	}

	public PreviewableItem getPreviewableItem()
	{
		throw new Error();
	}

	public void refresh()
	{
		// nothing to do here
	}

	public DuringSaveOperation getWizardSaveOperation(String key)
	{
		return saveOperations.get(key);
	}

	public void setWizardSaveOperation(String key, DuringSaveOperation op)
	{
		saveOperations.put(key, op);
	}

	public Map<String, DuringSaveOperation> getSaveOperations()
	{
		return saveOperations;
	}

	public boolean isNoCancel()
	{
		return noCancel;
	}

	public void setNoCancel(boolean noCancel)
	{
		this.noCancel = noCancel;
	}

	public List<UnsavedEditOperation> getUnsavedEdits()
	{
		return unsavedEdits;
	}

	public void addUnsavedEdit(UnsavedEditOperation editOp)
	{
		unsavedEdits.add(editOp);
	}

	@Override
	public void setItemPack(ItemPack<Item> pack)
	{
		item = pack.getItem();
		xmlBag = pack.getXml();
		xml = (xmlBag == null ? null : xmlBag.toString());
		stagingId = pack.getStagingID();
	}

	@Override
	public ItemPack<Item> getItemPack()
	{
		return new ItemPack<>(item, getItemxml(), stagingId);
	}

	public Operation getOperation()
	{
		return operation;
	}

	public WizardMetadataMapper getWizardMetadataMapper()
	{
		return wizardMetadataMapper;
	}

	public void setWizardMetadataMapper(WizardMetadataMapper wizardMetadataMapper)
	{
		this.wizardMetadataMapper = wizardMetadataMapper;
	}

	protected Deque<Pair<String, Integer>> ensurePathOverrides()
	{
		if( pathOverrides == null )
		{
			pathOverrides = new ArrayDeque<Pair<String, Integer>>();
		}
		return pathOverrides;
	}

	/**
	 * @return A *copy* of the path overrides. If you want to change them, use
	 *         pushPathOverride and popPathOverride
	 */
	public Deque<Pair<String, Integer>> getPathOverrides()
	{
		return new ArrayDeque<Pair<String, Integer>>(ensurePathOverrides());
	}

	public void pushPathOverride(Pair<String, Integer> override)
	{
		ensurePathOverrides().push(override);
	}

	public Pair<String, Integer> popPathOveride()
	{
		return ensurePathOverrides().pop();
	}

	@Override
	public void onSessionSave()
	{
		if( xmlBag != null )
		{
			xml = xmlBag.toString();
		}
		xmlBag = null;
		pathOverrides = null;
	}

	@Override
	public WizardState clone()
	{
		try
		{
			final WizardState other = (WizardState) super.clone();
			other.xmlBag = null;

			other.drm = Lists.newArrayList(drm);
			other.duplicateData = Maps.newHashMap(duplicateData);
			other.unsavedEdits = Lists.newArrayList(unsavedEdits);
			other.saveOperations = Maps.newHashMap(saveOperations);
			if( pageStates != null )
			{
				other.pageStates = Lists.newArrayList(pageStates);
			}

			// TODO:
			// other.pages = null;

			return other;
		}
		catch( CloneNotSupportedException e )
		{
			throw Throwables.propagate(e);
		}
	}

	public String getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(String thumbnail)
	{
		this.thumbnail = thumbnail;
	}

	public BiMap<UUID, String> getRegisteredFilenames()
	{
		return registeredFilenames;
	}
}
