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

package com.tle.web.itemadmin.section;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
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
