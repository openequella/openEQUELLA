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

package com.tle.web.connectors.manage;

import javax.inject.Inject;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
public class ConnectorManagementQuerySection
	extends
		AbstractResetFiltersQuerySection<AbstractResetFiltersQuerySection.AbstractQuerySectionModel, ConnectorManagementSearchEvent>
{

	@PlugKey("manage.selectconnector")
	private static String KEY_SELECT;
	@PlugKey("manage.query.search")
	private static Label LABEL_SEARCH;

	private static final String DIV_QUERY = "searchform";

	@Inject
	private ConnectorService connectorService;

	@Component(parameter = "connector", supported = true)
	private SingleSelectionList<BaseEntityLabel> connectorList;

	@Inject
	private BundleCache bundleCache;

	@TreeLookup
	private ConnectorManagementResultsSection searchResults;
	@TreeLookup
	private ConnectorBulkSelectionSection bulkSection;

	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		connectorList.setListModel(new ConnectorsListModel());
		searchButton.setLabel(LABEL_SEARCH);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		searchButton.setClickHandler(searchResults.getRestartSearchHandler(tree));
		connectorList.addChangeEventHandler(new StatementHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("resetSearch"), "searchactions-filters", "bulk-selection", "searchactions-sort")));
	}

	@EventHandlerMethod
	public void resetSearch(SectionInfo info)
	{
		getResetFiltersSection().resetFilters(info);
		bulkSection.unselectAll(info);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		renderQueryActions(context, getModel(context));
		return viewFactory.createResult("manage-query.ftl", this);
	}

	public SingleSelectionList<BaseEntityLabel> getConnectorList()
	{
		return connectorList;
	}

	public Connector getConnector(SectionInfo info)
	{
		BaseEntityLabel label = connectorList.getSelectedValue(info);
		if( label == null )
		{
			return null;

		}
		return connectorService.get(label.getId());
	}

	@Override
	protected String getAjaxDiv()
	{
		return DIV_QUERY;
	}

	@Override
	public AbstractQuerySectionModel instantiateModel(SectionInfo info)
	{
		return new AbstractQuerySectionModel();
	}

	public class ConnectorsListModel extends DynamicHtmlListModel<BaseEntityLabel>
	{
		public ConnectorsListModel()
		{
			setSort(true);
		}

		@Override
		protected Option<BaseEntityLabel> getTopOption()
		{
			return new KeyOption<BaseEntityLabel>(KEY_SELECT, "", null);
		}

		@Override
		protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
		{
			return connectorService.listForViewing();
		}

		@Override
		protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
		{
			return new NameValueOption<BaseEntityLabel>(new BundleNameValue(bent.getBundleId(), bent.getUuid(),
				bundleCache), bent);
		}
	}

}
