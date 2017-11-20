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

package com.tle.web.viewitem.viewer;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.edge.gui.workflow.WorkflowVisualiser;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Format;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;

@SuppressWarnings("nls")
public class WorkflowFlowchartSection extends AbstractPrototypeSection<Object> implements ViewItemViewer
{
	private static final String IMG_FILENAME = "statusimage.png";

	private static final Logger LOGGER = Logger.getLogger(WorkflowFlowchartSection.class);

	private static final Color COMPLETE_TASKS = new Color(200, 235, 200);
	private static final Color INCOMPLETE_TASKS = new Color(255, 150, 150);
	private static final Color CURRENT_TASK = new Color(245, 245, 190);

	@Inject
	private UserService userService;
	@TreeLookup
	private RootItemFileSection rootSection;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addViewerMapping(Type.FULL, this, IMG_FILENAME);
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return DISCOVER_AND_VIEW_PRIVS;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final Item item = (Item) resource.getViewableItem().getItem();
		final Set<String> complete = new HashSet<String>();
		final Map<String, String> incomplete = new HashMap<String, String>();
		final WorkflowVisualiser visualiser = new WorkflowVisualiser(item.getItemDefinition().getWorkflow().getRoot(),
			null);

		// Gather a bit of information about the current moderation status
		gatherTasks(item, complete, incomplete);

		// Highlight the current task
		ItemKey itemId = resource.getViewableItem().getItemId();
		if( itemId instanceof ItemTaskId )
		{
			String taskId = ((ItemTaskId) itemId).getTaskId();
			visualiser.setColourForNode(CURRENT_TASK, taskId);
			visualiser.addColourLegend(CURRENT_TASK, CurrentLocale.get("actions.viewitemaction.moderation"));
			complete.remove(taskId);
			incomplete.remove(taskId);
		}

		highlightTasks(visualiser, complete, incomplete);

		HttpServletResponse response = info.getResponse();
		try
		{
			response.setContentType("image/png");
			visualiser.writeToPng(response.getOutputStream());
		}
		catch( Exception ex )
		{
			LOGGER.fatal("Error generating workflow status image", ex);
			throw new SectionsRuntimeException(ex);
		}
		info.setRendered();
		return null;
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	private void highlightTasks(final WorkflowVisualiser visualiser, final Set<String> complete,
		final Map<String, String> incomplete)
	{
		if( !complete.isEmpty() )
		{
			visualiser.setColourForNodes(COMPLETE_TASKS, complete);
			visualiser.addColourLegend(COMPLETE_TASKS, CurrentLocale.get("actions.viewitemaction.completed"));
		}

		if( !incomplete.isEmpty() )
		{
			visualiser.setColourForNodes(INCOMPLETE_TASKS, incomplete.keySet());
			visualiser.addColourLegend(INCOMPLETE_TASKS, CurrentLocale.get("actions.viewitemaction.incomplete"));
		}

		// Show message for moderators at incomplete tasks
		for( Map.Entry<String, String> entry : incomplete.entrySet() )
		{
			String userID = entry.getValue();
			if( userID != null )
			{
				UserBean user = userService.getInformationForUser(entry.getValue());
				String text = CurrentLocale.get("actions.viewitemaction.assigned", Format.format(user));
				visualiser.addMessageToNode(entry.getKey(), text);
			}
		}
	}

	private void gatherTasks(Item item, Set<String> complete, Map<String, String> incomplete)
	{
		for( WorkflowNodeStatus wns : item.getModeration().getStatuses() )
		{
			if( wns instanceof WorkflowItemStatus )
			{
				WorkflowItemStatus wis = (WorkflowItemStatus) wns;
				if( wis.getStatus() == WorkflowNodeStatus.COMPLETE )
				{
					complete.add(wis.getNode().getUuid());
				}
				else if( wis.getStatus() == WorkflowNodeStatus.INCOMPLETE )
				{
					incomplete.put(wis.getNode().getUuid(), wis.getAssignedTo());
				}
			}
			else if( wns.getNode() instanceof ScriptNode )
			{
				if( wns.getStatus() == WorkflowNodeStatus.COMPLETE )
				{
					complete.add(wns.getNode().getUuid());
				}
				else if( wns.getStatus() == WorkflowNodeStatus.INCOMPLETE )
				{
					incomplete.put(wns.getNode().getUuid(), null);
				}
			}
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "statusimage";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
