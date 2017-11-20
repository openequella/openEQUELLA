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

package com.tle.web.searching.itemlist;

import javax.inject.Inject;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.item.service.ItemResolver;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractPrototypeSection;

abstract class ItemListFileCountDisplaySection extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, StandardItemListEntry>
{
	@Inject
	private ItemResolver itemResolver;

	@Inject
	private ConfigurationService configService;

	protected boolean isFileCountDisabled()
	{
		return configService.getProperties(new SearchSettings()).isFileCountDisabled();
	}

	protected boolean canViewRestricted(IItem<?> item)
	{
		return itemResolver.canViewRestrictedAttachments(item, null);
	}

	/**
	 * 
	 * @param item
	 * @param attachment
	 * @return true if the attachment is restricted AND the user doesn't have permissions to view it.
	 */
	protected boolean checkRestrictedAttachment(IItem<?> item, IAttachment attachment)
	{
		return itemResolver.checkRestrictedAttachment(item, attachment, null);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
