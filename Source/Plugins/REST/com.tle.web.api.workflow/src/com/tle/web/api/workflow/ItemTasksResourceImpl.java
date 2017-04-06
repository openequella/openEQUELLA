package com.tle.web.api.workflow;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import com.tle.beans.item.ItemTaskId;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.CommentOperation;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.tasks.AcceptOperation;
import com.tle.core.workflow.operations.tasks.RejectOperation;
import com.tle.web.api.item.tasks.interfaces.ItemTaskResource;

/**
 * This class deals with tasks performed directly upon an item.
 * 
 * @see TaskResourceImpl for the search for tasks API method
 * @author Larry
 */
@Bind(ItemTaskResource.class)
@Singleton
public class ItemTasksResourceImpl implements ItemTaskResource
{
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;

	/**
	 * accept equates to 'Approving' a task ...<br>
	 * NB: Same as EPS endpoint.
	 * 
	 * @param uuid
	 * @param version
	 * @param taskUuid
	 * @param message optional, in query string
	 * @return
	 */
	@Override
	public Response accept(
// @formatter:off
		final String uuid, 
		final int version,
		final String taskUuid, 
		final String message
		// @formatter:on
	)
	{
		ItemTaskId itemTaskIdObj = new ItemTaskId(uuid, version, taskUuid);
		AcceptOperation acceptance = workflowFactory.accept(itemTaskIdObj.getTaskId(), message);
		itemService.operation(itemTaskIdObj, acceptance, workflowFactory.save());
		return Response.ok().build();
	}

	/**
	 * NB: Same as EPS endpoint.
	 * 
	 * @param uuid
	 * @param version
	 * @param taskUuid
	 * @param message optional, in query string
	 * @param to optional, in query string
	 * @return
	 */
	@Override
	public Response reject(
// @formatter:off
		final String uuid, 
		final int version,
		final String taskUuid, 
		final String message,
		final String toNodeUuid
		// @formatter:on
	)
	{
		ItemTaskId itemTaskIdObj = new ItemTaskId(uuid, version, taskUuid);
		RejectOperation rejection = workflowFactory.reject(itemTaskIdObj.getTaskId(), message, toNodeUuid);
		itemService.operation(itemTaskIdObj, rejection, workflowFactory.save());
		return Response.ok().build();
	}

	/**
	 * Post a moderation comment, isolated from any actual moderation action.
	 * 
	 * @param user
	 * @return
	 */
	@Override
	public Response comment(
// @formatter:off
		final String uuid, 
		final int version,
		final String taskUuid, 
		final String message
		// @formatter:on
	)
	{
		ItemTaskId itemTaskIdObj = new ItemTaskId(uuid, version, taskUuid);
		CommentOperation commentary = workflowFactory.comment(itemTaskIdObj.getTaskId(), message);
		itemService.operation(itemTaskIdObj, commentary, workflowFactory.save());
		return Response.ok().build();
	}
}
