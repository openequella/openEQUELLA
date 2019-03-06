package com.tle.resttests.test.user;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.json.assertions.UserAssertions;
import com.tle.json.entity.Groups;
import com.tle.json.requests.GroupRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.UserRequestsBuilder;

public class GroupApiTest extends AbstractRestAssuredTest
{
	private GroupRequests groups;
	private GroupRequests groupsAsGuest;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		UserRequestsBuilder builder = new UserRequestsBuilder(this);
		groups = builder.groups();
		groupsAsGuest = builder.groups(this);
	}

	@Test
	public void changeParent() throws Exception
	{
		String parentName = context.getFullName("Parent");
		ObjectNode parent = Groups.json(null, parentName, RestTestConstants.USERID_AUTOTEST);
		parent = groups.create(parent);
		String parentId = groups.getId(parent);

		String child1Name = context.getFullName("Child1");
		ObjectNode child1 = Groups.json(null, child1Name, RestTestConstants.USERID_MODERATOR1);
		child1.put("parentId", parentId);
		child1 = groups.create(child1);
		String child1Id = groups.getId(child1);

		String child2Name = context.getFullName("Child2");
		ObjectNode child2 = Groups.json(null, child2Name, RestTestConstants.USERID_MODERATOR2);
		child2.put("parentId", child1Id);
		child2 = groups.create(child2);
		String child2Id = groups.getId(child2);

		groupsAsGuest.users(groupsAsGuest.accessDeniedRequest(), parentId, true);
		assertUsers(groups.users(parentId, true), true, true, true);
		assertUsers(groups.users(child1Id, true), false, true, true);
		assertUsers(groups.users(child2Id, true), false, false, true);

		child2.put("parentId", (JsonNode) null);
		groups.editId(child2);

		assertUsers(groups.users(parentId, true), true, true, false);
		assertUsers(groups.users(child1Id, true), false, true, false);
		assertUsers(groups.users(child2Id, true), false, false, true);
	}

	@Test
	public void allParents() throws Exception
	{
		String parentName = context.getFullName("Parent");
		ObjectNode parent = Groups.json(null, parentName, RestTestConstants.USERID_AUTOTEST);
		parent = groups.create(parent);
		String parentId = groups.getId(parent);

		String child1Name = context.getFullName("Child1");
		ObjectNode child1 = Groups.json(null, child1Name, RestTestConstants.USERID_MODERATOR1);
		child1.put("parentId", parentId);
		child1 = groups.create(child1);
		String child1Id = groups.getId(child1);

		String child2Name = context.getFullName("Child2");
		ObjectNode child2 = Groups.json(null, child2Name, RestTestConstants.USERID_MODERATOR2);
		child2.put("parentId", child1Id);
		child2 = groups.create(child2);
		String child2Id = groups.getId(child2);

		assertUsers(groups.users(parentId, true), true, true, true);
		assertUsers(groups.users(child1Id, true), false, true, true);
		assertUsers(groups.users(child2Id, true), false, false, true);

		assertUsers(groups.users(parentId, false), true, false, false);
		assertUsers(groups.users(child1Id, false), false, true, false);
		assertUsers(groups.users(child2Id, false), false, false, true);

		child2.withArray("users").add(RestTestConstants.USERID_MODERATOR1);
		groups.editId(child2);
		child2 = groups.get(child2Id);

		assertUsers(groups.users(parentId, true), true, true, true);
		assertUsers(groups.users(child1Id, true), false, true, true);
		assertUsers(groups.users(child2Id, true), false, true, true);

		parent.withArray("users").remove(0);
		groups.editId(parent);
		parent = groups.get(parentId);

		assertUsers(groups.users(parentId, true), false, true, true);
		assertUsers(groups.users(child1Id, true), false, true, true);
		assertUsers(groups.users(child2Id, true), false, true, true);
	}

	private void assertUsers(ObjectNode search, boolean autotest, boolean mod1, boolean mod2)
	{
		Assert.assertTrue(search.has("results"));
		JsonNode results = search.get("results");
		List<String> users = Lists.newArrayList();
		for( JsonNode result : results )
		{
			users.add(result.get("id").asText());
		}

		Assert.assertEquals(users.contains(RestTestConstants.USERID_AUTOTEST), autotest, "Group should"
			+ (autotest ? "" : "n't") + " contain user autotest");
		Assert.assertEquals(users.contains(RestTestConstants.USERID_MODERATOR1), mod1, "Group should"
			+ (mod1 ? "" : "n't") + " contain user moderator1");
		Assert.assertEquals(users.contains(RestTestConstants.USERID_MODERATOR2), mod2, "Group should"
			+ (mod2 ? "" : "n't") + " contain user moderator2");

	}

	@Test
	public void crud() throws Exception
	{
		String groupName = context.getFullName("Group Test");
		ObjectNode group = Groups.json(null, groupName, RestTestConstants.USERID_AUTOTEST);
		groupsAsGuest.createFail(groupsAsGuest.accessDeniedRequest(), group);
		group = groups.create(group);
		String groupId = groups.getId(group);
		UserAssertions.assertGroup(group, groupId, groupName, RestTestConstants.USERID_AUTOTEST);
		groupName = context.getFullName("Changed group");
		group = Groups.json(groupId, groupName, RestTestConstants.USERID_MODERATOR1,
			RestTestConstants.USERID_MODERATOR2);
		groupsAsGuest.editNoPermission(group);
		groups.editId(group);
		groupsAsGuest.get(groupsAsGuest.accessDeniedRequest(), groupId);
		group = groups.get(groupId);
		UserAssertions.assertGroup(group, groupId, groupName, RestTestConstants.USERID_MODERATOR1,
			RestTestConstants.USERID_MODERATOR2);
	}

	@Test
	public void search() throws Exception
	{
		String groupName = context.getFullName("AGroupToSearch");
		ObjectNode group = Groups.json(null, groupName, RestTestConstants.USERID_AUTOTEST);

		groups.create(group);

		// Equella actually does searching
		if( testConfig.isEquella() )
		{
			groupsAsGuest.list(groupsAsGuest.accessDeniedRequest());
			ObjectNode results = groups.list();

			Assert.assertTrue(results.get("available").intValue() > 1);
			JsonNode searchGroup = null;
			for( JsonNode result : results.get("results") )
			{
				if( result.has("name") && groupName.equals(result.get("name").asText()) )
				{
					searchGroup = result;
				}
			}

			Assert.assertNotNull(searchGroup, "group not found");
		}
		else
		{
			ObjectNode error = groups.search("*AGroupToSearch");

			Assert.assertEquals(error.get("code").intValue(), 501);
			Assert.assertEquals(error.get("error").textValue(), "Not Implemented");
			Assert.assertEquals(error.get("error_description").textValue(),
				"Searching for groups is no longer possible");
		}
	}

	@Override
	protected String getDefaultUser()
	{
		//Guest
		return null;
	}
}
