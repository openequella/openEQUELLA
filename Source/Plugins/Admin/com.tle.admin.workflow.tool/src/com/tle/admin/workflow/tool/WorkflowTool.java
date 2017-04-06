package com.tle.admin.workflow.tool;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.admin.workflow.WorkflowEditor;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.Workflow;
import com.tle.core.remoting.RemoteAbstractEntityService;

public class WorkflowTool extends BaseEntityTool<Workflow>
{
	public WorkflowTool() throws Exception
	{
		super(Workflow.class, RemoteWorkflowService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<Workflow> getService(ClientService client)
	{
		return client.getService(RemoteWorkflowService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "workflow";
	}

	@Override
	protected BaseEntityEditor<Workflow> createEditor(boolean readonly)
	{
		return new WorkflowEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.workflowtool.name");
	}
}
