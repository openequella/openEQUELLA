package com.tle.core.services.entity;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemTaskId;
import com.tle.common.workflow.RemoteWorkflowService;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.WorkflowItemStatus;
import com.tle.common.workflow.WorkflowMessage;
import com.tle.common.workflow.node.WorkflowItem;

/**
 * @author Nicholas Read
 */
public interface WorkflowService extends AbstractEntityService<EntityEditingBean, Workflow>, RemoteWorkflowService
{
	/**
	 * Returns a list of moderators for a given workflow item. This is useful in
	 * the case of unanimous acceptance, where all users that must moderate this
	 * item. For this to succeed all the moderators must be defined as a
	 * specific user or a group. There is currently no means to determine all
	 * moderators if a role is specified, and <code>null</code> will be
	 * returned.
	 */
	Set<String> getAllModeratorUserIDs(PropBagEx itemxml, WorkflowItem workflow);

	boolean canCurrentUserModerate(PropBagEx itemxml, WorkflowItem task, WorkflowItemStatus status);

	String getLastRejectionMessage(Item item);

	boolean isOptionalTask(WorkflowItem workflowItem, PropBagEx itemxml);

	int getMessageCount(ItemKey itemKey);

	List<WorkflowMessage> getMessages(ItemKey itemKey);

	WorkflowItemStatus getIncompleteStatus(ItemTaskId itemTaskId);

	List<TaskModerator> getModeratorList(ItemTaskId taskId, boolean includeAccepted);

	List<WorkflowMessage> getCommentsForTask(ItemTaskId itemTaskId);

	Collection<BaseEntityLabel> listManagable();

	WorkflowItem getManageableTask(long taskId);

	int getItemCountForWorkflow(String uuid);
}
