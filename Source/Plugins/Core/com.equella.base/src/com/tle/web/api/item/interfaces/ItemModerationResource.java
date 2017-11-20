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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.tle.web.api.item.interfaces.beans.ItemStatusBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Produces({"application/json"})
@Path("item/{uuid}/{version}/moderation")
@Api(value = "/item/{uuid}/{version}/moderation", description = "item-moderation")
@SuppressWarnings("nls")
public interface ItemModerationResource
{
	static final String APIDOC_ITEMUUID = "The uuid of the item";
	static final String APIDOC_ITEMVERSION = "The version of the item";

	@GET
	@ApiOperation(value = "Get the current moderation state", response = ItemStatusBean.class)
	public ItemStatusBean getModeration(
		// @formatter:off
		@ApiParam(APIDOC_ITEMUUID) @PathParam("uuid") String uuid,
		@ApiParam(APIDOC_ITEMVERSION) @PathParam("version") int version); // @formatter:on
}
