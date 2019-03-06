package com.tle.resttests.test.workflow;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

public class ItemWorkflowLiveTest extends AbstractEntityCreatorTest
{
	private String collectionUuid1;
	private String collectionUuid2;
	private TaskRequests tasks1;

	private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";
	private static final String STEP2 = "ff5b5b31-9d83-11e2-9e96-0800200c9a66";

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		tasks1 = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
	}

	@Test
	public void create()
	{
		ObjectNode workflow = Workflows.json("Move live workflow1");
		ObjectNode task1 = Workflows.task(STEP1, "Only step", false, RestTestConstants.USERID_MODERATOR1);
		task1.put("moveLive", "arrival");
		Workflows.rootChild(workflow, task1);
		collectionUuid1 = collections.createId(CollectionJson.json("Move live collection1", RestTestConstants.SCHEMA_BASIC,
			workflows.createId(workflow)));
		workflow = Workflows.json("Move live workflow2");
		task1 = Workflows.task(STEP1, "Step1", false, RestTestConstants.USERID_MODERATOR1);
		task1.put("moveLive", "accepted");
		ObjectNode task2 = Workflows.task(STEP2, "Step2", false, RestTestConstants.USERID_MODERATOR1);
		Workflows.rootChild(workflow, task1);
		Workflows.rootChild(workflow, task2);
		collectionUuid2 = collections.createId(CollectionJson.json("Move live collection2", RestTestConstants.SCHEMA_BASIC,
			workflows.createId(workflow)));

	}

	@Test(dependsOnMethods = "create")
	public void arrival()
	{
		ItemId itemId = items
			.createId(collectionUuid1, true, "item/name", context.getFullName("Move live immediately"));
		ObjectNode moderation = items.moderation(itemId);
		ItemStatusAssertions.assertStatus(moderation, "live");
		Assert.assertNotNull(moderation.get("nodes"));
		tasks1.accept(itemId, STEP1, "Good work");
		moderation = items.moderation(itemId);
		ItemStatusAssertions.assertStatus(moderation, "live");
		Assert.assertNull(moderation.get("nodes"));
	}

	@Test(dependsOnMethods = "create")
	public void accepted()
	{
		ItemId itemId = items.createId(collectionUuid2, true, "item/name",
			context.getFullName("Move live after acceptance"));
		ObjectNode moderation = items.moderation(itemId);
		ItemStatusAssertions.assertStatus(moderation, "moderating");
		Assert.assertNotNull(moderation.get("nodes"));
		tasks1.accept(itemId, STEP1, "Good work");
		moderation = items.moderation(itemId);
		ItemStatusAssertions.assertStatus(moderation, "live");
		Assert.assertNotNull(moderation.get("nodes"));
		tasks1.accept(itemId, STEP2, "Good work");
		moderation = items.moderation(itemId);
		ItemStatusAssertions.assertStatus(moderation, "live");
		Assert.assertNull(moderation.get("nodes"));
	}
}
