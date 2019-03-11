package com.tle.resttests.test.workflow;

import static com.tle.json.assertions.ItemStatusAssertions.findStatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Function;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.SchedulerRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import java.sql.Date;
import org.testng.annotations.Test;

@Test(groups = "eps")
public class ItemWorkflowAutoActionTest extends AbstractEntityCreatorTest {
  private String collectionUuid;
  private TaskRequests tasks;

  private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";
  private static final String STEP2 = "ff5b5b31-9d83-11e2-9e96-0800200c9a66";
  private static final String STEP3 = "ff5b5b32-9d83-11e2-9e96-0800200c9a66";
  private static final String SERIAL = "ff5b5b33-9d83-11e2-9e96-0800200c9a66";
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
    ObjectNode workflow = Workflows.json("Auto action workflow");
    ObjectNode task1 =
        Workflows.task(STEP1, "Auto accept", false, RestTestConstants.USERID_MODERATOR1);
    task1.put("escalate", "true");
    task1.put("dueDatePath", "/item/dueDate");
    task1.put("autoAction", "accept");
    ObjectNode serial = Workflows.serial(SERIAL, "Reject point", true);
    ObjectNode task2 = Workflows.task(STEP2, "No auto", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task3 =
        Workflows.task(STEP3, "Auto reject", false, RestTestConstants.USERID_MODERATOR1);
    task3.put("escalate", "true");
    task3.put("dueDatePath", "/item/dueDate");
    task3.put("autoAction", "reject");
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow, serial);
    Workflows.child(serial, task2);
    Workflows.child(serial, task3);
    collectionUuid =
        collections.createId(
            CollectionJson.json(
                "Auto action collection",
                RestTestConstants.SCHEMA_BASIC,
                workflows.createId(workflow)));
  }

  @Test(dependsOnMethods = "create")
  public void auto() throws InterruptedException {
    Date overdue = new Date(0L);
    ItemId itemId =
        items.createId(
            collectionUuid,
            true,
            "item/name",
            context.getFullName("Accept automatically"),
            "item/dueDate",
            ISO8601Utils.format(overdue));

    scheduler.execute("com.tle.core.item.workflow.impl.CheckModerationTask");

    ObjectNode task = tasks.findTaskToModerate(itemId, context.getNamePrefix(), STEP2);
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertComment(findStatus(moderation, STEP1), 0, null, "system", "accept");
    tasks.accept(task, null);

    scheduler.execute("com.tle.core.item.workflow.impl.CheckModerationTask");
    task = tasks.findTaskToModerate(itemId, context.getNamePrefix(), STEP2);

    ObjectNode step2Status =
        items.untilModeration(
            itemId,
            new Function<ObjectNode, ObjectNode>() {
              @Override
              public ObjectNode apply(ObjectNode moderation) {
                ObjectNode step2Status = findStatus(moderation, SERIAL, STEP2);
                if (step2Status
                    .get("cause")
                    .get("comments")
                    .get(0)
                    .get("type")
                    .asText()
                    .equals("reject")) {
                  return step2Status;
                }
                return null;
              }
            });

    ItemStatusAssertions.assertComment(step2Status.get("cause"), 0, null, "system", "reject");
    tasks.reject(task, "Get outta here", null);

    moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "rejected");
  }
}
