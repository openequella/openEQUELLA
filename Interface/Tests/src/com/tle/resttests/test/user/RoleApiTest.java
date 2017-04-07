package com.tle.resttests.test.user;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.UserAssertions;
import com.tle.json.entity.Roles;
import com.tle.json.requests.RoleRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.UserRequestsBuilder;

// Ugh. Due to EQUELLA using ridiculous configuration properties, doing
// concurrent updates to roles is a flakey joke
@Test(singleThreaded = true)
public class RoleApiTest extends AbstractRestAssuredTest
{
	private RoleRequests roles;
	private RoleRequests rolesAsGuest;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		UserRequestsBuilder builder = new UserRequestsBuilder(this);
		roles = builder.roles();
		rolesAsGuest = builder.roles(this);
	}

	@Test
	public void crud() throws Exception
	{
		String roleName = context.getFullName("Simple Role");
		String expression = "U:" + RestTestConstants.USERID_AUTOTEST;
		ObjectNode role = Roles.json(null, roleName, expression);
		role = roles.create(role);
		String roleId = roles.getId(role);
		UserAssertions.assertRole(role, roleId, roleName, expression);
		roleName = context.getFullName("Edited name");
		expression = "U:" + RestTestConstants.USERID_MODERATOR1;
		role = Roles.json(roleId, roleName, expression);
		rolesAsGuest.editNoPermission(role);
		roles.editId(role);
		role = roles.get(roleId);
		UserAssertions.assertRole(role, roleId, roleName, expression);
		rolesAsGuest.delete(rolesAsGuest.accessDeniedRequest(), roleId);
	}

	@Test(groups = "eps")
	public void search() throws Exception
	{
		String roleName = context.getFullName("ARoleToSearch");
		ObjectNode role = Roles.json(null, roleName, "*");

		roles.create(role);

		ObjectNode error = roles.search("*ARoleToSearch");

		Assert.assertEquals(error.get("code").intValue(), 501);
		Assert.assertEquals(error.get("error").textValue(), "Not Implemented");
		Assert.assertEquals(error.get("error_description").textValue(), "Searching for roles is no longer possible");

		ObjectNode searchRole = roles.getByName(roleName);
		UserAssertions.assertRole(searchRole, null, roleName, "*");
	}

	@Override
	protected String getDefaultUser()
	{
		//Guest
		return null;
	}
}
