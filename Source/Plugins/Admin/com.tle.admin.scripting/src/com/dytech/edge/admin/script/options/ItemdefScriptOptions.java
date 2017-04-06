/*
 * Created on Jul 5, 2005
 */
package com.dytech.edge.admin.script.options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.exceptions.ApplicationException;
import com.dytech.edge.exceptions.NotFoundException;
import com.tle.common.NameValue;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;

public class ItemdefScriptOptions implements ScriptOptions
{
	private static final Log LOGGER = LogFactory.getLog(ItemdefScriptOptions.class);

	final List<NameValue> workflowSteps;
	private Workflow workflow;
	private RemoteWorkflowService workflowService;

	public ItemdefScriptOptions(ClientService services)
	{
		workflowSteps = new ArrayList<NameValue>();
		workflowService = services.getService(RemoteWorkflowService.class);
	}

	@Override
	public boolean hasItemStatus()
	{
		return true;
	}

	@Override
	public boolean restrictItemStatusForModeration()
	{
		return false;
	}

	@Override
	public boolean hasUserIsModerator()
	{
		return true;
	}

	@Override
	public boolean hasWorkflow()
	{
		return workflow != null;
	}

	@Override
	public List<NameValue> getWorkflowSteps()
	{
		return workflowSteps;
	}

	@Override
	public String getWorkflowStepName(final String uuid)
	{
		final WorkflowItem[] items = new WorkflowItem[1];
		ProcessNode node = new ProcessNode()
		{
			@Override
			public boolean processItem(WorkflowItem item)
			{
				if( item.getUuid().equals(uuid) )
				{
					items[0] = item;
					return true;
				}
				return false;
			}
		};
		traverse(workflow.getRoot(), node);
		WorkflowItem item = items[0];
		String name = null;
		if( item == null )
		{
			name = "Unknown";
		}
		else
		{
			name = CurrentLocale.get(item.getDisplayName());
		}
		return name;
	}

	public void setCurrentWorkflow(long selectedWorkflowId) throws ApplicationException
	{
		workflowSteps.clear();
		workflow = null;

		if( selectedWorkflowId != 0 )
		{
			try
			{
				workflow = workflowService.get(selectedWorkflowId);
				WorkflowNode root = workflow.getRoot();
				traverse(root, new ProcessNode()
				{
					@Override
					public boolean processItem(WorkflowItem item)
					{
						workflowSteps.add(new NameValue(CurrentLocale.get(item.getDisplayName()), item.getUuid()));
						return false;
					}
				});
			}
			catch( NotFoundException e )
			{
				LOGGER.info("Workflow '" + selectedWorkflowId + "' not found");
			}
		}
	}

	private interface ProcessNode
	{
		boolean processItem(WorkflowItem item);
	}

	private boolean traverse(WorkflowNode root, ProcessNode p)
	{
		Iterator<WorkflowNode> i = root.iterateChildren();
		while( i.hasNext() )
		{
			WorkflowNode node = i.next();
			if( node.isLeafNode() )
			{
				WorkflowItem item = (WorkflowItem) node;
				if( p.processItem(item) )
				{
					return true;
				}
			}
			else
			{
				WorkflowTreeNode tnode = (WorkflowTreeNode) node;
				if( traverse(tnode, p) )
				{
					return true;
				}
			}
		}
		return false;
	}
}
