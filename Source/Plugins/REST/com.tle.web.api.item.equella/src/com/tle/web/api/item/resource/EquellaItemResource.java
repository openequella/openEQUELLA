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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

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

	@PUT
	@Path("/quick/{filename}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Quick contribute a new item")
	Response newItemQuick(@ApiParam(value = "Filename", defaultValue = "") @PathParam("filename") String filename,
		InputStream binaryData, @Context UriInfo info);

	@GET
	@Path("/{uuid}/{version}/comment/{commentuuid}")
	@ApiOperation(value = "Retrieve a single comment for an item by ID.")
	CommentBean getOneComment(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(required = true) @PathParam("commentuuid") String commentUuid
		); // @formatter:on

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{uuid}/{version}/comment")
	@ApiOperation(value = "Add a comment")
	Response postComments(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(value = "A comment in json format") CommentBean commentBean
		); // @formatter:on

	@DELETE
	@Path("/{uuid}/{version}/comment")
	@ApiOperation(value = "Delete a comment")
	Response deleteComment(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version,
		@ApiParam(APIDOC_ITEMVERSION) @QueryParam("commentuuid") String commentUuid
		);// @formatter:on
}
