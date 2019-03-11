package com.tle.resttests.test.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.SchedulerRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import org.testng.annotations.Test;

@Test(groups = "eps")
public class ItemWorkflowReviewTest extends AbstractEntityCreatorTest {
  private TaskRequests tasks;

  private ItemId itemId;

  private String collectionUuid;

  private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";

  private SchedulerRequests scheduler;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder();
    tasks = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
    scheduler = builder.scheduler();
  }

  @Test
  public void create() {
    ObjectNode workflow = Workflows.json("Review workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    Workflows.rootChild(workflow, task1);

    String workflowUuid = workflows.createId(workflow);
    ObjectNode collection =
        CollectionJson.json("Review collection", RestTestConstants.SCHEMA_BASIC, workflowUuid);
    collection.put("reviewPeriod", 0);
    collectionUuid = collections.createId(collection);

    itemId = items.createId(collectionUuid, true, "item/name", context.getFullName("Review Item"));
  }

  @Test(dependsOnMethods = "create")
  public void straightIntoReview() {
    tasks.accept(itemId, STEP1, "Good work");

    scheduler.execute("com.tle.core.item.workflow.impl.CheckReviewTask");

    tasks.findTaskToModerate(itemId, context.getNamePrefix(), STEP1);
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "review");
  }
}
