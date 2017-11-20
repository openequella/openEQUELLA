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

import java.util.Arrays;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@SuppressWarnings("nls")
public class FilterByItemStatusSection extends AbstractPrototypeSection<FilterByItemStatusSection.Model>
	implements
		SearchEventListener<FreetextSearchEvent>,
		HtmlRenderer,
		ResetFiltersListener
{
	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "is", parameter = "status", supported = true)
	protected SingleSelectionList<ItemStatus> itemStatus;

	@PlugKey("filter.bystatus.modonly")
	@Component(name = "om", parameter = "modonly", supported = true)
	protected Checkbox onlyInModeration;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		SimpleHtmlListModel<ItemStatus> statii = new ItemStatusListModel();
		statii.addAll(getStatusList());
		itemStatus.setListModel(statii);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	protected List<ItemStatus> getStatusList()
	{
		return Arrays.asList(ItemStatus.values());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler changeHandler = searchResults.getRestartSearchHandler(tree);
		itemStatus.addChangeEventHandler(changeHandler);
		onlyInModeration.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null,
			"searchresults-actions")));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}
		return viewFactory.createResult("filter/filterbystatus.ftl", context);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( getModel(info).isDisabled() )
		{
			return;
		}

		ItemStatus selStatus = itemStatus.getSelectedValue(info);
		if( selStatus != null )
		{
			event.filterByStatus(selStatus);
		}
		if( onlyInModeration.isChecked(info) )
		{
			event.getRawSearch().setModerating(true);
		}
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model(false);
	}

	public SingleSelectionList<ItemStatus> getItemStatus()
	{
		return itemStatus;
	}

	public void setOnlyInModeration(SectionInfo info, boolean inModeration)
	{
		onlyInModeration.setChecked(info, inModeration);
	}

	public static class Model
	{
		private boolean disabled;
		private boolean hideCheckBox;

		public Model(boolean disabled)
		{
			this.disabled = disabled;
		}

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}

		public boolean isHideCheckBox()
		{
			return hideCheckBox;
		}

		public void setHideCheckBox(boolean hideCheckBox)
		{
			this.hideCheckBox = hideCheckBox;
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		itemStatus.setSelectedStringValue(info, null);
		onlyInModeration.setChecked(info, false);
	}

	public Checkbox getOnlyInModeration()
	{
		return onlyInModeration;
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(itemStatus, null, null);
	}

}
