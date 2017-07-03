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

package com.tle.web.search.filter;

import java.util.ArrayList;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
@NonNullByDefault
public class FilterByCollectionSection
	extends
		AbstractFilterByCollectionSection<AbstractFilterByCollectionSection.AbstractFilterByCollectionModel>
{

	@Override
	public DynamicHtmlListModel<WhereEntry> getCollectionModel()
	{
		return new FilterByCollectionListModel();
	}

	public class FilterByCollectionListModel extends DynamicHtmlListModel<WhereEntry>
	{
		public FilterByCollectionListModel()
		{
			setSort(true);
		}

		@Nullable
		@Override
		public WhereEntry getValue(SectionInfo info, @Nullable String value)
		{
			if( value == null || ALL_KEY.equals(value) )
			{
				return null;
			}
			return new WhereEntry(value);
		}

		@Override
		protected Option<WhereEntry> getTopOption()
		{
			return new KeyOption<WhereEntry>("com.tle.web.search.query.collection.all", ALL_KEY, null);
		}

		@Override
		protected Iterable<WhereEntry> populateModel(SectionInfo info)
		{
			List<WhereEntry> collectionOptions = new ArrayList<WhereEntry>();

			List<BaseEntityLabel> listSearchable = itemDefinitionService.listSearchable();

			for( BaseEntityLabel bel : listSearchable )
			{
				collectionOptions.add(new WhereEntry(bel));
			}

			return collectionOptions;
		}

		@Override
		protected Option<WhereEntry> convertToOption(SectionInfo info, WhereEntry obj)
		{
			return obj.convert();
		}
	}

}