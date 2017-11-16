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

package com.tle.web.api.item.resource.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemEditingException;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.relation.RelationService;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.item.interfaces.beans.RelationBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Bind
@Produces({"application/json"})
@Path("item/{uuid}/{version}/relation/")
@Api(value = "/item/{uuid}/{version}/relation", description = "item-relation")
@Singleton
@SuppressWarnings("nls")
public class ItemRelationResourceImpl
{
	private static final String PATH_RELATION_ID = "/{relationId}";

	@Inject
	private RelationService relationService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private UrlLinkService urlLinkService;

	@GET
	@ApiOperation(value = "List all relations for an item")
	public List<RelationBean> getAll(@PathParam("uuid") String uuid, @PathParam("version") int version)
	{
		List<RelationBean> relationBeans = Lists.newArrayList();
		ItemId itemId = new ItemId(uuid, version);
		List<Relation> relations = relationService.getRelationsForItem(itemId);
		for( Relation relation : relations )
		{
			relationBeans.add(convertBean(relation, itemId));
		}
		return relationBeans;
	}

	@POST
	@Consumes("application/json")
	@ApiOperation(value = "Create a relation for an item")
	public Response create(@PathParam("uuid") String uuid, @PathParam("version") int version, RelationBean relation)
	{
		String fromUuid = relation.getFrom() != null ? relation.getFrom().getUuid() : null;
		String toUuid = relation.getTo() != null ? relation.getTo().getUuid() : null;
		if( fromUuid != null && toUuid != null )
		{
			throw new ItemEditingException("Can only specify either 'from' or 'to', not both");
		}
		ItemId thisId = new ItemId(uuid, version);
		final ItemId fromId;
		final ItemId toId;
		if( fromUuid != null )
		{
			fromId = new ItemId(fromUuid, relation.getFrom().getVersion());
			toId = thisId;
		}
		else
		{
			// implied, but stops the null pointer warning
			if( toUuid == null )
			{
				throw new Error("toBean is null");
			}
			fromId = thisId;
			toId = new ItemId(toUuid, relation.getTo().getVersion());
		}
		long relationId = relationService.createRelation(fromId, toId, fromId.equals(thisId), relation.getRelation(),
			relation.getFromResource(), relation.getToResource());
		return Response.created(
			UriBuilder.fromResource(getClass()).path(getClass(), "get").build(uuid, version, relationId)).build();
	}

	@GET
	@Path(PATH_RELATION_ID)
	@ApiOperation(value = "Get a relation for an item")
	public RelationBean get(@PathParam("uuid") String uuid, @PathParam("version") int version,
		@PathParam("relationId") long relationId)
	{
		ItemId itemId = new ItemId(uuid, version);
		Relation relation = relationService.getForView(itemId, relationId);
		return convertBean(relation, itemId);
	}

	@DELETE
	@Path(PATH_RELATION_ID)
	@ApiOperation(value = "Delete a relation for an item")
	public Response delete(@PathParam("uuid") String uuid, @PathParam("version") int version,
		@PathParam("relationId") long relationId)
	{
		ItemId itemId = new ItemId(uuid, version);
		relationService.delete(itemId, relationId);
		return Response.ok().build();
	}

	private RelationBean convertBean(Relation relation, ItemKey itemId)
	{
		RelationBean relationBean = new RelationBean();
		relationBean.setId(relation.getId());
		relationBean.setRelation(relation.getRelationType());
		if( itemId.equals(relation.getFirstItem().getItemId()) )
		{
			relationBean.setTo(convertItem(relation.getSecondItem()));
		}
		else
		{
			relationBean.setFrom(convertItem(relation.getFirstItem()));
		}
		relationBean.setFromResource(relation.getFirstResource());
		relationBean.setToResource(relation.getSecondResource());
		Map<String, String> links = Maps.newHashMap();
		relationBean.set("links", links);

		links.put(
			"self",
			urlLinkService.getMethodUriBuilder(getClass(), "get")
				.build(itemId.getUuid(), itemId.getVersion(), relation.getId()).toString());
		return relationBean;
	}

	private ItemBean convertItem(Item item)
	{
		ItemBean itemBean = new ItemBean();
		itemBean.setUuid(item.getUuid());
		itemBean.setVersion(item.getVersion());
		itemLinkService.addLinks(itemBean);
		return itemBean;
	}

}
