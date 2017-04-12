package com.tle.core.services.entity.impl;

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

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.beanlib.hibernate3.Hibernate3BeanReplicator;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.LockedException;
import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.ejb.helpers.ValidationHelper;
import com.dytech.edge.exceptions.ItemNotFoundException;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.beans.IdTransformer;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTask;
import com.tle.beans.item.ItemTaskId;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.ItemIdExtension;
import com.tle.common.Pair;
import com.tle.common.WorkflowOperationParams;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.common.security.WorkflowTaskDynamicTarget;
import com.tle.common.security.WorkflowTaskTarget;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.WorkflowNodeStatus;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.dao.WorkflowDao;
import com.tle.core.events.GroupDeletedEvent;
import com.tle.core.events.GroupEditEvent;
import com.tle.core.events.GroupIdChangedEvent;
import com.tle.core.events.ItemDeletedEvent;
import com.tle.core.events.ItemOperationEvent;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.GroupChangedListener;
import com.tle.core.events.listeners.ItemDeletedListener;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.AbstractFactoryLocator;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.services.EventService;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.entity.TaskModerator;
import com.tle.core.services.entity.TaskModerator.Type;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.core.workflow.event.WorkflowChangeEvent;
import com.tle.core.workflow.filters.BaseFilter;
import com.tle.core.workflow.filters.FilterFactory;
import com.tle.core.workflow.filters.ResetFilter;

@Singleton
@SuppressWarnings("nls")
@Bind(WorkflowService.class)
@SecureEntity(RemoteWorkflowService.ENTITY_TYPE)
public class WorkflowServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, Workflow, WorkflowService>
	implements
		WorkflowService,
		GroupChangedListener,
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

	private final WorkflowDao workflowDao;

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
		if( !deletedNodes.isEmpty() )
		{
			eventService.publishApplicationEvent(new WorkflowChangeEvent(oldWorkflow.getId(), deletedNodes, true));
			workflowDao.markForReset(deletedNodes);
			oldNodes.removeAll(deletedNodes);
			publishEventAfterCommit(new ItemOperationEvent(new ClassBeanLocator<ResetFilter>(ResetFilter.class)));
		}
		if( !changedNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(changedNodes, false)));
		}
		if( !resaveNodes.isEmpty() )
		{
			publishEventAfterCommit(new ItemOperationEvent(new CheckForStepsLocator(resaveNodes, true)));
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
	@Transactional
	public void groupEditedEvent(GroupEditEvent groupEditEvent)
	{
		groupChange(groupEditEvent.getGroupID());
	}

	private void groupChange(String groupId)
	{
		Collection<WorkflowItem> nodesForGroup = workflowDao.findTasksForGroup(groupId);

		if( !Check.isEmpty(nodesForGroup) )
		{
			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForGroup, false)));
		}
	}

	public static class CheckForStepsLocator extends AbstractFactoryLocator<FilterFactory, BaseFilter>
	{
		private static final long serialVersionUID = 1L;
		private final Collection<Long> nodeIds;
		private final boolean forceModify;

		public CheckForStepsLocator(Collection<? extends WorkflowNode> nodes, boolean forceModify)
		{
			super(FilterFactory.class);
			this.nodeIds = Lists.newArrayList(Collections2.transform(nodes, IdTransformer.INSTANCE));
			this.forceModify = forceModify;
		}

		@Override
		protected BaseFilter invoke(FilterFactory factory)
		{
			return factory.checkForSteps(nodeIds, forceModify);
		}
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		Collection<WorkflowItem> nodesForUser = workflowDao.findTasksForUser(event.getUserID());
		if( !Check.isEmpty(nodesForUser) )
		{
			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForUser, false)));
		}

		super.userDeletedEvent(event);
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		final String fromId = event.getFromUserId();
		final String toId = event.getToUserId();

		final Collection<WorkflowMessage> messages = workflowDao.findMessagesForUser(fromId);
		for( WorkflowMessage message : messages )
		{
			message.setUser(toId);
			workflowDao.saveAny(message);
		}

		final Collection<WorkflowItemStatus> wisses = workflowDao.findWorkflowItemStatusesForUser(fromId);
		for( WorkflowItemStatus wis : wisses )
		{
			if( wis.getAssignedTo() != null && wis.getAssignedTo().equals(fromId) )
			{
				wis.setAssignedTo(toId);
			}
			final Set<String> acceptedUsers = wis.getAcceptedUsers();
			if( acceptedUsers.contains(fromId) )
			{
				acceptedUsers.remove(fromId);
				acceptedUsers.add(toId);
			}
			workflowDao.saveAny(wis);
		}

		final Collection<ModerationStatus> mods = workflowDao.findModerationStatusesForUser(fromId);
		for( ModerationStatus mod : mods )
		{
			mod.setRejectedBy(toId);
			workflowDao.saveAny(mod);
		}

		Collection<WorkflowItem> nodesForUser = workflowDao.findTasksForUser(fromId);
		if( !Check.isEmpty(nodesForUser) )
		{
			for( WorkflowItem n : nodesForUser )
			{
				Set<String> users = n.getUsers();
				users.remove(fromId);
				users.add(toId);
			}

			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForUser, false)));
		}

		super.userIdChangedEvent(event);
	}

	@Override
	@Transactional
	public void groupDeletedEvent(GroupDeletedEvent event)
	{
		groupChange(event.getGroupID());
	}

	@Override
	@Transactional
	public void groupIdChangedEvent(GroupIdChangedEvent event)
	{
		Collection<WorkflowItem> nodesForGroup = workflowDao.findTasksForGroup(event.getFromGroupId());
		if( !Check.isEmpty(nodesForGroup) )
		{
			for( WorkflowItem n : nodesForGroup )
			{
				Set<String> groups = n.getGroups();
				groups.remove(event.getFromGroupId());
				groups.add(event.getToGroupId());
			}

			publishEvent(new ItemOperationEvent(new CheckForStepsLocator(nodesForGroup, false)));
		}
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
		Set<Class<?>> clazzes = new HashSet<Class<?>>();
		clazzes.add(WorkflowNode.class);
		clazzes.add(LanguageBundle.class);
		Hibernate3BeanReplicator replicator = new Hibernate3BeanReplicator(clazzes, null, null);
		Workflow cloned = replicator.copy(workflow);
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
	public void setup(ItemKey itemId, WorkflowOperationParams params, Item item)
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
		WorkflowItemStatus status = getIncompleteStatus(taskId);
		Set<String> accepted = status.getAcceptedUsers();
		Set<String> allModeratorUserIDs = getAllModeratorUserIDs(itemxml, task);
		List<TaskModerator> moderators = Lists.newArrayList();
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
}
