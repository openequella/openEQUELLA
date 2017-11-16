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

package com.tle.web.api.item.interfaces;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;

import com.tle.common.interfaces.CsvList;
import com.tle.web.api.interfaces.beans.FileListBean;
import com.tle.web.api.item.interfaces.beans.CommentBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@SuppressWarnings("nls")
@Produces({"application/json"})
@Path("item")
@Api(value = "/item", description = "item")
public interface ItemResource
{
	static final String APIDOC_ITEMUUID = "The uuid of the item";
	static final String APIDOC_ITEMVERSION = "The version of the item";
	static final String APIDOC_WAITFORINDEX = "Number of seconds to wait for the item to be indexed";
	static final String APIDOC_FILEID = "The id of a file area to use";

	static final String ALL_ALLOWABLE_INFOS = "basic,metadata,attachment,detail,navigation,drm,all";

	@GET
	@Path("/{uuid}/{version}")
	@ApiOperation(value = "Get information about an item", response = ItemBean.class)
	public ItemBean getItem(
		// @formatter:off
		@Context UriInfo uriInfo,
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(value = "How much information to return for the item", required = false, allowableValues = ALL_ALLOWABLE_INFOS, allowMultiple = true) @QueryParam("info") CsvList info);
	// @formatter:on

	@POST
	@Path("/copy")
	@ApiOperation(value = "Copy an item's files to a new file area")
	public Response createStagingFromItem(@QueryParam("uuid") String itemUuid, @QueryParam("version") int itemVersion)
		throws IOException;

	@PUT
	@Path("/{uuid}/{version}")
	@Consumes("application/json")
	@ApiOperation(value = "Edit an item", notes = "When editing an item, attachment UUIDs will be automatically generated if they are "
		+ "left blank or if a placeholder in the format of 'uuid:0', 'uuid:1', etc. "
		+ "You can use these placeholders to refer to the attachments in navigation nodes.")
	public Response edit(
	// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(value = APIDOC_FILEID) @QueryParam("file") final String stagingUuid,
		@ApiParam(value = "The id of the lock in use, if the item is locked") @QueryParam("lock") final String lockId,
		@ApiParam(value = "If locked, whether or not to keep the item locked after editing", allowableValues = ",true,false", defaultValue = "false") 
			@QueryParam("keeplocked") final boolean keepLocked,
		@ApiParam(value = APIDOC_WAITFORINDEX, required=false) @QueryParam("waitforindex") final String waitForIndex,
		@ApiParam(value = "The uuid of the task if the item is in moderation", required=false) @QueryParam("taskUuid") final String taskUuid,
		@ApiParam(value = "The item in json format") final ItemBean itemBean);
	// @formatter:on

	@POST
	@Consumes("application/json")
	@ApiOperation(value = "Create a new item")
	public Response newItem(
		// @formatter:off
		@Context UriInfo uriInfo,
		@ApiParam(value = APIDOC_FILEID) @QueryParam("file") String stagingUuid,
		@ApiParam(value = "Whether or not to leave the item as a draft", allowableValues = ",true,false", defaultValue = "false") 
			@QueryParam("draft") boolean draft,
		@ApiParam(value = APIDOC_WAITFORINDEX, required=false) @QueryParam("waitforindex") String waitForIndex,
		@ApiParam(value = "The item bean in json format") ItemBean itemBean);
	// @formatter:on

	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get information about all versions of this item", response = ItemBean.class)
	public List<ItemBean> getAllVersions(
		// @formatter:off
		@Context UriInfo uriInfo,
		@ApiParam(value = "The uuid to list all the versions of", required = true) @PathParam("uuid") String uuid,
		@ApiParam(required = false, allowableValues = ALL_ALLOWABLE_INFOS, allowMultiple = true) @QueryParam("info") CsvList info);
	// @formatter:on

	@GET
	@Path("/{uuid}/latest")
	@ApiOperation(value = "Get information about the latest version of this item", response = ItemBean.class)
	public ItemBean getLatest(
		// @formatter:off
		@Context UriInfo uriInfo,
		@ApiParam(value = "The uuid to find the latest version of", required = true) @PathParam("uuid") String uuid,
		@ApiParam(required = false, allowableValues = ALL_ALLOWABLE_INFOS, allowMultiple = true) @QueryParam("info") CsvList info);
	// @formatter:on

	@GET
	@Path("/{uuid}/latestlive")
	@ApiOperation(value = "Get information about the latest live version of this item", response = ItemBean.class)
	public ItemBean getLatestLive(
		// @formatter:off
		@Context UriInfo uriInfo,
		@ApiParam(value = "The uuid to find the latest live version of", required = true) @PathParam("uuid") String uuid,
		@ApiParam(required = false, allowableValues = ALL_ALLOWABLE_INFOS, allowMultiple = true) @QueryParam("info") CsvList info);
	// @formatter:on

	@GET
	@Path("/{uuid}/{version}/comment")
	@ApiOperation(value = "Get all comments for an item", response = CommentBean.class)
	public List<CommentBean> getComments(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version);// @formatter:on

	@GET
	@Path("/{uuid}/{version}/history")
	@ApiOperation(value = "Get all history for an item", response = HistoryEventBean.class)
	public List<HistoryEventBean> getHistory(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version); // @formatter:on

	@DELETE
	@Path("/{uuid}/{version}")
	@ApiOperation(value = "Delete an item")
	public Response deleteItem(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(value = APIDOC_WAITFORINDEX, required=false) @QueryParam("waitforindex") final String waitForIndex,
		@ApiParam("Whether or not to purge the item completely from the database") @QueryParam("purge") boolean purge); // @formatter:on

	@GET
	@Path("/{uuid}/{version}/file")
	@ApiOperation(value = "Get item's file listing", response = FileListBean.class)
	@Produces(MediaType.APPLICATION_JSON)
	public FileListBean listFiles(@Context UriInfo uriInfo,
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version);
	// @formatter:on

	@HEAD
	@Path("/{uuid}/{version}/file/{path:(.*)}")
	@ApiOperation(value = "Get file metadata")
	@Produces(MediaType.WILDCARD)
	public Response headFile(@Context HttpHeaders headers,
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,  
		@ApiParam("File path") @PathParam("path") String path);
	// @formatter:on

	@GET
	@Path("/{uuid}/{version}/file/{path:(.*)}")
	@ApiOperation(value = "Read file content")
	@Cache(maxAge = 86400, sMaxAge = 0, mustRevalidate = true)
	@Produces(MediaType.WILDCARD)
	public Response readFile(@Context HttpHeaders headers,
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,  
		@ApiParam("File path") @PathParam("path") String path);
	// @formatter:on
}
