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
import javax.ws.rs.core.Response;

import com.tle.beans.item.ItemTaskId;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.workflow.AcceptOperation;
import com.tle.core.item.standard.operations.workflow.RejectOperation;
import com.tle.core.item.standard.operations.workflow.WorkflowCommentOperation;
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
	private ItemOperationFactory operationFactory;

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
		AcceptOperation acceptance = operationFactory.accept(itemTaskIdObj.getTaskId(), message, null);
		itemService.operation(itemTaskIdObj, acceptance, operationFactory.save());
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
		RejectOperation rejection = operationFactory.reject(itemTaskIdObj.getTaskId(), message, toNodeUuid, null);
		itemService.operation(itemTaskIdObj, rejection, operationFactory.save());
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
		WorkflowCommentOperation commentary = operationFactory.comment(itemTaskIdObj.getTaskId(), message, null);
		itemService.operation(itemTaskIdObj, commentary, operationFactory.save());
		return Response.ok().build();
	}
}
