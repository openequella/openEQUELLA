/**
 * 
 */
package com.tle.web.search.filter;

import com.tle.beans.item.ItemStatus;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.equella.ItemStatusKeys;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class ItemStatusListModel extends SimpleHtmlListModel<ItemStatus>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(ItemStatusListModel.class);

	public ItemStatusListModel()
	{
		add(null);
	}

	@SuppressWarnings("nls")
	@Override
	protected Option<ItemStatus> convertToOption(ItemStatus obj)
	{
		if( obj == null )
		{
			return new KeyOption<ItemStatus>(RESOURCES.key("statusfilter.all"), "", null);
		}
		return new KeyOption<ItemStatus>(ItemStatusKeys.get(obj), obj.name().toLowerCase(), obj);
	}
}