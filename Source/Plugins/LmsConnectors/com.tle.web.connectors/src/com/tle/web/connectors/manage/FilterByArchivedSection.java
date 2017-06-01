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

import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.HiddenState;
import com.tle.web.sections.standard.annotations.Component;

public class FilterByArchivedSection extends AbstractPrototypeSection<FilterByArchivedSection.Model>
	implements
		SearchEventListener<ConnectorManagementSearchEvent>,
		HtmlRenderer,
		ResetFiltersListener
{
	@ViewFactory
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private ConnectorManagementQuerySection querySection;
	@Inject
	private ConnectorRepositoryService repositoryService;

	@Component(parameter = "archived", supported = true)
	private HiddenState checkState;
	@Component
	private Checkbox includeArchived;

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
		includeArchived.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree,
			events.getEventHandler("setChecked"), "filterbycourse"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}
		if( isArchived(context) )
		{
			includeArchived.setChecked(context, true);
		}
		Connector connector = querySection.getConnector(context);
		if( connector == null )
		{
			return null;
		}

		ConnectorTerminology terminology = repositoryService.getConnectorTerminology(connector.getLmsType());
		includeArchived.setLabel(context, new KeyLabel(terminology.getShowArchived()));

		return viewFactory.createResult("filter/filterbyarchive.ftl", context);
	}

	@EventHandlerMethod
	public void setChecked(SectionInfo info)
	{
		boolean checked = includeArchived.isChecked(info);
		checkState.setValue(info, checked ? 2 : 1);
	}

	public boolean isArchived(SectionInfo info)
	{
		if( checkState.getIntValue(info, 0) % 2 == 0 )
		{
			return true;
		}
		return false;
	}

	@Override
	public void prepareSearch(SectionInfo info, ConnectorManagementSearchEvent connectorEvent) throws Exception
	{
		if( getModel(info).isDisabled() )
		{
			return;
		}
		ConnectorContentSearch search = connectorEvent.getSearch();
		boolean archived = isArchived(info);
		search.setArchived(archived);
		connectorEvent.setUserFiltered(!archived);
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

	@Override
	public void reset(SectionInfo info)
	{
		checkState.setValue(info, 0);
	}

	public Checkbox getIncludeArchived()
	{
		return includeArchived;
	}

	public HiddenState getCheckState()
	{
		return checkState;
	}
}
