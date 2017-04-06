package com.tle.web.api.usermanagement;

import javax.ws.rs.Path;

import com.tle.web.api.users.interfaces.UserResource;
import com.wordnik.swagger.annotations.Api;

/**
 * @author Aaron
 */
@Path("usermanagement/local/user/")
@Api(value = "/usermanagement/local/user", description = "usermanagement-local-user")
public interface EquellaUserResource extends UserResource
{
	// Nothing to add, just the Path annotation
}
