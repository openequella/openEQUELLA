package com.tle.web.api.institution;

import javax.ws.rs.Path;

import com.tle.web.api.users.interfaces.RoleResource;
import com.wordnik.swagger.annotations.Api;

/**
 * @author Aaron
 */
@Path("usermanagement/local/role/")
@Api(value = "/usermanagement/local/role", description = "usermanagement-local-role")
public interface EquellaRoleResource extends RoleResource
{
	// Nothing to add, just the path annotation at the top
}
