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

package com.tle.web.cloud.search.filters;

import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
public class CloudFilterByLanguageSection extends AbstractCloudFilter
{
	@PlugKey("filter.bylanguage.title")
	private static Label LABEL_TITLE;
	@PlugKey("filter.bylanguage.top")
	private static String DEFAULT_OPTION;

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void prepareSearch(SectionInfo info, CloudSearchEvent event) throws Exception
	{
		String val = list.getSelectedValueAsString(info);
		if( !Check.isEmpty(val) )
		{
			event.getCloudSearch().setLanguage(val);
		}
	}

	@Override
	public HtmlListModel<NameValue> buildListModel()
	{
		return new DynamicHtmlListModel<NameValue>()
		{
			@Override
			protected Option<NameValue> getTopOption()
			{
				return new KeyOption<NameValue>(DEFAULT_OPTION, "", null);
			}

			@Override
			protected Iterable<NameValue> populateModel(SectionInfo info)
			{
				return cloudService.getCloudFilterInfo().getLanguages();
			}
		};
	}

	@Override
	protected String getPublicParam()
	{
		return "lang";
	}
}
