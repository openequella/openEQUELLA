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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.interfaces.BaseEntityReference;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowItem.AutoAction;
import com.tle.common.workflow.node.WorkflowItem.MoveLive;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.event.ItemOperationEvent;
import com.tle.core.item.standard.filter.workflow.ResetFilter;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.core.workflow.event.WorkflowChangeEvent;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.core.workflow.standard.service.impl.WorkflowStandardServiceImpl.CheckForStepsLocator;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;
import com.tle.web.api.workflow.interfaces.beans.WorkflowNodeBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class WorkflowEditorImpl extends AbstractBaseEntityEditor<Workflow, WorkflowBean> implements WorkflowEditor
{
	@Inject
	private WorkflowService workflowService;
	@Inject
	private WorkflowDao workflowDao;

	@Nullable
	private Map<String, WorkflowNode> oldNodes;
	private final Set<WorkflowNode> changedNodes = new HashSet<WorkflowNode>();
	private final Set<WorkflowNode> resaveNodes = new HashSet<WorkflowNode>();

	@AssistedInject
	public WorkflowEditorImpl(@Assisted Workflow entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public WorkflowEditorImpl(@Assisted Workflow entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(WorkflowBean bean)
	{
		super.copyCustomFields(bean);
		if( editing )
		{
			oldNodes = entity.getAllNodesAsMap();
		}

		entity.setMovelive(bean.isMoveLive());

		Set<WorkflowNode> nodes = entity.getNodes();
		if( nodes == null )
		{
			nodes = new HashSet<>();
			entity.setNodes(nodes);
		}

		final WorkflowNodeBean root = bean.getRoot();
		if( root != null )
		{
			makeNodes(nodes, root, null, 0);
		}

		final Set<WorkflowNode> deletedNodes = new HashSet<WorkflowNode>();
		final Map<String, WorkflowNode> newNodes = entity.getAllNodesAsMap();
		if( oldNodes != null )
		{
			for( Entry<String, WorkflowNode> oldNodeEntry : oldNodes.entrySet() )
			{
				if( !newNodes.containsKey(oldNodeEntry.getKey()) )
				{
					deletedNodes.add(oldNodeEntry.getValue());
				}
			}
		}
		if( !deletedNodes.isEmpty() )
		{
			publishEvent(new WorkflowChangeEvent(entity.getId(), deletedNodes, true));
			workflowDao.markForReset(deletedNodes);
			publishEventAfterCommit(new ItemOperationEvent(new ClassBeanLocator<ResetFilter>(ResetFilter.class)));
		}
	}

	private void makeNodes(Set<WorkflowNode> nodes, WorkflowNodeBean nodeBean, @Nullable WorkflowNode parent,
		int nodeIndex)
	{
		final WorkflowNode node;
		boolean changed = false;
		boolean resave = false;

		final WorkflowNode existing = (oldNodes == null ? null : oldNodes.get(nodeBean.getUuid()));
		switch( nodeBean.getType() )
		{
			case WorkflowNode.SERIAL_TYPE:
				if( existing != null )
				{
					node = existing;
				}
				else
				{
					node = new SerialNode();
				}
				break;

			case WorkflowNode.PARALLEL_TYPE:
				if( existing != null )
				{
					node = existing;
				}
				else
				{
					node = new ParallelNode();
				}
				break;

			case WorkflowNode.DECISION_TYPE:
				final DecisionNode decnode;
				if( existing != null )
				{
					decnode = (DecisionNode) existing;
				}
				else
				{
					decnode = new DecisionNode();
				}
				decnode.setScript(nodeBean.getScript());
				final BaseEntityReference collection = nodeBean.getCollection();
				if( collection != null )
				{
					decnode.setCollectionUuid(collection.getUuid());
				}
				node = decnode;
				break;

			case WorkflowNode.ITEM_TYPE:
				final WorkflowItem winode;
				if( existing != null )
				{
					winode = (WorkflowItem) existing;
					changed = winode.isUnanimousacceptance() != nodeBean.isUnanimousacceptance();
					resave = !Objects.equals(winode.getUserPath(), nodeBean.getUserPath());
					resave |= winode.isAllowEditing() != nodeBean.isAllowEditing()
						&& !Check.isEmpty(nodeBean.getUserPath());
					resave |= (winode.getPriority() != nodeBean.getPriority());
				}
				else
				{
					winode = new WorkflowItem();
				}
				winode.setUnanimousacceptance(nodeBean.isUnanimousacceptance());
				winode.setEscalate(nodeBean.isEscalate());
				winode.setEscalationdays(nodeBean.getEscalationdays());
				winode.setAllowEditing(nodeBean.isAllowEditing());
				final String moveLive = nodeBean.getMoveLive();
				if( moveLive != null )
				{
					winode.setMovelive(MoveLive.valueOf(moveLive.toUpperCase()));
				}
				final String autoAction = nodeBean.getAutoAction();
				if( autoAction != null )
				{
					winode.setAutoAction(AutoAction.valueOf(autoAction.toUpperCase()));
				}
				winode.setPriority(nodeBean.getPriority());
				winode.setActionDays(nodeBean.getActionDays());
				winode.setUserPath(nodeBean.getUserPath());
				winode.setDueDatePath(nodeBean.getDueDatePath());
				winode.setDescription(
					getBundle(null, getStrings(nodeBean.getDescriptionStrings(), nodeBean.getDescription())));

				if( editing )
				{
					winode.setUsers(ensureSet(winode.getUsers()));
					winode.setGroups(ensureSet(winode.getGroups()));
					winode.setRoles(ensureSet(winode.getRoles()));
					changed |= editSet(winode.getUsers(), nodeBean.getUsers());
					changed |= editSet(winode.getGroups(), nodeBean.getGroups());
					changed |= editSet(winode.getRoles(), nodeBean.getRoles());
				}
				else
				{
					winode.setUsers(nodeBean.getUsers());
					winode.setGroups(nodeBean.getGroups());
					winode.setRoles(nodeBean.getRoles());
				}

				node = winode;
				break;

			default:
				throw new RuntimeException("Unknown node type '" + nodeBean.getType() + "'");
		}

		String uuid = nodeBean.getUuid();
		if( uuid == null )
		{
			uuid = UUID.randomUUID().toString();
		}
		node.setUuid(uuid);
		node.setName(getBundle(null, getStrings(nodeBean.getNameStrings(), nodeBean.getName())));
		node.setWorkflow(entity);
		node.setParent(parent);
		node.setChildIndex(nodeIndex);
		node.setRejectPoint(nodeBean.isRejectPoint());

		final List<WorkflowNodeBean> subNodes = nodeBean.getNodes();
		if( subNodes != null )
		{
			int index = 0;
			for( WorkflowNodeBean subNodeBean : subNodes )
			{
				makeNodes(nodes, subNodeBean, node, index);
				index++;
			}
		}

		if( resave )
		{
			resaveNodes.add(node);
		}
		else if( changed )
		{
			changedNodes.add(node);
		}

		nodes.add(node);
	}

	private Set<String> ensureSet(@Nullable Set<String> existing)
	{
		if( existing == null )
		{
			return new HashSet<>();
		}
		return existing;
	}

	private boolean editSet(Set<String> oldSet, @Nullable Set<String> newSet)
	{
		boolean changed;
		if( newSet == null )
		{
			changed = !oldSet.isEmpty();
			oldSet.clear();
			return changed;
		}
		changed = oldSet.retainAll(newSet);
		changed |= oldSet.addAll(newSet);
		return changed;
	}

	@Override
	protected void afterFinishedEditing()
	{
		super.afterFinishedEditing();

		if( !changedNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(changedNodes, false)));
		}
		if( !resaveNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(resaveNodes, true)));
		}
	}

	@Override
	protected AbstractEntityService<?, Workflow> getEntityService()
	{
		return workflowService;
	}

	@BindFactory
	public interface WorkflowEditorFactory
	{
		WorkflowEditorImpl createExistingEditor(@Assisted Workflow workflow,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		WorkflowEditorImpl createNewEditor(@Assisted Workflow workflow,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
