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

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.item.service.DrmService;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.ims.service.IMSService;

public abstract class AbstractEditMetadataOperation extends AbstractStandardWorkflowOperation
{
	public static final String EDITMETADATA_EXTENSION = "edit"; //$NON-NLS-1$

	protected ItemPack<Item> newPack;

	@Inject
	private DrmService drmService;

	//TODO: this would be done by extension point
	@Inject
	private IMSNavigationHelper navHelper;
	@Inject
	private IMSService imsService;

	@Override
	public boolean execute()
	{
		itemService.executeExtensionOperationsLater(params, EDITMETADATA_EXTENSION);
		ItemPack<Item> pack = params.getItemPack();
		if( pack != null )
		{
			Item item = pack.getItem();
			item.setDateModified(params.getDateNow());
		}
		ensureItem();
		createHistory(HistoryEvent.Type.edit);
		return true;
	}

	private void ensureItem()
	{
		Item newItem = newPack.getItem();

		checkExistence();
		ensureNavigationNodes(newPack.getItem(), newPack.getStagingID());
		ensureDrm(newItem);
		ensureItemInternal(newItem);

		getParams().setUpdateSecurity(true);
	}

	protected abstract void checkExistence();

	protected abstract void ensureItemInternal(Item newItem);

	private void ensureNavigationNodes(Item item, String stagingId)
	{
		if( !Check.isEmpty(item.getTreeNodes()) )
		{
			return;
		}
		FileHandle attachFiles;
		if( !Check.isEmpty(stagingId) )
		{
			attachFiles = new StagingFile(stagingId);
		}
		else
		{
			attachFiles = itemFileService.getItemFile(item);
		}
		Attachments attachments = new UnmodifiableAttachments(item);
		List<ImsAttachment> imsAttachments = attachments.getList(AttachmentType.IMS);
		if( imsAttachments.isEmpty() )
		{
			return;
		}
		ImsAttachment imsAttachment = imsAttachments.get(0);
		boolean expand = imsAttachment.isExpand();

		if( !expand )
		{
			return;
		}

		String szPackage = imsAttachment.getUrl();

		IMSManifest manifest;
		try
		{
			manifest = imsService.getImsManifest(attachFiles, szPackage, true);
			String scormVersion = imsService.getScormVersion(attachFiles, szPackage);
			if( manifest != null )
			{
				navHelper.createTree(manifest, item, attachFiles, szPackage, !Check.isEmpty(scormVersion), expand);
			}
		}
		catch( IOException e )
		{
			LOGGER
				.error(CurrentLocale.get("com.tle.core.workflow.operations.editmeta.error.readingmanifest", szPackage)); //$NON-NLS-1$
		}
	}

	private void ensureDrm(Item item)
	{
		DrmSettings drmSettings = item.getDrmSettings();
		if( drmSettings != null )
		{
			List<WizardPage> pages = item.getItemDefinition().getWizard().getPages();
			for( WizardPage page : pages )
			{
				if( page instanceof DRMPage )
				{
					DRMPage drmPage = (DRMPage) page;
					drmSettings.setDrmPageUuid(drmPage.getUuid());
					drmService.mergeSettings(drmSettings, drmPage);
					return;
				}
			}
		}
		// no drm page, and unless it's a case of custom scripting, purge any
		// existing drm settings
		if( drmSettings == null
			|| !DrmSettings.CUSTOM_SCRIPTED_DRMSETTINGS_PAGE_PLACEHOLDER.equals(drmSettings.getDrmPageUuid()) )
		{
			item.setDrmSettings(null);
		}
	}

	public void setItemPack(ItemPack newPack)
	{
		this.newPack = newPack;
	}
}
