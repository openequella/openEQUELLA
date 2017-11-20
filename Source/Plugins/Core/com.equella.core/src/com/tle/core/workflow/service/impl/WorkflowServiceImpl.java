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

package com.tle.core.workflow.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.hibernate.equella.service.InitialiserService;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTask;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.Pair;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.WorkflowTaskDynamicTarget;
import com.tle.common.security.WorkflowTaskTarget;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.TaskModerator;
import com.tle.common.workflow.TaskModerator.Type;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.events.services.EventService;
import com.tle.core.filesystem.WorkflowMessageFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.ItemIdExtension;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.item.operations.ItemOperationParams;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.ValidationHelper;
import com.tle.core.services.user.UserService;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.core.workflow.event.WorkflowChangeEvent;
import com.tle.core.workflow.extension.WorkflowNodesSaveExtension;
import com.tle.core.workflow.service.WorkflowService;

@Singleton
@SuppressWarnings("nls")
@Bind(WorkflowService.class)
@SecureEntity(RemoteWorkflowService.ENTITY_TYPE)
public class WorkflowServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, Workflow, WorkflowService>
		implements
		WorkflowService,
		ItemDeletedListener,
		ItemIdExtension
{
	private static final String[] BLANKS = {"name"};

	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private UserService userService;
	@Inject
	private ItemService itemService;
	@Inject
	private EventService eventService;
	@Inject
	private PluginTracker<WorkflowNodesSaveExtension> nodeSaveExtensions;

	private final WorkflowDao workflowDao;
	private Function<Workflow, Workflow> cloner;

	@Inject
	public WorkflowServiceImpl(WorkflowDao workflowDao)
	{
		super(Node.WORKFLOW, workflowDao);
		this.workflowDao = workflowDao;
	}

	@Override
	protected Collection<Pair<Object, Node>> getOtherTargetListObjects(Workflow workflow)
	{
		Collection<WorkflowItem> tasks = workflow.getAllWorkflowItems().values();
		Collection<Pair<Object, Node>> results = new ArrayList<Pair<Object, Node>>(tasks.size());

		for( WorkflowItem task : tasks )
		{
			WorkflowTaskTarget target = new WorkflowTaskTarget(workflow.getId(), task.getUuid());
			results.add(new Pair<Object, Node>(target, Node.WORKFLOW_TASK));
		}

		return results;
	}

	@Override
	protected void saveTargetLists(EntityEditingSession<EntityEditingBean, Workflow> session, EntityPack<Workflow> pack)
	{
		// Delete any existing workflow task entries
		getAclManager().deleteAllEntityChildren(Node.WORKFLOW_TASK, pack.getEntity().getId());

		Map<Object, TargetList> targets = new HashMap<Object, TargetList>();
		pack.setOtherTargetLists(targets);

		// Generate ACL entries for the moderators in each task.
		for( WorkflowItem task : pack.getEntity().getAllWorkflowItems().values() )
		{
			WorkflowTaskTarget target = new WorkflowTaskTarget(pack.getEntity().getId(), task.getUuid());

			boolean ae = task.isAllowEditing();

			List<TargetListEntry> entries = new ArrayList<TargetListEntry>();
			addEntries(entries, Recipient.USER, task.getUsers(), ae);
			addEntries(entries, Recipient.GROUP, task.getGroups(), ae);
			addEntries(entries, Recipient.ROLE, task.getRoles(), ae);

			TargetList targetList = new TargetList();
			targetList.setEntries(entries);

			targets.put(target, targetList);
		}

		super.saveTargetLists(session, pack);
	}

	private void addEntries(List<TargetListEntry> targetList, Recipient recipientType, Set<String> recipients,
							boolean allowEditing)
	{
		if( recipients != null )
		{
			for( String recipientId : recipients )
			{
				String recipient = SecurityConstants.getRecipient(recipientType, recipientId);

				TargetListEntry entry1 = new TargetListEntry();
				entry1.setGranted(true);
				entry1.setOverride(true);
				entry1.setWho(recipient);
				entry1.setPrivilege("MODERATE_ITEM");
				targetList.add(entry1);

				TargetListEntry entry2 = new TargetListEntry();
				entry2.setGranted(true);
				entry2.setOverride(true);
				entry2.setWho(recipient);
				entry2.setPrivilege("VIEW_ITEM");
				targetList.add(entry2);

				if( allowEditing )
				{
					TargetListEntry entry3 = new TargetListEntry();
					entry3.setGranted(true);
					entry3.setOverride(true);
					entry3.setWho(recipient);
					entry3.setPrivilege("EDIT_ITEM");
					targetList.add(entry3);
				}
			}
		}
	}

	@Override
	public boolean canCurrentUserModerate(PropBagEx itemxml, WorkflowItem task, WorkflowItemStatus status)
	{
		UserState userState = CurrentUser.getUserState();
		if( userState.isSystem() )
		{
			return true;
		}
		String userID = CurrentUser.getUserID();
		Set<String> acceptedUsers = status.getAcceptedUsers();
		if( acceptedUsers.contains(userID) )
		{
			return false;
		}
		Set<String> groups = task.getGroups();
		if( !Check.isEmpty(groups) )
		{
			Set<String> currentGroups = CurrentUser.getUsersGroups();
			if( !Collections.disjoint(groups, currentGroups) )
			{
				return true;
			}
		}
		Set<String> users = task.getUsers();
		if( !Check.isEmpty(users) && users.contains(userID) )
		{
			return true;
		}

		String path = task.getUserPath();
		if( !Check.isEmpty(path) && itemxml.getNodeList(path).contains(userID) )
		{
			return true;
		}

		if( !Check.isEmpty(task.getRoles()) )
		{
			Set<String> roles = userState.getUsersRoles();
			if( !Collections.disjoint(roles, task.getRoles()) )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	@Transactional
	public boolean isOptionalTask(WorkflowItem task, PropBagEx itemxml)
	{
		boolean hasRoles = !Check.isEmpty(task.getRoles());
		return !(hasRoles || task.isUnanimousacceptance() || getAllModeratorUserIDs(itemxml, task).size() == 1);

	}

	@Override
	@Transactional
	public Set<String> getUsersToNotifyOnScriptError(ScriptNode node)
	{
		final Set<String> results = new HashSet<String>();
		Set<String> users = node.getUsersNotifyOnError();
		if( !Check.isEmpty(users) )
		{
			results.addAll(userService.getInformationForUsers(users).keySet());
		}

		final Set<String> groups = node.getGroupsNotifyOnError();
		if( !Check.isEmpty(groups) )
		{
			for( String groupID : groups )
			{
				for( UserBean user : userService.getUsersInGroup(groupID, true) )
				{
					results.add(user.getUniqueID());
				}
			}
		}

		return results;
	}

	@Override
	@Transactional
	public Set<String> getUsersToNotifyOnScriptCompletion(ScriptNode node)
	{
		final Set<String> results = new HashSet<String>();
		Set<String> users = node.getUsersNotifyOnCompletion();
		if( !Check.isEmpty(users) )
		{
			results.addAll(userService.getInformationForUsers(users).keySet());
		}

		final Set<String> groups = node.getGroupsNotifyOnCompletion();
		if( !Check.isEmpty(groups) )
		{
			for( String groupID : groups )
			{
				for( UserBean user : userService.getUsersInGroup(groupID, true) )
				{
					results.add(user.getUniqueID());
				}
			}
		}

		return results;
	}

	@Override
	@Transactional
	public Set<String> getAllModeratorUserIDs(PropBagEx itemxml, WorkflowItem workflow)
	{
		final Set<String> results = new HashSet<String>();

		final Set<String> usersToCheck = new HashSet<String>();

		final String path = workflow.getUserPath();
		if( !Check.isEmpty(path) )
		{
			usersToCheck.addAll(itemxml.getNodeList(path));
		}

		final Set<String> users = workflow.getUsers();
		if( !Check.isEmpty(users) )
		{
			usersToCheck.addAll(users);
		}

		// Add all users
		if( !Check.isEmpty(usersToCheck) )
		{
			results.addAll(userService.getInformationForUsers(usersToCheck).keySet());
		}

		// Add all users of each groups
		final Set<String> groups = workflow.getGroups();
		if( !Check.isEmpty(groups) )
		{
			for( String groupID : groups )
			{
				for( UserBean user : userService.getUsersInGroup(groupID, true) )
				{
					results.add(user.getUniqueID());
				}
			}
		}

		return results;
	}

	@Override
	public List<Class<?>> getReferencingClasses(long id)
	{
		List<Class<?>> usage = new ArrayList<Class<?>>();
		if( !itemDefinitionService.enumerateForWorkflow(id).isEmpty() )
		{
			usage.add(ItemDefinition.class);
		}
		return usage;
	}

	@Override
	protected void deleteReferences(Workflow entity)
	{
		eventService.publishApplicationEvent(new WorkflowChangeEvent(entity.getId(), null, true));
	}

	@Override
	@SecureOnReturn(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
	@Transactional(propagation = Propagation.REQUIRED)
	public Workflow stopEdit(EntityPack<Workflow> pack, boolean unlock)
	{
		Workflow oldEntity = get(pack.getEntity().getId());
		Workflow newEntity = pack.getEntity();

		// TODO: refactor this stopEdit to be the one with the session
		validate(null, newEntity);
		oldEntity.setDateModified(new Date());

		try
		{
			lockingService.getLock(newEntity);

			editCommonFields(oldEntity, newEntity);
			oldEntity.setOwner(newEntity.getOwner());
			oldEntity.setMovelive(newEntity.isMovelive());

			editNodes(oldEntity, newEntity);
			oldEntity.getAllNodesAsMap().clear();
			oldEntity.setAttributes(newEntity.getAttributes());

			// TODO: refactor this stopEdit to be the one with the session
			saveTargetLists(null, pack);

			saveFiles(pack, unlock, unlock, false, newEntity);
		}
		catch( LockedException e )
		{
			throw new RuntimeApplicationException(e);
		}

		auditLogService.logEntityModified(newEntity.getId());

		return newEntity;
	}

	private void editNodes(Workflow oldWorkflow, Workflow newWorkflow)
	{
		Map<String, WorkflowNode> newMap = newWorkflow.getAllNodesAsMap();
		Set<WorkflowNode> oldNodes = oldWorkflow.getNodes();
		Set<WorkflowNode> deletedNodes = new HashSet<WorkflowNode>();
		Set<WorkflowNode> changedNodes = new HashSet<WorkflowNode>();
		Set<WorkflowNode> resaveNodes = new HashSet<WorkflowNode>();

		Iterator<WorkflowNode> iter = oldNodes.iterator();
		while( iter.hasNext() )
		{
			WorkflowNode node = iter.next();
			String uuid = node.getUuid();
			WorkflowNode newNode = newMap.get(uuid);
			if( newNode != null )
			{
				editNode(node, newNode, changedNodes, resaveNodes);
				newMap.remove(uuid);
			}
			else
			{
				deletedNodes.add(node);
			}
		}
		for( WorkflowNode newNode : newMap.values() )
		{
			newNode.setId(0);
			oldNodes.add(newNode);
		}
		Map<String, WorkflowNode> oldMap = oldWorkflow.getAllNodesAsMap();
		for( WorkflowNode node : oldNodes )
		{
			WorkflowNode parent = node.getParent();
			if( parent != null )
			{
				node.setParent(oldMap.get(parent.getUuid()));
			}
		}
		for( WorkflowNodesSaveExtension extension : nodeSaveExtensions.getBeanList() )
		{
			extension.workflowNodesSaved(oldWorkflow, oldNodes, deletedNodes, changedNodes, resaveNodes);
		}
	}

	private void editNode(WorkflowNode node, WorkflowNode newNode, Set<WorkflowNode> changedNodes,
						  Set<WorkflowNode> resaveNodes)
	{
		newNode.setId(node.getId());
		node.setChildIndex(newNode.getChildIndex());
		node.setName(editBundle(node.getName(), newNode.getName()));
		node.setRejectPoint(newNode.isRejectPoint());
		node.setParent(newNode.getParent());
		if( node.getType() == WorkflowNode.ITEM_TYPE )
		{
			WorkflowItem item = (WorkflowItem) node;
			WorkflowItem newItem = (WorkflowItem) newNode;
			boolean changed = item.isUnanimousacceptance() != newItem.isUnanimousacceptance();
			boolean resave = !Objects.equals(item.getUserPath(), newItem.getUserPath());
			changed |= editSet(item.getUsers(), newItem.getUsers());
			changed |= editSet(item.getGroups(), newItem.getGroups());
			changed |= editSet(item.getRoles(), newItem.getRoles());
			resave |= item.isAllowEditing() != newItem.isAllowEditing() && !Check.isEmpty(newItem.getUserPath());
			resave |= (item.getPriority() != newItem.getPriority());
			editSet(item.getAutoAssigns(), newItem.getAutoAssigns());
			item.setDescription(editBundle(item.getDescription(), newItem.getDescription()));
			item.setAutoAssignNode(newItem.getAutoAssignNode());
			item.setAutoAssignSchemaUuid(newItem.getAutoAssignSchemaUuid());
			item.setUserPath(newItem.getUserPath());
			item.setUserSchemaUuid(newItem.getUserSchemaUuid());
			item.setAllowEditing(newItem.isAllowEditing());
			item.setUnanimousacceptance(newItem.isUnanimousacceptance());
			item.setEscalate(newItem.isEscalate());
			item.setEscalationdays(newItem.getEscalationdays());
			item.setMovelive(newItem.getMovelive());
			item.setAutoAction(newItem.getAutoAction());
			item.setActionDays(newItem.getActionDays());
			item.setDueDatePath(newItem.getDueDatePath());
			item.setDueDateSchemaUuid(newItem.getDueDateSchemaUuid());
			item.setPriority(newItem.getPriority());
			if( resave )
			{
				resaveNodes.add(item);
			}
			else if( changed )
			{
				changedNodes.add(item);
			}
		}
		else if( node.getType() == WorkflowNode.DECISION_TYPE )
		{
			DecisionNode decision = (DecisionNode) node;
			DecisionNode newDecision = (DecisionNode) newNode;
			decision.setScript(newDecision.getScript());
			decision.setCollectionUuid(newDecision.getCollectionUuid());
		}
		else if( node.getType() == WorkflowNode.SCRIPT_TYPE )
		{
			ScriptNode scriptNode = (ScriptNode) node;
			ScriptNode newScriptNode = (ScriptNode) newNode;
			scriptNode.setDescription(editBundle(scriptNode.getDescription(), newScriptNode.getDescription()));
			scriptNode.setMovelive(newScriptNode.getMovelive());
			scriptNode.setProceedNext(newScriptNode.isProceedNext());
			boolean changed = !Objects.equals(newScriptNode.getScript(), scriptNode.getScript());
			scriptNode.setScript(newScriptNode.getScript());
			scriptNode.setNotifyOnCompletion(newScriptNode.isNotifyOnCompletion());
			editSet(scriptNode.getUsersNotifyOnCompletion(), newScriptNode.getUsersNotifyOnCompletion());
			editSet(scriptNode.getGroupsNotifyOnCompletion(), newScriptNode.getGroupsNotifyOnCompletion());
			editSet(scriptNode.getUsersNotifyOnError(), newScriptNode.getUsersNotifyOnError());
			editSet(scriptNode.getGroupsNotifyOnError(), newScriptNode.getGroupsNotifyOnError());
			if (changed)
			{
				changedNodes.add(scriptNode);
			}
		}
	}

	private boolean editSet(Set<String> oldSet, Set<String> newSet)
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
	protected void doValidation(EntityEditingSession<EntityEditingBean, Workflow> session, Workflow entity,
								List<ValidationError> errors)
	{
		ValidationHelper.checkBlankFields(entity, BLANKS, errors);
	}

	@Override
	protected Workflow getForClone(long id)
	{
		Workflow workflow = get(id);
		Workflow cloned = cloner.apply(workflow);
		initialiserService.initialiseClones(cloned);
		return cloned;
	}

	@Override
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		TargetList targetList = new TargetList();
		targetList.setPartial(true);
		aclManager.setTargetList(Node.WORKFLOW_TASK, new WorkflowTaskDynamicTarget(event.getKey()), targetList);
	}

	@Override
	@Transactional
	public String getLastRejectionMessage(Item item)
	{
		return item.getModeration().getRejectedMessage();
	}

	@Override
	public void setup(ItemKey itemId, ItemOperationParams params, Item item)
	{
		ItemTaskId taskItemId = (ItemTaskId) itemId;
		String taskId = taskItemId.getTaskId();
		WorkflowItem task = workflowDao.getTaskForItem(item, taskId);
		if( task == null )
		{
			throw new ItemNotFoundException(itemId);
		}
		params.setSecurityObject(new ItemTask(item, taskId));
	}

	@Override
	@Transactional
	public int getMessageCount(ItemKey itemKey)
	{
		return workflowDao.getCommentCount(itemKey);
	}

	@Override
	@Transactional
	public List<WorkflowMessage> getMessages(ItemKey itemKey)
	{
		return workflowDao.getMessages(itemKey);
	}

	@Override
	@Transactional
	public WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId)
	{
		return workflowDao.getIncompleteStatus(itemTaskId);
	}

	@Override
	@Transactional
	public List<TaskModerator> getModeratorList(ItemTaskId taskId, boolean includeAccepted)
	{
		Item item = itemService.getUnsecure(taskId);
		PropBagEx itemxml = itemService.getItemXmlPropBag(item);
		WorkflowItem task = workflowDao.getTaskForItem(item, taskId.getTaskId());
		List<TaskModerator> moderators = Lists.newArrayList();
		WorkflowItemStatus status = getIncompleteStatus(taskId);
		if( status != null )
		{
			Set<String> accepted = status.getAcceptedUsers();
			Set<String> allModeratorUserIDs = getAllModeratorUserIDs(itemxml, task);

			for( String userId : allModeratorUserIDs )
			{
				boolean acceptedAlready = accepted.contains(userId);
				if( includeAccepted || !acceptedAlready )
				{
					moderators.add(new TaskModerator(userId, Type.USER, acceptedAlready));
				}
			}
			Set<String> roles = task.getRoles();
			for( String roleId : roles )
			{
				moderators.add(new TaskModerator(roleId, Type.ROLE, false));
			}
		}
		return moderators;
	}

	/**
	 * @return non-null List of WorkflowMessage
	 */
	@Override
	@Transactional
	public List<WorkflowMessage> getCommentsForTask(ItemTaskId itemTaskId)
	{
		WorkflowItemStatus status = workflowDao.getIncompleteStatus(itemTaskId);
		if( status == null )
		{
			return Lists.newArrayList();
		}
		List<WorkflowMessage> allMessages = Lists.newArrayList(status.getComments());
		WorkflowNodeStatus cause = status.getCause();
		if( cause != null )
		{
			for( WorkflowMessage workflowMessage : cause.getComments() )
			{
				char type = workflowMessage.getType();
				if( type == WorkflowMessage.TYPE_REJECT || type == WorkflowMessage.TYPE_SUBMIT
						|| type == WorkflowMessage.TYPE_ACCEPT )
				{
					allMessages.add(workflowMessage);
				}
			}
		}
		return allMessages;
	}

	@Override
	@Transactional
	@SecureOnReturn(priv = "MANAGE_WORKFLOW")
	public Collection<BaseEntityLabel> listManagable()
	{
		return listAll();
	}

	@Override
	@Transactional
	public WorkflowItem getManageableTask(long taskId)
	{
		WorkflowItem item = workflowDao.getWorkflowTaskById(taskId);
		checkManage(item.getWorkflow());
		return item;
	}

	@SecureOnCall(priv = "MANAGE_WORKFLOW")
	protected void checkManage(Workflow workflow)
	{
		// erm
	}

	@Override
	@Transactional
	public int getItemCountForWorkflow(String uuid)
	{
		return workflowDao.getItemCountForWorkflow(uuid);
	}

	@Override
	public boolean cleanupMessageFiles(ItemKey itemKey)
	{
		cleanFilesForMessages(getMessages(itemKey));
		return true;
	}

	private void cleanFilesForMessages(Collection<WorkflowMessage> messages)
	{
		for( WorkflowMessage msg : messages )
		{
			fileSystemService.removeFile(new WorkflowMessageFile(msg.getUuid()));
		}
	}

	@Override
	public void cleanupMessageFiles(Collection<WorkflowNodeStatus> statuses)
	{
		for ( WorkflowNodeStatus status: statuses)
		{
			cleanFilesForMessages(status.getComments());
		}
	}

	@Override
	@Inject
	public void setInitialiserService(InitialiserService initialiserService) {
		super.setInitialiserService(initialiserService);
		this.cloner = initialiserService.createCloner(getClass().getClassLoader());
	}
}
