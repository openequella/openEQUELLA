package com.tle.resttests.test.workflow;

import static com.tle.json.assertions.ItemStatusAssertions.findStatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ItemWorkflowParallelTest extends AbstractEntityCreatorTest {
  private TaskRequests tasks;

  private String collectionUuid;

  private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";
  private static final String STEP2 = "ff5b5b31-9d83-11e2-9e96-0800200c9a66";
  private static final String PARA = "ff5b5b32-9d83-11e2-9e96-0800200c9a66";

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder();
    tasks = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
  }

  @Test
  public void create() {
    ObjectNode workflow = Workflows.json("Parallel workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode para = Workflows.parallel(PARA, "Parallel step", true);

    Workflows.rootChild(workflow, para);
    Workflows.child(para, task1);
    Workflows.child(para, task2);

    String workflowUuid = workflows.createId(workflow);
    collectionUuid =
        collections.createId(
            CollectionJson.json(
                "Parallel collection", RestTestConstants.SCHEMA_BASIC, workflowUuid));
  }

  @Test(dependsOnMethods = "create")
  public void modInParallel() {
    ItemId itemId =
        items.createId(collectionUuid, true, "item/name", context.getFullName("Parallel Item"));
    ObjectNode moderation = items.moderation(itemId);
    ObjectNode step1status = findStatus(moderation, PARA, STEP1);
    ObjectNode step2status = findStatus(moderation, PARA, STEP2);
    Assert.assertNotNull(step1status.get("started"));
    Assert.assertNotNull(step2status.get("started"));
    tasks.accept(itemId, STEP2, "Step 2 first");
    tasks.accept(itemId, STEP1, "Back to step 1");
    moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }
}
