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

package com.tle.web.notification.filters;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.standard.indexer.NotificationIndex;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.notification.WebNotificationExtension;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@SuppressWarnings("nls")
public class FilterByNotificationReason extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private NotificationService notificationService;

	@Component(parameter = "reason", supported = true)
	private SingleSelectionList<Void> reasonList;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> resultsSection;

	@PlugKey(value = "notereason.any", html = false)
	private static Label LABEL_ANY;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		reasonList.setListModel(new ReasonModel());
		reasonList.addChangeEventHandler(resultsSection.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("reasonfilter.ftl", this);
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		String reason = reasonList.getSelectedValueAsString(info);
		if( !Check.isEmpty(reason) )
		{
			event.filterByTerm(false, NotificationIndex.FIELD_REASON, reason);
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		reasonList.setSelectedStringValue(info, null);
	}

	public void setReason(SectionInfo info, String reason)
	{
		reasonList.setSelectedStringValue(info, reason);
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(reasonList, null, null);
	}

	public SingleSelectionList<Void> getReasonList()
	{
		return reasonList;
	}

	public class ReasonModel extends DynamicHtmlListModel<Void>
	{
		@Override
		protected Iterable<Void> populateModel(SectionInfo info)
		{
			return null;
		}

		@Override
		protected Iterable<Option<Void>> populateOptions(SectionInfo info)
		{
			List<Option<Void>> options = Lists.newArrayList();
			Collection<String> notifications = notificationService.getNotificationTypes();
			for( String type : notifications )
			{
				WebNotificationExtension extension = (WebNotificationExtension) notificationService
					.getExtensionForType(type);
				if( extension.isIndexed(type) )
				{
					options.add(new LabelOption<Void>(extension.getReasonFilterLabel(type), type, null));
				}
			}
			return options;
		}

		@Override
		protected Option<Void> getTopOption()
		{
			return new LabelOption<Void>(LABEL_ANY, "", null);
		}
	}
}
