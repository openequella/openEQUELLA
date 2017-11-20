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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.FileSystemException;
import com.dytech.edge.exceptions.WorkflowException;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.beans.entity.itemdef.Wizard;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemXml;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.event.IndexItemBackgroundEvent;
import com.tle.core.item.event.IndexItemNowEvent;
import com.tle.core.item.event.ItemWentLiveEvent;
import com.tle.core.item.event.UpdateReferencedUrlsEvent;
import com.tle.core.item.event.WaitForItemIndexEvent;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemLockingService;
import com.tle.core.item.standard.FilterFactory;
import com.tle.core.item.standard.NotifyMyLive;
import com.tle.core.item.standard.operations.workflow.InsecureArchiveOperation;
import com.tle.core.notification.beans.Notification;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.exceptions.AccessDeniedException;

public class SaveOperation extends AbstractStandardWorkflowOperation implements DeleteHandler
{
	private static final String CREATE_ITEM_PRIV = "CREATE_ITEM"; //$NON-NLS-1$

	public static final String KEY_PRESAVE = "preSaveOperations"; //$NON-NLS-1$
	public static final String KEY_POSTSAVE = "postSaveOperations"; //$NON-NLS-1$

	private static final String SAVE_SCRIPT_NAME = "saveOperation"; //$NON-NLS-1$

	private final boolean unlock;
	private boolean noAutoArchive;
	private boolean noSaveScript;
	private final List<WorkflowOperation> preSaveOperations;
	private final List<WorkflowOperation> postSaveOperations;

	@Inject
	private ItemLockingService lockingService;
	@Inject
	private StagingService stagingService;
	@Inject
	private QuotaService quotaService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private ItemDao dao;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private FilterFactory filterFactory;
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private Provider<InsecureArchiveOperation> archiveProvider;

	@AssistedInject
	protected SaveOperation()
	{
		this(true, null, null);
	}

	@AssistedInject
	protected SaveOperation(@Assisted boolean unlock)
	{
		this(unlock, null, null);
	}

	@AssistedInject
	protected SaveOperation(@Assisted boolean unlock, @Assisted("pre") List<WorkflowOperation> preSaveOperations,
		@Assisted("post") List<WorkflowOperation> postSaveOperations)
	{
		this.unlock = unlock;
		this.preSaveOperations = preSaveOperations;
		this.postSaveOperations = postSaveOperations;
	}

	@Override
	public boolean execute()
	{
		params.setAttribute("unlock", Boolean.toString(unlock));
		itemService.executeOperationsNow(params, preSaveOperations);
		itemService.executeExtensionOperationsNow(params, "preSave");
		ItemPack<Item> pack = params.getItemPack();
		if( params.isModified() )
		{
			if( !noSaveScript )
			{
				runSaveScript();
			}
			Item item = pack.getItem();
			item.setDateForIndex(params.getDateNow());
			if( item.getDateModified() == null )
			{
				item.setDateModified(params.getDateNow());
			}

			final boolean wentlive = params.isWentLive();
			if( wentlive )
			{
				addNotifications(item.getItemId(), item.getNotifications(), Notification.REASON_WENTLIVE, true);
				if (getWorkflow() != null)
				{
					NotifyMyLive.notifyOwners(this, userPreferenceService);
				}

				if( dao.getLatestLiveVersion(item.getUuid()) > item.getVersion() )
				{
					// Dodgy hack to avoid dodgy copy-and-paste hack
					InsecureArchiveOperation iao = archiveProvider.get();
					iao.setParams(params);
					iao.execute();
				}
			}
			saveToRepository(pack, params.isUpdate());
			saveAttachments(item);

			ItemKey newKey = params.getItemKey();
			params.setItemKey(newKey, item.getId());

			if( wentlive )
			{
				addAfterCommitEvent(new ItemWentLiveEvent(newKey));
				if( !noAutoArchive && newKey.getVersion() > 1 )
				{
					params.addFilter(filterFactory.archiveOld(newKey));
				}
			}
			addIndexingEvents(params.getItemIdKey(), item);
		}
		else if( params.isRequiresReindex() )
		{
			addAfterCommitEvent(new IndexItemBackgroundEvent(params.getItemIdKey(), true));
		}
		if( unlock && pack != null )
		{
			lockingService.unlock(pack.getItem(), false);
			params.getSecurityStatus().setLock(null);
		}

		return false;
	}

	protected void addIndexingEvents(ItemIdKey newKey, Item item)
	{
		addAfterCommitEvent(new IndexItemNowEvent(newKey));
		addAfterCommitEvent(new IndexItemBackgroundEvent(newKey, false));
		addAfterCommitEvent(new WaitForItemIndexEvent(newKey));
	}

	protected void runSaveScript()
	{
		Wizard wizard = getCollection().getWizard();
		if( wizard != null )
		{
			final String saveScript = wizard.getSaveScript();
			if( !Check.isEmpty(saveScript) )
			{
				itemService.executeScript(saveScript, SAVE_SCRIPT_NAME, createScriptContext(null), true);
			}
		}
	}

	private void saveToRepository(ItemPack<Item> itemPack, boolean doUpdate)
	{
		Item item = itemPack.getItem();

		if( !doUpdate && aclManager
			.filterNonGrantedPrivileges(item.getItemDefinition(), Arrays.asList(CREATE_ITEM_PRIV)).isEmpty() )
		{
			throw new AccessDeniedException("You do not have the required privileges to" //$NON-NLS-1$
				+ " create items in this collection"); //$NON-NLS-1$
		}

		itemHelper.updateItemFromXml(itemPack, this, true);

		item.setInstitution(CurrentInstitution.get());
		String xmlString = itemPack.getXml().toString();
		if( doUpdate )
		{
			ItemXml itemXml = item.getItemXml();
			itemXml.setXml(xmlString);
		}
		else
		{
			item.setItemXml(new ItemXml(xmlString));
		}
		dao.saveOrUpdate(item);
		for( Runnable r : params.getAfterSave() )
		{
			r.run();
		}
		itemService.updateMetadataBasedSecurity(getItemXml(), getItem());
		itemService.executeOperationsNow(params, postSaveOperations);
		itemService.executeExtensionOperationsNow(params, "postSave"); //$NON-NLS-1$

		try
		{
			addAfterCommitEvent(new UpdateReferencedUrlsEvent(getItemKey()));
		}
		catch( Exception e )
		{
			LOGGER.warn("There was an error updating referenced urls for submitted item " + getItemId(), e);
		}

		dao.flush();
	}

	protected StagingFile getStagingForCommit()
	{
		return getStaging();
	}

	protected void saveAttachments(Item item)
	{
		try
		{
			final StagingFile staging = getStagingForCommit();
			if( staging != null )
			{
				final ItemFile itemFile = itemFileService.getItemFile(item);
				item.setTotalFileSize(quotaService.checkQuotaAndReturnNewItemSize(item, staging));

				params.addAfterCommitHook(ItemOperationParams.COMMIT_HOOK_PRIORITY_MEDIUM, new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if( unlock )
							{
								fileSystemService.commitFiles(staging, itemFile);
							}
							else
							{
								fileSystemService.saveFiles(staging, itemFile);
							}

							if( unlock )
							{
								stagingService.removeStagingArea(staging, false);
							}

						}
						catch( IOException e )
						{
							LOGGER.error("Error committing files", e); //$NON-NLS-1$
							throw new RuntimeException(e);
						}
					}
				});
			}
		}
		catch( FileSystemException ex )
		{
			if( ex.getCause() instanceof FileNotFoundException )
			{
				LOGGER.info("No attachments"); //$NON-NLS-1$
			}
			else
			{
				throw ex;
			}
		}
		catch( QuotaExceededException e )
		{
			throw new WorkflowException(e);
		}
	}

	@Override
	public boolean isReadOnly()
	{
		return false;
	}

	@Override
	public void deleteBundleObject(Object obj)
	{
		if( params.isUpdate() )
		{
			dao.deleteAny(obj);
		}
	}

	protected void setNoAutoArchive(boolean noAutoArchive)
	{
		this.noAutoArchive = noAutoArchive;
	}

	protected void setNoSaveScript(boolean noSaveScript)
	{
		this.noSaveScript = noSaveScript;
	}
}
