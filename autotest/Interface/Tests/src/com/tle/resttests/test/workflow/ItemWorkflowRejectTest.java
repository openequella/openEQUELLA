package com.tle.resttests.test.workflow;

import static com.tle.json.assertions.ItemStatusAssertions.assertNodeStatus;
import static com.tle.json.assertions.ItemStatusAssertions.findStatus;
import static com.tle.json.assertions.ItemStatusAssertions.Status.C;
import static com.tle.json.assertions.ItemStatusAssertions.Status.I;
import static com.tle.json.assertions.ItemStatusAssertions.Status.W;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions.Status;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

public class ItemWorkflowRejectTest extends AbstractEntityCreatorTest
{
	private TaskRequests tasks;

	private ItemId itemId;

	private String collectionUuid;

	private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";
	private static final String STEP2 = "ff5b5b31-9d83-11e2-9e96-0800200c9a66";
	private static final String STEP3 = "ff5b5b32-9d83-11e2-9e96-0800200c9a66";
	private static final String STEP4 = "ff5b5b33-9d83-11e2-9e96-0800200c9a66";
	private static final String SERIAL = "ff5b5b34-9d83-11e2-9e96-0800200c9a66";

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		tasks = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
	}

	@Test
	public void create()
	{
		ObjectNode workflow = Workflows.json("Workflow Reject sibling");
		workflow.put("moveLive", true);
		ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
		task1.put("rejectPoint", true);
		ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR1);
		Workflows.rootChild(workflow, task1);
		Workflows.rootChild(workflow, task2);
		ObjectNode serial = Workflows.serial(SERIAL, "Serial", true);
		ObjectNode task3 = Workflows.task(STEP3, "Step 3", false, RestTestConstants.USERID_MODERATOR1);
		ObjectNode task4 = Workflows.task(STEP4, "Step 4", false, RestTestConstants.USERID_MODERATOR1);
		Workflows.child(serial, task3);
		Workflows.child(serial, task4);
		Workflows.rootChild(workflow, serial);

		String workflowUuid = workflows.createId(workflow);
		collectionUuid = collections.createId(CollectionJson.json("Workflow Node Test",
			RestTestConstants.SCHEMA_BASIC, workflowUuid));

		itemId = items.createId(collectionUuid, true, "item/name", context.getFullName("Node Item"));
	}

	private void assertUpto(Status step1, Status step2, Status step3, Status step4)
	{
		ObjectNode moderation = items.moderation(itemId);
		assertNodeStatus(findStatus(moderation, STEP1), step1);
		assertNodeStatus(findStatus(moderation, STEP2), step2);
		assertNodeStatus(findStatus(moderation, SERIAL, STEP3), step3);
		assertNodeStatus(findStatus(moderation, SERIAL, STEP4), step4);
	}

	@Test(dependsOnMethods = "create")
	public void rejectToSibling()
	{
		tasks.accept(itemId, STEP1, "Good work");
		assertUpto(C, I, W, W);
		tasks.reject(itemId, STEP2, "Back to step 1", STEP1);
		assertUpto(I, W, W, W);
		tasks.accept(itemId, STEP1, "Good work again");
		assertUpto(C, I, W, W);
	}

	@Test(dependsOnMethods = "rejectToSibling")
	public void rejectToParentSibling()
	{
		tasks.accept(itemId, STEP2, "Good work on step2");
		assertUpto(C, C, I, W);
		tasks.reject(itemId, STEP3, "Back to step 1", STEP1);
		assertUpto(I, W, W, W);
		tasks.accept(itemId, STEP1, "Good work again");
		assertUpto(C, I, W, W);
	}

	@Test(dependsOnMethods = "rejectToParentSibling")
	public void rejectToParent()
	{
		tasks.accept(itemId, STEP2, "Good work on step2");
		assertUpto(C, C, I, W);
		tasks.accept(itemId, STEP3, "Well done");
		assertUpto(C, C, C, I);
		tasks.reject(itemId, STEP4, "Back to parent", SERIAL);
		assertUpto(C, C, I, W);
		tasks.accept(itemId, STEP3, "Well done again");
		assertUpto(C, C, C, I);
	}

	@Test(dependsOnMethods = "create")
	public void illegalRejects()
	{
		ItemId itemId = items.createId(collectionUuid, "item/name", context.getFullName("Illegal attempts"));
		tasks.rejectFail(itemId, "frogs", "nothing", "nothing");
		tasks.accept(itemId, STEP1, "Good work");
		tasks.accept(itemId, STEP2, "Good work again");
		tasks.rejectFail(itemId, STEP3, "reject doesnt exist", "nothing");
		tasks.rejectFail(itemId, STEP1, "already complete", "nothing");
		tasks.rejectFail(itemId, STEP4, "not moderating yet", "nothing");
		tasks.rejectFail(itemId, STEP3, "not a reject point", STEP2);
		tasks.rejectFail(itemId, STEP3, "not a parent point", STEP4);
	}

}
