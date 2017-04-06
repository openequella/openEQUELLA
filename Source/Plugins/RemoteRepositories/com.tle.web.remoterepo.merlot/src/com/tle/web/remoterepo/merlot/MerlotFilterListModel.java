package com.tle.web.remoterepo.merlot;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

/**
 * @author Aaron
 */
public class MerlotFilterListModel extends DynamicHtmlListModel<NameValue>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(MerlotFilterListModel.class);

	private final MerlotFilterType filterType;

	public MerlotFilterListModel(MerlotFilterType filterType)
	{
		this.filterType = filterType;
	}

	@SuppressWarnings("nls")
	@Override
	protected Iterable<NameValue> populateModel(SectionInfo info)
	{
		final List<NameValue> vals = new ArrayList<NameValue>();
		vals.add(new BundleNameValue(RESOURCES.key("filter.all"), ""));
		vals.addAll(filterType.getValues(info));
		return vals;
	}
}