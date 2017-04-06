package com.tle.web.searching.itemlist;

import javax.inject.Inject;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.system.SearchSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.item.ItemResolver;
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
