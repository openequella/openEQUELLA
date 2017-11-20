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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.searching.SearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.item.service.ItemService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.ClusteredTask;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.section.AbstractBulkResultsDialog;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.connectors.service.ConnectorBulkOperationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
public class ConnectorBulkSelectionSection extends AbstractBulkSelectionSection<ConnectorItemKey>
{
	private static final String KEY_SELECTIONS = "connectorSelections"; //$NON-NLS-1$

	@PlugKey("connector.selectionsbox.selectall")
	private static Label LABEL_SELECTALL;
	@PlugKey("connector.selectionsbox.unselect")
	private static Label LABEL_UNSELECTALL;
	@PlugKey("connector.selectionsbox.viewselected")
	private static Label LABEL_VIEWSELECTED;
	@PlugKey("connector.selectionsbox.pleaseselect")
	private static Label LABEL_PLEASE;
	@PlugKey("connector.selectionsbox.count")
	private static String LABEL_COUNT;

	@TreeLookup
	private ConnectorManagementResultsSection resultsSection;
	@TreeLookup
	private ConnectorManagementQuerySection querySection;

	@Inject
	@Component
	private ConnectorBulkResultsDialog bulkDialog;
	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorBulkOperationService connectorBulkService;
	@Inject
	private ItemService itemService;
	@Inject
	private TaskService taskService;

	@Override
	protected boolean showSelections(SectionInfo info)
	{
		Connector connector = querySection.getConnector(info);
		if( connector == null || !connectorService.canExport(connector)
			|| !repositoryService.supportsExport(connector.getLmsType()) )
		{
			return false;
		}
		return true;
	}

	@Override
	protected Label getLabelSelectAll()
	{
		return LABEL_SELECTALL;
	}

	@Override
	protected Label getLabelUnselectAll()
	{
		return LABEL_UNSELECTALL;
	}

	@Override
	protected Label getLabelViewSelected()
	{
		return LABEL_VIEWSELECTED;
	}

	@Override
	protected AbstractBulkResultsDialog<ConnectorItemKey> getBulkDialog()
	{
		return bulkDialog;
	}

	@Override
	protected Label getPleaseSelectLabel()
	{
		return LABEL_PLEASE;
	}

	@Override
	protected Label getSelectionBoxCountLabel(int selectionCount)
	{
		return new PluralKeyLabel(LABEL_COUNT, selectionCount);
	}

	@Override
	public void selectAll(SectionInfo info)
	{
		ConnectorManagementSearchEvent searchEvent = resultsSection.createSearchEvent(info);
		info.processEvent(searchEvent);

		Connector connector = searchEvent.getConnector();
		ConnectorContentSearch search = searchEvent.getSearch();
		ExternalContentSortType sort = search.getSort();
		Model<ConnectorItemKey> model = getModel(info);

		try
		{
			SearchResults<ConnectorContent> findAllUsages = repositoryService.findAllUsages(connector,
				CurrentUser.getUsername(), searchEvent.getQuery(), search.getCourse(), search.getFolder(),
				search.isArchived(), 0, Integer.MAX_VALUE, sort, search.isReverse());

			Set<ConnectorItemKey> selections = model.getSelections();

			for( ConnectorContent content : findAllUsages.getResults() )
			{
				final String uuid = content.getUuid();
				if( uuid != null )
				{
					int version = content.getVersion();
					if( version == 0 )
					{
						// FIXME perhaps not very efficient...
						version = itemService.getLatestVersion(content.getUuid());
						content.setVersion(version);
					}
					selections.add(new ConnectorItemKey(content, connector.getId()));
				}
			}
		}
		catch( LmsUserNotFoundException e )
		{
			throw new RuntimeException(e);
		}

		model.setModifiedSelection(true);
	}

	@Override
	protected String getKeySelections()
	{
		return KEY_SELECTIONS;
	}

	@Override
	public String executeWithExecutor(SectionInfo info, BeanLocator<? extends BulkOperationExecutor> executor)
	{
		Model<ConnectorItemKey> model = getModel(info);
		Set<ConnectorItemKey> selections = model.getSelections();
		List<ConnectorItemKey> items = new ArrayList<ConnectorItemKey>(selections);
		selections.clear();
		model.setModifiedSelection(true);
		ClusteredTask task = connectorBulkService.createTask(items, executor);
		return taskService.startTask(task);
	}

	@Override
	protected boolean useBitSet()
	{
		return false;
	}
}
