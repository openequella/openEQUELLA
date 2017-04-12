package com.tle.web.cloud.search.selection;

import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.search.CloudSearchListEntry;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.search.selection.AbstractSelectItemListExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;

/**
 * @author Aaron
 */
@Bind
public class CloudSelectItemListExtension extends AbstractSelectItemListExtension<CloudItem, CloudSearchListEntry>
{
	@Override
	protected ViewableItem<CloudItem> getViewableItem(SectionInfo info, CloudItem item)
	{
		return new CloudViewableItem(item);
	}

	@Override
	public String getItemExtensionType()
	{
		return CloudConstants.ITEM_EXTENSION;
	}
}
