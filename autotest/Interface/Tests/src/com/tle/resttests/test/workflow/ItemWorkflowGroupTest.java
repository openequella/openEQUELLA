package com.tle.resttests.test.workflow;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import java.util.Map;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

public class ItemWorkflowGroupTest extends AbstractEntityCreatorTest {
  private String collectionUuid;
  private TaskRequests tasks1;
  private TaskRequests tasks2;
  private TaskRequests tasks3;

  private static final String STEP1 = "ff5b5b30-9d83-11e2-9e96-0800200c9a66";
  private static final String STEP2 = "ff5b5b31-9d83-11e2-9e96-0800200c9a66";
  private static final String STEP3 = "ff5b5b32-9d83-11e2-9e96-0800200c9a66";

  private Map<TaskRequests, String> userMap = Maps.newHashMap();

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder();
    tasks1 = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
    tasks2 = builder.user(RestTestConstants.USERID_MODERATOR2).tasks();
    tasks3 = builder.user(RestTestConstants.USERID_AUTOTEST).tasks();
    userMap.put(tasks1, RestTestConstants.USERID_MODERATOR1);
    userMap.put(tasks2, RestTestConstants.USERID_MODERATOR2);
    userMap.put(tasks3, RestTestConstants.USERID_AUTOTEST);
  }

  @Test
  public void create() {
    ObjectNode workflow = Workflows.json("Workflow Task workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Unanimous group", false);
    task1.put("unanimousacceptance", true);
    Workflows.setGroups(task1, RestTestConstants.GROUPID_MODERATORS);
    ObjectNode task2 = Workflows.task(STEP2, "First user group", false);
    Workflows.setGroups(task2, RestTestConstants.GROUPID_MODERATORS);
    ObjectNode task3 = Workflows.task(STEP3, "Role moderate", false);
    Workflows.setRoles(task3, RestTestConstants.ROLEID_EVERYONE);
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow, task2);
    Workflows.rootChild(workflow, task3);
    String workflowUuid = workflows.createId(workflow);
    collectionUuid =
        collections.createId(
            CollectionJson.json(
                "Workflow Task collection", RestTestConstants.SCHEMA_BASIC, workflowUuid));
  }

  @Test(dependsOnMethods = "create")
  public void moderate() {
    ItemId itemId =
        items.createId(collectionUuid, true, "item/name", context.getFullName("Groups and roles"));
    tasks1.accept(itemId, STEP1, "Good work");
    tasks2.accept(itemId, STEP1, "Good work2");
    tasks2.accept(itemId, STEP2, "Good work3");
    tasks3.accept(itemId, STEP3, "Good work4");
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  private ObjectNode findTaskToModerate(TaskRequests tasks, ItemId itemId, String task) {
    String userId = userMap.get(tasks);

    getTokens().invalidate(userId);
    return tasks.findTaskToModerate(itemId, context.getNamePrefix(), task);
  }

  @Test(dependsOnMethods = "create")
  public void checkIndex() {
    ItemId itemId =
        items.createId(
            collectionUuid, true, "item/name", context.getFullName("Groups and roles indexing"));
    tasks1.accept(findTaskToModerate(tasks1, itemId, STEP1), "Good work");
    tasks2.accept(findTaskToModerate(tasks2, itemId, STEP1), "Good work2");
    tasks2.accept(findTaskToModerate(tasks2, itemId, STEP2), "Good work3");
    tasks3.accept(findTaskToModerate(tasks3, itemId, STEP3), "Good work4");
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }
}
