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
