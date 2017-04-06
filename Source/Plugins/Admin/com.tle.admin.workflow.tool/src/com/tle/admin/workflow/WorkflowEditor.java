package com.tle.admin.workflow;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.workflow.Workflow;

@SuppressWarnings("nls")
public class WorkflowEditor extends BaseEntityEditor<Workflow>
{
	public WorkflowEditor(BaseEntityTool<Workflow> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected AbstractDetailsTab<Workflow> constructDetailsTab()
	{
		return new StepsTab();
	}

	@Override
	protected List<BaseEntityTab<Workflow>> getTabs()
	{
		ArrayList<BaseEntityTab<Workflow>> tabs = new ArrayList<BaseEntityTab<Workflow>>();
		tabs.add((StepsTab) detailsTab);
		tabs.add(new AccessControlTab<Workflow>(Node.WORKFLOW));
		return tabs;
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.workflow.workfloweditor.entname");
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.workflow.workfloweditor.title");
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.workflow.workfloweditor.docname");
	}
}
