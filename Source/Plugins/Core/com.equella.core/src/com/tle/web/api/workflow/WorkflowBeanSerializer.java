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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.workflow.WorkflowEditorImpl.WorkflowEditorFactory;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;
import com.tle.web.api.workflow.interfaces.beans.WorkflowNodeBean;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class WorkflowBeanSerializer extends AbstractEquellaBaseEntitySerializer<Workflow, WorkflowBean, WorkflowEditor>
{
	@Inject
	private WorkflowService workflowService;
	@Inject
	private WorkflowEditorFactory editorFactory;

	@Override
	protected WorkflowBean createBean()
	{
		return new WorkflowBean();
	}

	@Override
	protected Workflow createEntity()
	{
		return new Workflow();
	}

	@Override
	protected void copyCustomFields(Workflow entity, WorkflowBean bean, Object data)
	{
		super.copyCustomFields(entity, bean, data);

		final WorkflowNode root = entity.getRoot();
		if( root != null )
		{
			final WorkflowNodeBean rootBean = convertToBean(root);
			bean.setRoot(rootBean);
			convertToBeans(root.getChildren(), rootBean);
		}
		else
		{
			// Ummm... why??
			bean.setRoot(new WorkflowNodeBean());
		}
	}

	private WorkflowNodeBean convertToBean(WorkflowNode node)
	{
		final WorkflowNodeBean nodeBean = new WorkflowNodeBean();
		final char type = node.getType();
		nodeBean.setType(type);
		nodeBean.setUuid(node.getUuid());
		nodeBean.setName(getI18NString(node.getName(), node.getUuid()));
		nodeBean.setNameStrings(getI18NStrings(node.getName(), true));
		nodeBean.setRejectPoint(node.isRejectPoint());

		switch( type )
		{
			case WorkflowNode.SERIAL_TYPE:
				// No-op
				break;

			case WorkflowNode.PARALLEL_TYPE:
				// No-op
				break;

			case WorkflowNode.DECISION_TYPE:
				final DecisionNode decNode = (DecisionNode) node;
				nodeBean.setScript(decNode.getScript());
				final String collectionUuid = decNode.getCollectionUuid();
				if( collectionUuid != null )
				{
					nodeBean.setCollection(new BaseEntityReference(collectionUuid));
				}
				break;

			case WorkflowNode.ITEM_TYPE:
				final WorkflowItem itemNode = (WorkflowItem) node;
				nodeBean.setDescription(getI18NString(itemNode.getDescription(), null));
				nodeBean.setDescriptionStrings(getI18NStrings(itemNode.getDescription(), false));
				nodeBean.setActionDays(itemNode.getActionDays());
				nodeBean.setAllowEditing(itemNode.isAllowEditing());
				nodeBean.setAutoAction(itemNode.getAutoAction().toString().toLowerCase());
				nodeBean.setDueDatePath(itemNode.getDueDatePath());
				nodeBean.setEscalate(itemNode.isEscalate());
				nodeBean.setEscalationdays(itemNode.getEscalationdays());
				nodeBean.setGroups(itemNode.getGroups());
				nodeBean.setMoveLive(itemNode.getMovelive().toString().toLowerCase());
				nodeBean.setPriority(itemNode.getPriority());
				nodeBean.setRoles(itemNode.getRoles());
				nodeBean.setUnanimousacceptance(itemNode.isUnanimousacceptance());
				nodeBean.setUserPath(itemNode.getUserPath());
				nodeBean.setUsers(itemNode.getUsers());
				break;
		}
		return nodeBean;
	}

	private void convertToBeans(List<WorkflowNode> nodes, WorkflowNodeBean beanParent)
	{
		final List<WorkflowNodeBean> nodeBeans = new ArrayList<>();
		for( WorkflowNode node : nodes )
		{
			nodeBeans.add(convertToBean(node));
		}
		beanParent.setNodes(nodeBeans);
	}

	@Override
	protected WorkflowEditor createExistingEditor(Workflow entity, String stagingUuid, String lockId, boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected WorkflowEditor createNewEditor(Workflow entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected AbstractEntityService<?, Workflow> getEntityService()
	{
		return workflowService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.WORKFLOW;
	}
}
