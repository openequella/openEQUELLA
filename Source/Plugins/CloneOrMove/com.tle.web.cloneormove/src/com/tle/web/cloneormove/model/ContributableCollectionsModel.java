package com.tle.web.cloneormove.model;

import java.util.Collections;
import java.util.List;

import com.dytech.common.text.NumberStringComparator;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.TextBundle;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class ContributableCollectionsModel extends DynamicHtmlListModel<ItemDefinition>
{
	private final ItemDefinitionService itemdefService;
	private final BundleCache bundleCache;

	public ContributableCollectionsModel(ItemDefinitionService itemdefService, BundleCache bundleCache)
	{
		this.itemdefService = itemdefService;
		this.bundleCache = bundleCache;
	}

	@Override
	protected Iterable<ItemDefinition> populateModel(SectionInfo info)
	{
		List<ItemDefinition> itemDefs = itemdefService.enumerateCreateable();
		Collections.sort(itemDefs, new NumberStringComparator<ItemDefinition>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(ItemDefinition t)
			{
				return TextBundle.getLocalString(t.getName(), bundleCache, null, "");
			}
		});
		itemDefs.add(0, null);
		return itemDefs;
	}

	@Override
	protected Option<ItemDefinition> convertToOption(SectionInfo info, ItemDefinition itemDef)
	{
		if( itemDef == null )
		{
			return new NameValueOption<ItemDefinition>(new NameValue(
				CurrentLocale.get("com.tle.web.cloneormove.selectcollection.option.collection.none"), ""), null);
		}
		return new NameValueOption<ItemDefinition>(new BundleNameValue(itemDef.getName(), itemDef.getUuid(),
			bundleCache), itemDef);
	}
}
