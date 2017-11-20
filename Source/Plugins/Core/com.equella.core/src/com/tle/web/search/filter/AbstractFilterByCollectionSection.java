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

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

public abstract class AbstractFilterByCollectionSection<M extends AbstractFilterByCollectionSection.AbstractFilterByCollectionModel>
	extends
		AbstractPrototypeSection<M>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	protected static final String ALL_KEY = "all";

	@Inject
	protected ItemDefinitionService itemDefinitionService;
	@Inject
	protected BundleCache bundleCache;

	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@TreeLookup
	protected AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@Component(name = "c", parameter = "in", supported = true)
	protected SingleSelectionList<WhereEntry> collectionList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		collectionList.setListModel(getCollectionModel());
		collectionList.setAlwaysSelect(true);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( isDisabled(info) )
		{
			return;
		}

		WhereEntry selCollection = collectionList.getSelectedValue(info);
		if( selCollection != null )
		{
			ItemDefinition entity = selCollection.getEntity();
			event.filterByCollection(entity.getUuid());
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( isDisabled(context) )
		{
			return null;
		}

		return viewFactory.createResult("filter/filterbycollection.ftl", context);
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	public boolean isDisabled(SectionInfo info)
	{
		return getModel(info).isDisabled();
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		collectionList.addChangeEventHandler(
			new StatementHandler(searchResults.getResultsUpdater(tree, null, "searchresults-actions")));
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractFilterByCollectionModel(false);
	}

	public abstract DynamicHtmlListModel<WhereEntry> getCollectionModel();

	public static class AbstractFilterByCollectionModel
	{
		private boolean disabled;

		public AbstractFilterByCollectionModel(boolean disabled)
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

	}

	@Override
	public void reset(SectionInfo info)
	{
		collectionList.setSelectedStringValue(info, null);
	}

	public SingleSelectionList<WhereEntry> getCollectionList()
	{
		return collectionList;
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(collectionList, null, null);
	}

	// Cut down version of SearchQuerySection.WhereEntry
	public class WhereEntry
	{
		private final String value;
		@Nullable
		private NameValue name;
		@Nullable
		private ItemDefinition entity;

		public WhereEntry(BaseEntityLabel bel)
		{
			this(bel.getUuid());
			this.name = new BundleNameValue(bel.getBundleId(), value, bundleCache);
		}

		public WhereEntry(String uuid)
		{
			this.value = uuid;
		}

		public Option<WhereEntry> convert()
		{
			return new NameValueOption<WhereEntry>(getName(), this);
		}

		private NameValue getName()
		{
			if( name == null )
			{
				name = new BundleNameValue(getEntity().getName(), value, bundleCache);
			}
			return name;
		}

		public ItemDefinition getEntity()
		{
			if( entity == null )
			{
				entity = itemDefinitionService.getByUuid(value);
			}
			return entity;
		}

		public String getValue()
		{
			return value;
		}
	}
}
