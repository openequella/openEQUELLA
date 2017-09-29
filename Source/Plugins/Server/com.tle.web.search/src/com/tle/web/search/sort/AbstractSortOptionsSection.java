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

package com.tle.web.search.sort;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.searching.SortField;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@NonNullByDefault
@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractSortOptionsSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractPrototypeSection<AbstractSortOptionsSection.SortOptionsModel>
	implements
		SearchEventListener<SE>,
		HtmlRenderer,
		SortOptionsListener
{
	@ViewFactory(fixed = true)
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@PlugKey("sortsection.results.reverse")
	private static Label LABEL_REVERSE;

	@Component(name = "so", parameter = "sort", supported = true)
	protected SingleSelectionList<SortOption> sortOptions;

	@Component(name = "r", parameter = "rs", supported = true)
	protected Checkbox reverse;
	private ImmutableList<SortOption> staticOptions;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		List<SortOption> sorts = new ArrayList<SortOption>();
		addSortOptions(sorts);
		staticOptions = ImmutableList.copyOf(sorts);

		sortOptions.setListModel(createListModel());
		sortOptions.setAlwaysSelect(true);
		reverse.setLabel(LABEL_REVERSE);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SORT);
	}

	@Nullable
	protected abstract String getDefaultSearch(SectionInfo info);

	@Override
	@Nullable
	public Iterable<SortOption> addSortOptions(SectionInfo info, AbstractSortOptionsSection<?> section)
	{
		if( section == this )
		{
			return staticOptions;
		}
		return null;
	}

	protected HtmlListModel<SortOption> createListModel()
	{
		return new DynamicHtmlListModel<SortOption>()
		{
			@Override
			protected Iterable<SortOption> populateModel(SectionInfo info)
			{
				SortOptionsEvent event = new SortOptionsEvent(AbstractSortOptionsSection.this);
				info.processEvent(event);
				return Iterables.concat(event.getExtraOptions());
			}

			@Override
			protected Option<SortOption> convertToOption(SectionInfo info, SortOption obj)
			{
				return new LabelOption<SortOption>(obj.getLabel(), obj.getValue(), obj);
			}

			@Override
			public String getDefaultValue(SectionInfo info)
			{
				String defaultValue = getDefaultSearch(info);
				if( defaultValue == null || getOption(info, defaultValue) == null )
				{
					return super.getDefaultValue(info);
				}
				return defaultValue;
			}
		};
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		sortOptions.addChangeEventHandler(searchResults.getResultsUpdater(tree, null, "reverse"));
		reverse.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null)));
	}

	@Override
	public void prepareSearch(SectionInfo info, SE event)
	{
		if( !getModel(info).isDisabled() )
		{
			SortOption selOpt = sortOptions.getSelectedValue(info);
			if( selOpt != null )
			{
				SortField[] fields = createSortFromOption(info, selOpt);
				event.setSortFields(reverse.isChecked(info), fields);
			}
		}
	}

	protected SortField[] createSortFromOption(SectionInfo info, SortOption selOpt)
	{
		return selOpt.createSort();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !getModel(context).isDisabled() )
		{
			return viewFactory.createResult("sort/sortoptions.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	public Checkbox getReverse()
	{
		return reverse;
	}

	protected void addSortOptions(List<SortOption> sorts)
	{
		// For sub classes - Override this method to add sort options
	}

	public SingleSelectionList<SortOption> getSortOptions()
	{
		return sortOptions;
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SortOptionsModel();
	}

	public static class SortOptionsModel
	{
		private boolean disabled;

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(sortOptions, null, null);
	}
}
