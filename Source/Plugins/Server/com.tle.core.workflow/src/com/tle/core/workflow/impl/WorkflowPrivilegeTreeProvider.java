package com.tle.core.workflow.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.core.services.entity.WorkflowService;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class WorkflowPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Workflow>
{
	@Inject
	public WorkflowPrivilegeTreeProvider(WorkflowService workflowService)
	{
		super(workflowService, Node.ALL_WORKFLOWS, ResourcesService.getResourceHelper(
			WorkflowPrivilegeTreeProvider.class).key("securitytree.allworkflows"), Node.WORKFLOW, ResourcesService
			.getResourceHelper(WorkflowPrivilegeTreeProvider.class).key("securitytree.targetallworkflows"));
	}

	@Override
	protected Workflow createEntity()
	{
		return new Workflow();
	}
}
