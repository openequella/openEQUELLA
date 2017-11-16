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

package com.tle.web.api.item.resource;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.CommentBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * EQUELLA specific endpoints beyond those declared in ItemResource.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
@Produces(MediaType.APPLICATION_JSON)
@Path("item")
@Api(value = "/item", description = "item")
public interface EquellaItemResource extends ItemResource
{
	static final String APIDOC_ITEMUUID = "The uuid of the item";
	static final String APIDOC_ITEMVERSION = "The version of the item";
	static final String APIDOC_WAITFORINDEX = "Number of seconds to wait for the item to be indexed";
	static final String APIDOC_FILEID = "The id of a file area to use";

	static final String ALL_ALLOWABLE_INFOS = "basic,metadata,attachment,detail,navigation,drm,all";

	// @formatter:off
	@PUT
	@Path("/quick/{filename}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Quick contribute a new item")
	Response newItemQuick(
		@Context 
			UriInfo info,
		@ApiParam(value = "Filename", defaultValue = "") 
		@PathParam("filename") 
			String filename,
		InputStream binaryData);

	@GET
	@Path("/{uuid}/{version}/comment/{commentuuid}")
	@ApiOperation(value = "Retrieve a single comment for an item by ID.")
	CommentBean getOneComment(
		@Context 
			UriInfo info,
		@ApiParam(APIDOC_ITEMUUID) 
		@PathParam("uuid") 
			String uuid,
		@ApiParam(APIDOC_ITEMVERSION) 
		@PathParam("version") 
			int version,
		@ApiParam(required = true) 
		@PathParam("commentuuid") 
			String commentUuid
		); 

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{uuid}/{version}/comment")
	@ApiOperation(value = "Add a comment")
	Response postComments(
		@Context 
			UriInfo info,
		@ApiParam(APIDOC_ITEMUUID) 
		@PathParam("uuid") 
			String uuid,
		@ApiParam(APIDOC_ITEMVERSION)
		@PathParam("version")
			int version,
		@ApiParam(value = "A comment in json format") 
			CommentBean commentBean
		);

	@DELETE
	@Path("/{uuid}/{version}/comment")
	@ApiOperation(value = "Delete a comment")
	Response deleteComment(
		@Context 
			UriInfo info,
		@ApiParam(APIDOC_ITEMUUID) 
		@PathParam("uuid") 
			String uuid,
		@ApiParam(APIDOC_ITEMVERSION) 
		@PathParam("version") 
			int version,
		@ApiParam(APIDOC_ITEMVERSION) 
		@QueryParam("commentuuid") 
			String commentUuid
		);
}
