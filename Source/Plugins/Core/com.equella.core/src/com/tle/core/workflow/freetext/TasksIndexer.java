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

package com.tle.core.workflow.freetext;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.hibernate.Hibernate;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemTask;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.searching.DateFilter;
import com.tle.common.searching.DateFilter.Format;
import com.tle.common.searching.Search;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.freetext.index.ItemIndex;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.freetext.IndexedItem;

@SuppressWarnings("nls")
@Bind
public class TasksIndexer extends AbstractIndexingExtension
{
	public static final String FIELD_LASTACTION = "lastmodaction";
	public static final String FIELD_STARTWORKFLOW = "startmoddate";
	public static final String FIELD_PRIORITY = "task_priority";
	public static final String FIELD_DUEDATE = "task_duedate";
	public static final String FIELD_STARTED = "task_started";
	public static final String FIELD_WORKFLOW = "task_workflow";
	public static final String FIELD_MODUSER = "task_moduser";

	@SuppressWarnings("unused")
	private static final Log LOGGER = LogFactory.getLog(TasksIndexer.class);

	private static final String MODERATE_ITEM = "MODERATE_ITEM"; //$NON-NLS-1$
	private static final String DISCOVER_ITEM = "DISCOVER_ITEM"; //$NON-NLS-1$

	@Inject
	private WorkflowService workflowService;
	@Inject
	private RunAsInstitution runAs;

	private void addTasks(IndexedItem indexedItem)
	{
		Item item = indexedItem.getItem();
		if( !item.isModerating() )
		{
			return;
		}

		ModerationStatus modstatus = item.getModeration();
		if( modstatus == null )
		{
			return;
		}

		Workflow workflow = item.getItemDefinition().getWorkflow();
		if( workflow == null )
		{
			return;
		}

		Document itemdoc = indexedItem.getItemdoc();
		itemdoc.add(indexed(FIELD_WORKFLOW, workflow.getUuid()));

		for( WorkflowNodeStatus nodestatus : modstatus.getStatuses() )
		{
			if( nodestatus.getNode().getType() == WorkflowNode.ITEM_TYPE
				&& nodestatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				WorkflowItem task = (WorkflowItem) nodestatus.getNode();
				if( task != null )
				{
					String taskId = task.getUuid();
					ItemTask itemTask = new ItemTask(item, taskId);
					Document doc = new Document();
					addAllFields(doc, indexedItem.getBasicFields());

					WorkflowItemStatus taskstatus = (WorkflowItemStatus) nodestatus;

					boolean unan = !workflowService.isOptionalTask(task, indexedItem.getItemxml());

					Field taskField = keyword(FreeTextQuery.FIELD_WORKFLOW_TASKID, taskId);
					itemdoc.add(taskField);
					doc.add(taskField);
					doc.add(indexed(FreeTextQuery.FIELD_WORKFLOW_UNANIMOUS, Boolean.toString(unan)));
					String assignedTo = taskstatus.getAssignedTo();
					if( assignedTo == null )
					{
						assignedTo = ""; //$NON-NLS-1$
					}
					doc.add(indexed(FreeTextQuery.FIELD_WORKFLOW_ASSIGNEDTO, assignedTo));
					doc.add(
						indexed(FIELD_PRIORITY, Integer.toString(((WorkflowItem) taskstatus.getNode()).getPriority())));

					addDateField(doc, FIELD_DUEDATE, taskstatus.getDateDue(), DateFilter.Format.LONG, Long.MAX_VALUE);
					addDateField(doc, FIELD_STARTED, taskstatus.getStarted(), DateFilter.Format.LONG, null);

					for( String userid : taskstatus.getAcceptedUsers() )
					{
						doc.add(indexed(FreeTextQuery.FIELD_WORKFLOW_ACCEPTED, userid));
					}
					Set<String> moderatorList = workflowService.getAllModeratorUserIDs(indexedItem.getItemxml(), task);
					for( String userid : moderatorList )
					{
						doc.add(indexed(FIELD_MODUSER, userid));
					}

					addAllFields(doc,
						indexedItem.queryACLEntries(itemTask, MODERATE_ITEM, ItemIndex.convertStdPriv(MODERATE_ITEM)));

					doc.add(indexed(FIELD_WORKFLOW, workflow.getUuid()));
					indexedItem.getDocumentsForIndex(Search.INDEX_TASK).add(doc);
				}
			}
			else if( nodestatus.getNode().getType() == WorkflowNode.SCRIPT_TYPE
				&& nodestatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
			{
				ScriptNode task = (ScriptNode) nodestatus.getNode();
				if( task != null )
				{
					String taskId = task.getUuid();
					Document doc = new Document();
					addAllFields(doc, indexedItem.getBasicFields());

					Field taskField = keyword(FreeTextQuery.FIELD_WORKFLOW_TASKID, taskId);
					itemdoc.add(taskField);
					doc.add(taskField);

					doc.add(indexed(FIELD_WORKFLOW, workflow.getUuid()));
					indexedItem.getDocumentsForIndex(Search.INDEX_SCRIPT_TASK).add(doc);
				}
			}
		}
	}

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		addTasks(indexedItem);
		addModerationFields(indexedItem);
	}

	private void addModerationFields(IndexedItem indexedItem)
	{
		Document doc = indexedItem.getItemdoc();
		ModerationStatus modStatus = indexedItem.getItem().getModeration();
		if( modStatus != null )
		{
			addDateField(doc, FIELD_LASTACTION, modStatus.getLastAction(), Format.LONG, null);
			addDateField(doc, FIELD_STARTWORKFLOW, modStatus.getStart(), Format.LONG, null);
		}
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// nothing
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		for( final IndexedItem indexedItem : items )
		{
			try
			{
				runAs.executeAsSystem(indexedItem.getInstitution(), new Callable<Void>()
				{
					@Override
					public Void call()
					{
						indexedItem.prepareACLEntries(indexedItem, DISCOVER_ITEM);

						Item item = indexedItem.getItem();
						if( !item.isModerating() )
						{
							return null;
						}
						ModerationStatus modstatus = item.getModeration();
						if( modstatus == null )
						{
							return null;
						}

						Set<WorkflowNodeStatus> statuses = modstatus.getStatuses();
						// The implementation of ModerationStatus.getStatuses()
						// never returns null but, to be sure ...
						if( statuses == null ) // NOSONAR
						{
							return null;
						}

						Workflow workflow = item.getItemDefinition().getWorkflow();
						if( workflow == null )
						{
							return null;
						}

						for( WorkflowNodeStatus nodestatus : statuses )
						{
							if( nodestatus.getNode().getType() == WorkflowNode.ITEM_TYPE
								&& nodestatus.getStatus() == WorkflowNodeStatus.INCOMPLETE )
							{
								WorkflowItem task = (WorkflowItem) nodestatus.getNode();
								if( task != null )
								{
									Hibernate.initialize(task.getUsers());
									Hibernate.initialize(task.getGroups());
									Hibernate.initialize(task.getRoles());
									Hibernate.initialize(((WorkflowItemStatus) nodestatus).getAcceptedUsers());
								}
							}
						}
						return null;
					}
				});
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}
}
