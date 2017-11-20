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

import static com.tle.web.connectors.manage.ConnectorManagementItemList.DIV_PFX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.freetext.queries.BaseCompoundQuery;
import com.tle.core.freetext.queries.NodeInQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.bulk.section.AbstractBulkSelectionSection;
import com.tle.web.connectors.manage.ConnectorManagementItemList.ConnectorManagementListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.annotations.Component;

public class ConnectorManagementResultsSection
	extends
		AbstractSearchResultsSection<ConnectorManagementListEntry, ConnectorManagementSearchEvent, ConnectorManagmentSearchResultEvent, SearchResultsModel>
{
	@Inject
	private Provider<ConnectorManagementListEntry> entryProvider;
	@Inject
	private ConnectorRepositoryService repositoryService;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private ConnectorManagementItemList connectorManagementItemList;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemService itemService;

	@TreeLookup
	private ConnectorBulkSelectionSection selectionSection;

	@AjaxFactory
	protected AjaxGenerator ajaxEvents;

	@TreeLookup
	private ConnectorManagementQuerySection querySection;

	@Component
	@Inject
	private EditConnectorContentDialog editDialog;

	@EventFactory
	private EventGenerator events;
	private JSCallable deleteFunction;
	private JSCallable editFunction;

	@PlugKey("manage.label.select")
	private static Label LABEL_SELECT_CONNECTOR;
	@PlugKey("manage.remove")
	private static Label LABEL_DELETED;
	@PlugKey("manage.remove.error")
	private static Label LABEL_DELETED_ERROR;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(connectorManagementItemList, id);
		deleteFunction = events.getSubmitValuesFunction("deleteContent"); //$NON-NLS-1$
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		editFunction = editDialog.getOpenFunction();
		editDialog.setOkCallback(selectionSection.getUpdateSelection(tree, events.getEventHandler("refreshContent"))); //$NON-NLS-1$
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		SectionResult renderHtml = super.renderHtml(context);
		if( querySection.getConnectorList().getSelectedValue(context) == null )
		{
			getModel(context).setErrorTitle(LABEL_SELECT_CONNECTOR);
		}

		return renderHtml;
	}

	@EventHandlerMethod
	public void refreshContent(SectionInfo info, ConnectorItemKey itemId)
	{
		addAjaxDiv(info, itemId);
	}

	private void addAjaxDiv(SectionInfo info, ConnectorItemKey itemId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + itemId.toKeyString());
		}
	}

	@EventHandlerMethod
	public void deleteContent(SectionInfo info, ConnectorItemKey itemId)
	{
		Connector connector = connectorService.get(itemId.getConnectorId());
		boolean success;
		try
		{
			success = repositoryService.deleteContent(connector, CurrentUser.getUsername(), itemId.getContentId());
		}
		catch( Exception e )
		{
			throw new AccessDeniedException(LABEL_DELETED_ERROR.getText(), e);
		}

		if( success )
		{
			receiptService.setReceipt(LABEL_DELETED);
		}
		else
		{
			throw new AccessDeniedException(LABEL_DELETED_ERROR.getText());
		}
	}

	@Override
	public void processResults(SectionInfo info, ConnectorManagmentSearchResultEvent results)
	{
		List<ConnectorContent> content = results.getResults().getResults();
		FreetextSearchResults<FreetextResult> search = results.getSearch();
		Connector connector = results.getConnector();
		List<Item> items = search.getResults();
		HashMap<ItemKey, Item> itemLookop = new HashMap<ItemKey, Item>();
		for( Item item : items )
		{
			itemLookop.put(item.getItemId(), item);
		}

		Collection<String> words = new DefaultSearch.QueryParser(results.getEvent().getSearchedText())
			.getHilightedList();
		ListSettings<ConnectorManagementListEntry> listSettings = connectorManagementItemList.getListSettings(info);
		listSettings.setHilightedWords(words);

		for( ConnectorContent connectorContent : content )
		{
			final ConnectorManagementListEntry listEntry = entryProvider.get();
			listEntry.setContent(connectorContent);
			listEntry.setConnector(connector);
			final String uuid = connectorContent.getUuid();
			Item item = null;
			if( uuid != null )
			{
				ItemKey itemKey = new ItemId(uuid, connectorContent.getVersion());
				item = itemLookop.get(ItemId.fromKey(itemKey));
			}
			listEntry.setItem(item);
			listEntry.setInfo(info);
			listEntry.setDeleteFunction(deleteFunction);
			listEntry.setEditFunction(editFunction);
			connectorManagementItemList.addListItem(info, listEntry);
		}

	}

	@Override
	protected Label getNoResultsTitle(SectionInfo info, ConnectorManagementSearchEvent searchEvent,
		ConnectorManagmentSearchResultEvent resultsEvent)
	{
		if( querySection.getConnectorList().getSelectedValue(info) == null )
		{
			return LABEL_SELECT_CONNECTOR;
		}
		return super.getNoResultsTitle(info, searchEvent, resultsEvent);
	}

	@Override
	protected Label getSuggestions(SectionInfo info, ConnectorManagementSearchEvent searchEvent,
		ConnectorManagmentSearchResultEvent resultsEvent)
	{
		if( querySection.getConnectorList().getSelectedValue(info) == null )
		{
			return null;
		}
		return super.getSuggestions(info, searchEvent, resultsEvent);
	}

	@Override
	protected ConnectorManagmentSearchResultEvent createResultsEvent(SectionInfo info,
		ConnectorManagementSearchEvent connectorSearchEvent)
	{
		try
		{
			Connector connector = connectorSearchEvent.getConnector();
			List<ConnectorContent> allUsage = new ArrayList<ConnectorContent>();

			int offset = connectorSearchEvent.getOffset();
			int count = connectorSearchEvent.getCount();

			ConnectorContentSearch search = connectorSearchEvent.getSearch();
			ExternalContentSortType sort = search.getSort();

			SearchResults<ConnectorContent> findAllUsages = repositoryService.findAllUsages(connector,
				CurrentUser.getUsername(), connectorSearchEvent.getQuery(), search.getCourse(), search.getFolder(),
				search.isArchived(), offset, count, sort, search.isReverse());

			int unfilteredCount = 0;
			if( connectorSearchEvent.isUserFiltered() )
			{
				unfilteredCount = repositoryService.getUnfilteredAllUsagesCount(connector, CurrentUser.getUsername(),
					connectorSearchEvent.getQuery(), true);
			}

			allUsage.addAll(findAllUsages.getResults());

			BaseCompoundQuery mainQuery = new BaseCompoundQuery();
			mainQuery.setBooleanType(false);

			for( ConnectorContent content : allUsage )
			{
				int version = content.getVersion();
				String uuid = content.getUuid();
				if( uuid != null )
				{
					if( version == 0 )
					{
						// FIXME perhaps not very efficient...
						version = itemService.getLatestVersion(uuid);
						content.setVersion(version);
					}

					BaseCompoundQuery query = new BaseCompoundQuery();
					query.addQuery(new NodeInQuery(uuid, false, Collections.singleton(FreeTextQuery.FIELD_UUID), null));
					query.addQuery(new NodeInQuery(String.valueOf(version), false,
						Collections.singleton(FreeTextQuery.FIELD_VERSION), null));
					mainQuery.addQuery(query);
				}
			}

			DefaultSearch rawSearch = new DefaultSearch();
			rawSearch.setPrivilege(null);
			rawSearch.setFreeTextQuery(mainQuery.getFullFreeTextQuery());
			FreetextSearchResults<FreetextResult> searchResults = freeTextService.search(rawSearch, 0, count);

			SimpleSearchResults<ConnectorContent> simpleResults = new SimpleSearchResults<ConnectorContent>(allUsage,
				allUsage.size(), offset, findAllUsages.getAvailable());

			return new ConnectorManagmentSearchResultEvent(connectorSearchEvent, simpleResults, searchResults,
				unfilteredCount - findAllUsages.getAvailable(), connector);

		}
		catch( LmsUserNotFoundException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public ConnectorManagementSearchEvent createSearchEvent(SectionInfo info)
	{
		Connector connector = querySection.getConnector(info);
		if( connector != null )
		{
			ConnectorContentSearch search = new ConnectorContentSearch();
			search.setArchived(true);
			return new ConnectorManagementSearchEvent(search, connector);
		}
		return null;
	}

	@Override
	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		super.addAjaxUpdateDivs(tree, ajaxList);
		ajaxList.add(AbstractBulkSelectionSection.DIVID_SELECTBOX);
	}

	@Override
	public ConnectorManagementItemList getItemList(SectionInfo info)
	{
		return connectorManagementItemList;
	}

}
