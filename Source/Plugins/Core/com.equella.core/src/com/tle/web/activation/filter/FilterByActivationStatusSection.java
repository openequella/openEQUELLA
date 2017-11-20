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

package com.tle.web.activation.filter;

import javax.inject.Inject;

import com.tle.beans.activation.ActivateRequest;
import com.tle.common.NameValue;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
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
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

@SuppressWarnings("nls")
public class FilterByActivationStatusSection extends AbstractPrototypeSection<FilterByActivationStatusSection.Model>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FreetextSearchEvent>
{
	@PlugKey("filter.bystatus.all")
	private static String ALL_STATUSES;
	private static final String ALL_VALUE = "all";
	
	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@Inject
	private ActivationService activationService;

	@Component(name = "as")
	private SingleSelectionList<NameValue> activationStatus;
	
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		SimpleHtmlListModel<NameValue> activationStatusOptions = new SimpleHtmlListModel<NameValue>();
		activationStatusOptions.add(new BundleNameValue(ALL_STATUSES, ALL_VALUE));
		activationStatusOptions.add(new BundleNameValue(activationService.getStatusKey(ActivateRequest.TYPE_ACTIVE),
			String.valueOf(ActivateRequest.TYPE_ACTIVE)));
		activationStatusOptions.add(new BundleNameValue(activationService.getStatusKey(ActivateRequest.TYPE_INACTIVE),
			String.valueOf(ActivateRequest.TYPE_INACTIVE)));
		activationStatusOptions.add(new BundleNameValue(activationService.getStatusKey(ActivateRequest.TYPE_PENDING),
			String.valueOf(ActivateRequest.TYPE_PENDING)));
		activationStatus.setListModel(activationStatusOptions);
		activationStatus.setAlwaysSelect(true);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		JSHandler changeHandler = searchResults.getRestartSearchHandler(tree);
		activationStatus.addChangeEventHandler(changeHandler);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("filter/connector-filterbystatus.ftl", context);
	}


	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		String status = activationStatus.getSelectedValueAsString(info);
		if( !status.equals(ALL_VALUE) )
		{
			event.getRawSearch().setActivationStatus(status);
			event.setUserFiltered(true);
		}
	}

	@Override
	public void reset(SectionInfo info)
	{
		activationStatus.setSelectedStringValue(info, ALL_VALUE);
	}

	public class Model
	{
		// big lot of nothing
	}

	public SingleSelectionList<NameValue> getActivationStatus()
	{
		return activationStatus;
	}
}
