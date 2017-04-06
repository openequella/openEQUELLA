package com.tle.web.cloud.search.section;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.beans.item.ItemId;
import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.search.CloudSearchListEntry;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.itemlist.standard.AbstractItemlikeListAttachmentDisplaySection;
import com.tle.web.viewable.ViewableItem;

/**
 * @author Aaron
 */
@Bind
public class CloudSearchListAttachmentSection
	extends
		AbstractItemlikeListAttachmentDisplaySection<CloudItem, CloudSearchListEntry>
{
	@Inject
	private CloudService cloudService;

	@Override
	protected boolean canSeeAttachments(CloudItem item)
	{
		return true;
	}

	@Override
	protected SearchDetails getSearchDetails(CloudItem item)
	{
		return null;
	}

	@Override
	protected CloudItem getItem(CloudSearchListEntry entry)
	{
		return entry.getItem();
	}

	@Override
	protected CloudItem getItem(ItemId itemId)
	{
		return cloudService.getItem(itemId.getUuid(), itemId.getVersion());
	}

	@Override
	protected ViewableItem<CloudItem> getViewableItem(CloudItem item)
	{
		return new CloudViewableItem(item);
	}

	@Override
	public String getItemExtensionType()
	{
		return CloudConstants.ITEM_EXTENSION;
	}
}
