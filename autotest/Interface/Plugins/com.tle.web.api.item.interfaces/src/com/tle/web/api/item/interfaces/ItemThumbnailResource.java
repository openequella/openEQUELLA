package com.tle.web.api.item.interfaces;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Path("item/{uuid}/{version}/thumb/")
@Api(value = "/item/{uuid}/{version}/thumb", description = "item-thumbnail")
@SuppressWarnings("nls")
public interface ItemThumbnailResource {
  static final String APIDOC_UUID = "The uuid of the item to perform that action on";
  static final String APIDOC_VERSION = "The version of the item to perform that action on";

  @GET
  @Path("/{attachuuid}")
  @ApiOperation("Redirect to thumbnail")
  @Produces(MediaType.WILDCARD)
  public Response getThumb(
      @ApiParam(APIDOC_UUID) @PathParam("uuid") String uuid,
      @ApiParam(APIDOC_VERSION) @PathParam("version") int version,
      @PathParam("attachuuid") String attachUuid);
}
