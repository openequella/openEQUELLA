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

package com.tle.web.portal.section.admin.filter;

import javax.inject.Inject;

import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.core.portal.service.PortletService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.section.admin.PortletSearchEvent;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class FilterPortletByTypeSection extends AbstractPrototypeSection<Object>
	implements
		SearchEventListener<PortletSearchEvent>,
		HtmlRenderer,
		ResetFiltersListener
{
	@PlugKey("filter.bytype.all")
	private static String ALL_TYPES;

	@Inject
	private PortletService portletService;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Component(name = "pt", parameter = "pt", supported = true)
	protected SingleSelectionList<PortletTypeDescriptor> portletType;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	private JSHandler changeHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		PortletTypeModel portletTypeModel = new PortletTypeModel();
		portletTypeModel.addAll(portletService.listAllAvailableTypes());
		portletType.setListModel(portletTypeModel);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("admin/filter/filterbytype.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, PortletSearchEvent event) throws Exception
	{
		PortletTypeDescriptor type = portletType.getSelectedValue(info);
		if( type != null )
		{
			event.filterByType(type.getType());
		}
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		if( changeHandler == null )
		{
			changeHandler = searchResults.getRestartSearchHandler(tree);
		}
		portletType.addChangeEventHandler(changeHandler);
	}

	public SingleSelectionList<PortletTypeDescriptor> getPortletType()
	{
		return portletType;
	}

	public static class PortletTypeModel extends SimpleHtmlListModel<PortletTypeDescriptor>
	{
		public PortletTypeModel()
		{
			add(null);
		}

		@Override
		protected Option<PortletTypeDescriptor> convertToOption(PortletTypeDescriptor obj)
		{
			if( obj == null )
			{
				return new KeyOption<PortletTypeDescriptor>(ALL_TYPES, "", //$NON-NLS-1$
					null);
			}
			return new KeyOption<PortletTypeDescriptor>(obj.getNameKey(), obj.getType(), obj);
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		portletType.setSelectedStringValue(info, ALL_TYPES);
	}
}