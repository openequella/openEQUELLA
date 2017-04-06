package com.tle.web.api.workflow;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.WorkflowService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.workflow.interfaces.WorkflowResource;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;

/**
 * @author Aaron
 */
@Bind(WorkflowResource.class)
@Singleton
public class WorkflowResourceImpl extends AbstractBaseEntityResource<Workflow, BaseEntitySecurityBean, WorkflowBean>
	implements
		WorkflowResource
{
	@Inject
	private WorkflowService workflowService;
	@Inject
	private WorkflowBeanSerializer serializer;

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_WORKFLOWS};
	}

	@Override
	protected BaseEntitySecurityBean createAllSecurityBean()
	{
		return new BaseEntitySecurityBean();
	}

	@Override
	protected AbstractEntityService<?, Workflow> getEntityService()
	{
		return workflowService;
	}

	@Override
	protected BaseEntitySerializer<Workflow, WorkflowBean> getSerializer()
	{
		return serializer;
	}

	@Override
	protected Class<WorkflowResource> getResourceClass()
	{
		return WorkflowResource.class;
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_WORKFLOW;
	}
}
