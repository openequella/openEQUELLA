package com.tle.web.api.item.resource.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.interfaces.ItemActionsResource;

@Bind(ItemActionsResource.class)
@Singleton
public class ItemActionsResourceImpl implements ItemActionsResource
{
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;
	@Inject
	private ItemLinkService itemLinkService;

	@Override
	public Response submit(String uuid, int version, String submitMessage)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.secureSubmit(submitMessage), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response redraft(String uuid, int version)

	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.redraft(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response archive(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.archive(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response suspend(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.suspend(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response reset(String uuid, int version)

	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.reset(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response reactivate(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.reactivate(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response restore(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.restore(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response resume(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.resume(), workflowFactory.save());
		return okResponse(itemId);
	}

	@Override
	public Response review(String uuid, int version)
	{
		ItemId itemId = new ItemId(uuid, version);
		itemService.operation(itemId, workflowFactory.review(), workflowFactory.save());
		return okResponse(itemId);
	}

	private Response okResponse(ItemId itemId)
	{
		return Response.ok().location(itemLinkService.getItemURI(itemId)).build();
	}
}
