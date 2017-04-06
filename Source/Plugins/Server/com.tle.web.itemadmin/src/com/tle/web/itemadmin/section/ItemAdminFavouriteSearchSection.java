package com.tle.web.itemadmin.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.search.actions.AbstractFavouriteSearchSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;

/**
 * @author Aaron
 */
@Bind
public class ItemAdminFavouriteSearchSection extends AbstractFavouriteSearchSection
{
	@Inject
	private BundleCache bundleCache;
	@TreeLookup
	private ItemAdminQuerySection iaqs;

	@Override
	protected String getWithin(SectionInfo info)
	{
		WithinEntry wi = iaqs.getCollectionList().getSelectedValue(info);
		if( wi != null )
		{
			BaseEntityLabel label = wi.getBel();
			return new BundleNameValue(label.getBundleId(), label.getUuid(), bundleCache).getName();
		}
		return null;
	}

	@Override
	protected String getCriteria(SectionInfo info)
	{
		StringBuilder sbuf = new StringBuilder();
		List<String> criteriaList = iaqs.getModel(info).getCriteria();
		if( !Check.isEmpty(criteriaList) )
		{
			for( String criteria : criteriaList )
			{
				sbuf.append(criteria);
				sbuf.append('\n');
			}
		}

		return sbuf.toString();
	}
}
