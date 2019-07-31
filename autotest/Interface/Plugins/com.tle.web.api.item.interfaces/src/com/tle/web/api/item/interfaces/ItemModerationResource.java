package com.tle.web.api.item.interfaces;

import com.tle.web.api.item.interfaces.beans.ItemStatusBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/moderation")
@Api(value = "/item/{uuid}/{version}/moderation", description = "item-moderation")
@SuppressWarnings("nls")
public interface ItemModerationResource {
  static final String APIDOC_ITEMUUID = "The uuid of the item";
  static final String APIDOC_ITEMVERSION = "The version of the item";

  @GET
  @ApiOperation(value = "Get the current moderation state", response = ItemStatusBean.class)
  public ItemStatusBean getModeration(
      // @formatter:off
      @ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
      @ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version); // @formatter:on
}
