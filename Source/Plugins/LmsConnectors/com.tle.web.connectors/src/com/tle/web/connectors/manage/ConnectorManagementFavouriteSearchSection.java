package com.tle.web.connectors.manage;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.guice.Bind;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class ConnectorManagementFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@Inject
	private BundleCache bundleCache;
	@TreeLookup
	private ConnectorManagementQuerySection querySection;

	@Override
	protected String getWithin(SectionInfo info)
	{
		BaseEntityLabel label = querySection.getConnectorList().getSelectedValue(info);
		if( label != null )
		{
			return new BundleNameValue(label.getBundleId(), label.getUuid(), bundleCache).getName();
		}
		return null;
	}

	@Override
	protected String getCriteria(SectionInfo info)
	{
		return null;
	}
}
