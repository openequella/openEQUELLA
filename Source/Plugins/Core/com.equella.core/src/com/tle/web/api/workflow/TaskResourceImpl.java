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

package com.tle.web.api.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.interfaces.CsvList;
import com.tle.common.interfaces.equella.BundleString;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.common.workflow.TaskFilterCount;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.api.TaskFilterCountBean;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.core.services.item.TaskResult;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.item.tasks.interfaces.beans.TaskBean;
import com.tle.web.api.item.tasks.interfaces.beans.TaskStatusBean;
import com.tle.web.sections.SectionsController;
import com.tle.web.workflow.portal.TaskListFilters;
import com.tle.web.workflow.portal.TaskListSubsearch;
import com.tle.web.workflow.tasks.RootTaskListSection;

/**
 * @author Dustin
 */
@SuppressWarnings("nls")
@Bind(EquellaTaskResource.class)
@Singleton
public class TaskResourceImpl implements EquellaTaskResource
{
	private static final String REL_WEB_MODERATE = "web-moderate";
	private static final String DEFAULT_FILTER = "all";
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private WorkflowService workflowService;
	@Inject
	private TaskListFilters taskListFilters;

	// TODO: this is sub ottstimal
	@Inject
	private SectionsController sectionsController;

	private static final Map<String, String> FILTER_MAPPING = ImmutableMap.of(DEFAULT_FILTER, "taskall", "assignedme",
		"taskme", "assignedothers", "taskothers", "assignednone", "tasknoone", "mustmoderate", "taskmust");

	@Override
	public Response tasksSearch(UriInfo uriInfo, String filtering, String q, int start, int length, CsvList collections,
		String order, String reverse)
	{
		final SearchBean<TaskStatusBean> result = new SearchBean<TaskStatusBean>();
		final List<TaskStatusBean> resultsToReturn = Lists.newArrayList();

		// sanitise parameters
		final SortField orderType = getOrderType(order);

		int l = (length < 0 ? 0 : length);
		l = (l > 100 ? 100 : l);

		final String filterId = getMappedFilter(filtering);

		TaskListSubsearch filter = taskListFilters.getFilterForIdentifier(filterId);
		DefaultSearch filterSearch = filter.getSearch();

		DefaultSearch search = createSearch(q, collections == null ? null : CsvList.asList(collections), orderType,
			filterSearch);

		// Did we ask for any actual results to be returned (should there are
		// any)
		if( l != 0 )
		{
			final int offset = (start < 0 ? 0 : start);

			final FreetextSearchResults<TaskResult> searchResults = freetextService.search(search, offset, l);

			final List<TaskResult> actualSearchResults = Lists.newArrayList();

			// reverse parameter set?
			final boolean reverseOrder = Utils.parseLooseBool(reverse, false);

			for( int i = 0; i < searchResults.getCount(); i++ )
			{
				int index = reverseOrder ? (searchResults.getCount() - 1) - i : i;
				actualSearchResults.add(searchResults.getResultData(index));
			}

			for( TaskResult taskResult : actualSearchResults )
			{
				TaskStatusBean taskStatusBean = new TaskStatusBean();

				ItemBean itemBean = new ItemBean();
				ItemIdKey itemId = taskResult.getItemIdKey();
				itemBean.setUuid(itemId.getUuid());
				itemBean.setVersion(itemId.getVersion());
				itemLinkService.addLinks(itemBean);
				taskStatusBean.setItem(itemBean);

				String taskUuid = taskResult.getTaskId();
				ItemTaskId itemTaskId = new ItemTaskId(itemId, taskUuid);
				WorkflowItemStatus status = workflowService.getIncompleteStatus(itemTaskId);
				taskStatusBean.setTask(createTaskBean((WorkflowItem) status.getNode(), taskUuid));
				List<UserBean> accepted = Lists.newArrayList();
				Set<String> users = status.getAcceptedUsers();
				for( String user : users )
				{
					if( !Check.isEmpty(user) )
					{
						accepted.add(new UserBean(user));
					}
				}
				taskStatusBean.setAcceptedUsers(accepted);
				taskStatusBean.setAssignedTo(userOrNull(status.getAssignedTo()));
				taskStatusBean.setDueDate(status.getDateDue());
				taskStatusBean.setOverdue(status.isOverdue());
				taskStatusBean.setStartDate(status.getStarted());
				Map<String, String> links = Maps.newHashMap();
				// TODO: this is sub-ottstimal
				links.put(REL_WEB_MODERATE,
					RootTaskListSection.createModerateBookmark(sectionsController, itemTaskId).getHref());
				taskStatusBean.set("links", links);
				resultsToReturn.add(taskStatusBean);
			}
			result.setStart(searchResults.getOffset());
			result.setLength(searchResults.getCount());
			result.setAvailable(searchResults.getAvailable());
		}
		else
		{
			// requesting length of 0 - so all we want is the count
			int[] singleSearchCount = freetextService.countsFromFilters(Collections.singletonList(search));
			result.setAvailable(singleSearchCount[0]);
			result.setStart(0);
			result.setLength(0);
		}

		result.setResults(resultsToReturn);
		return Response.ok(result).build();
	}

	@Override
	public Response getTaskFilters(boolean ignoreZero, boolean includeCounts)
	{
		final List<TaskFilterCountBean> resultBeans = new ArrayList<TaskFilterCountBean>();
		if( !includeCounts && !ignoreZero )
		{
			final Set<String> filterNamesSet = taskListFilters.getFilterNamesSet();
			for( String filterName : filterNamesSet )
			{
				final TaskFilterCountBean fcb = new TaskFilterCountBean(filterName, null);
				resultBeans.add(fcb);
			}
		}
		else
		{
			// 2nd parameter true -skip the internal EQUELLA hrefs for the filter
			// counts
			final List<TaskFilterCount> filterCounts = taskListFilters.getFilterCounts(ignoreZero, true);
			for( TaskFilterCount taskFilterCount : filterCounts )
			{
				resultBeans.add(new TaskFilterCountBean(taskFilterCount));
			}
		}

		final SearchBean<TaskFilterCountBean> resultBean = new SearchBean<TaskFilterCountBean>();
		resultBean.setStart(0);
		resultBean.setResults(resultBeans);
		resultBean.setLength(resultBeans.size());
		resultBean.setAvailable(resultBeans.size());
		return Response.ok(resultBean).build();
	}

	//
	//	// TODO - Aaron sayeth thusly:
	//	// "I wouldn't have this. I'd have a top level endpoint called task-filters which returned the whole objects (some bean based around the TaskListSubsearch)"
	//	// https://devops-tools.pearson.com/stash/projects/EQ/repos/equella---master/pull-requests/8/overview?commentId=5539
	//	@Override
	//	public Set<String> getTaskFilterNames()
	//	{
	//		return;
	//	}

	private UserBean userOrNull(String user)
	{
		if( !Check.isEmpty(user) )
		{
			return new UserBean(user);
		}
		return null;
	}

	private TaskBean createTaskBean(WorkflowItem node, String taskUuid)
	{
		TaskBean taskBean = new TaskBean();
		taskBean.setUuid(taskUuid);
		taskBean.setName(new BundleString(node.getName(), taskUuid));
		taskBean.setDescription(new BundleString(node.getDescription()));
		taskBean.setPriority(node.getPriority());
		taskBean.setUnanimous(node.isUnanimousacceptance());
		taskBean.setWorkflow(new BaseEntityReference(node.getWorkflow().getUuid()));
		return taskBean;
	}

	private String getMappedFilter(String subsearch)
	{
		if( subsearch == null || !FILTER_MAPPING.containsKey(subsearch) )
		{
			subsearch = DEFAULT_FILTER;
		}
		return FILTER_MAPPING.get(subsearch);
	}

	private SortField getOrderType(String order)
	{
		if( order != null )
		{
			// allowed values are priority, duedate, name, waiting
			if( order.equals("priority") )
			{
				return new SortField(TasksIndexer.FIELD_PRIORITY, true, Type.LONG);
			}
			else if( order.equals("duedate") )
			{
				return new SortField(TasksIndexer.FIELD_DUEDATE, false, Type.LONG);
			}
			else if( order.equals("waiting") )
			{
				return new SortField(TasksIndexer.FIELD_STARTED, false, Type.LONG);
			}
			else if( order.equals("name") )
			{
				return new SortField(FreeTextQuery.FIELD_NAME, false, Type.STRING);
			}
		}
		return new SortField(TasksIndexer.FIELD_PRIORITY, true, Type.LONG);
	}

	private DefaultSearch createSearch(String freetext, Collection<String> collectionUuids, SortField orderType,
		DefaultSearch search)
	{
		search.setCollectionUuids(collectionUuids);
		search.setSortFields(orderType);
		search.setQuery(freetext);
		return search;
	}
}
