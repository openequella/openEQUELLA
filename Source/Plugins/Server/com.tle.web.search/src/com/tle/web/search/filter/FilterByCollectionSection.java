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

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
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

@SuppressWarnings("nls")
@NonNullByDefault
public class FilterByCollectionSection extends AbstractPrototypeSection<FilterByCollectionSection.Model>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	private static final String ALL_KEY = "all";

	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "c", parameter = "in", supported = true)
	private SingleSelectionList<WhereEntry> collectionList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		collectionList.setListModel(new SearchWhereModel());
		collectionList.setAlwaysSelect(true);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		collectionList.addChangeEventHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null,
			"searchresults-actions")));
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

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model(false);
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

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	public boolean isDisabled(SectionInfo info)
	{
		return getModel(info).isDisabled();
	}

	// Cut down version of SearchQuerySection.SearchWhereModel
	public class SearchWhereModel extends DynamicHtmlListModel<WhereEntry>
	{
		public SearchWhereModel()
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

	protected static class Model
	{
		private boolean disabled;

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
	}

	public SingleSelectionList<WhereEntry> getCollectionList()
	{
		return collectionList;
	}

	@Override
	public void reset(SectionInfo info)
	{
		collectionList.setSelectedStringValue(info, null);
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(collectionList, null, null);
	}
}