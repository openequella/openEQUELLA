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

package com.tle.web.remoterepo.merlot.filter;

import com.tle.common.NameValue;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.remoterepo.merlot.service.MerlotSearchParams.KeywordUse;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.merlot.MerlotRemoteRepoSearchEvent;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public class MerlotFilterKeywordTypeSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<MerlotRemoteRepoSearchEvent>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@ResourceHelper
	private PluginResourceHelper RESOURCES;

	@Component(name = "kc")
	private SingleSelectionList<NameValue> typeList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		final SimpleHtmlListModel<NameValue> listModel = new SimpleHtmlListModel<NameValue>();
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.all"), KeywordUse.ALL.name()));
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.any"), KeywordUse.ANY.name()));
		listModel.add(new BundleNameValue(RESOURCES.key("filter.type.phrase"), KeywordUse.EXACT_PHRASE.name()));
		typeList.setListModel(listModel);
		typeList.setAlwaysSelect(true);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/merlotfiltertype.ftl", this);
	}

	@Override
	public void prepareSearch(SectionInfo info, MerlotRemoteRepoSearchEvent event) 
	{
		event.setKeywordUse(KeywordUse.valueOf(typeList.getSelectedValueAsString(info)));
	}

	public SingleSelectionList<NameValue> getTypeList()
	{
		return typeList;
	}
}
