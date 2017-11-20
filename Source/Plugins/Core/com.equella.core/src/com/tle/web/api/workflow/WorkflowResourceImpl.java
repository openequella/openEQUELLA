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

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.workflow.Workflow;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.service.WorkflowService;
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
	protected Class<?> getResourceClass()
	{
		return WorkflowResource.class;
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_WORKFLOW;
	}
}
