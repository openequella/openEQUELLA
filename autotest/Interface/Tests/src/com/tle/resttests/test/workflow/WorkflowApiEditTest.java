package com.tle.resttests.test.workflow;

import static com.tle.json.assertions.ItemStatusAssertions.findStatus;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.assertions.ItemStatusAssertions.Status;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Workflows;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractEntityApiEditTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WorkflowApiEditTest extends AbstractEntityApiEditTest {
  private TaskRequests mod1tasks;
  private TaskRequests mod2tasks;

  private static final String STEP1 = UUID.randomUUID().toString();
  private static final String STEP2 = UUID.randomUUID().toString();
  private static final String STEP3 = UUID.randomUUID().toString();
  private static final String SERIAL = UUID.randomUUID().toString();
  private static final String PARALLEL = UUID.randomUUID().toString();

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder();
    mod1tasks = builder.user(RestTestConstants.USERID_MODERATOR1).tasks();
    mod2tasks = builder.user(RestTestConstants.USERID_MODERATOR2).tasks();
  }

  @Test
  public void testModifyUserPath() {
    ObjectNode workflow = Workflows.json("Metadata user workflow");
    ObjectNode metaTask = Workflows.task("Metadata user task", false);
    metaTask.put("userPath", "/item/moderators");
    Workflows.child(workflow.with("root"), metaTask);

    workflow = workflows.create(workflow);
    String collUuid =
        createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflows.getId(workflow));

    String itemName = context.getFullName("Metadata item");
    ObjectNode item =
        items.create(
            Items.json(
                collUuid,
                "item/name",
                itemName,
                "item/moderators",
                RestTestConstants.USERID_MODERATOR1,
                "item/moderators2",
                RestTestConstants.USERID_MODERATOR2),
            true);
    ItemId itemId = items.getId(item);
    findTaskToModerate(mod1tasks, itemId, null);
    asserter.assertStatus(item, "moderating");

    metaTask = (ObjectNode) workflow.get("root").get("nodes").get(0);
    metaTask.put("userPath", "/item/moderators2");
    workflows.editId(workflow);
    getTokens().invalidate(RestTestConstants.USERID_MODERATOR2);
    findTaskToModerate(mod2tasks, itemId, null);
  }

  @Test
  public void testModifyUsers() {
    ObjectNode workflow = Workflows.json("Fixed users workflow");
    ObjectNode task = Workflows.task("Fixed user task", false, RestTestConstants.USERID_MODERATOR1);
    Workflows.child(workflow.with("root"), task);

    workflow = workflows.create(workflow);
    String collUuid =
        createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflows.getId(workflow));

    String itemName = context.getFullName("Fixed item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);
    findTaskToModerate(mod1tasks, itemId, null);
    asserter.assertStatus(item, "moderating");

    task = (ObjectNode) workflow.get("root").get("nodes").get(0);
    Workflows.setUsers(task, RestTestConstants.USERID_MODERATOR2);
    workflows.editId(workflow);
    getTokens().invalidate(RestTestConstants.USERID_MODERATOR2);
    findTaskToModerate(mod2tasks, itemId, null);
  }

  @Test
  public void testModifyOrder() {
    ObjectNode workflow = Workflows.json("Ordering workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR2);
    ObjectNode task3 = Workflows.task(STEP3, "Step 3", false, RestTestConstants.USERID_MODERATOR2);
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow, task2);
    Workflows.rootChild(workflow, task3);

    workflow = workflows.create(workflow);
    String workflowId = workflows.getId(workflow);
    String collUuid = createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflowId);

    String itemName = context.getFullName("Fixed item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);
    mod1tasks.accept(findTaskToModerate(mod1tasks, itemId, STEP1), null);
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "moderating");

    JsonNode rootChildren = workflow.get("root").get("nodes");
    ((ObjectNode) rootChildren.get(0)).put("uuid", STEP2);
    ((ObjectNode) rootChildren.get(1)).put("uuid", STEP1);
    workflows.editResponse(workflow, workflowId);
    moderation = items.moderation(itemId);
    ObjectNode task2accept = findTaskToModerate(mod1tasks, itemId, STEP2);
    ItemStatusAssertions.assertNodeStatus(findStatus(moderation, STEP2), Status.I);
    ItemStatusAssertions.assertNodeStatus(findStatus(moderation, STEP3), Status.W);
    mod1tasks.accept(task2accept, null);
    mod2tasks.accept(findTaskToModerate(mod2tasks, itemId, STEP3), null);
    moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  @Test
  public void testDelete() {
    ObjectNode workflow = Workflows.json("Deleting workflow");
    ObjectNode task1 =
        Workflows.task(
            STEP1,
            "Step 1",
            false,
            RestTestConstants.USERID_MODERATOR1,
            RestTestConstants.USERID_MODERATOR2);
    task1.put("unanimousacceptance", true);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR2);
    ObjectNode task3 = Workflows.task(STEP3, "Step 3", false, RestTestConstants.USERID_MODERATOR2);
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow, task2);
    Workflows.rootChild(workflow, task3);

    workflow = workflows.create(workflow);
    String workflowId = workflows.getId(workflow);
    String collUuid = createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflowId);

    String itemName = context.getFullName("Deleting item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);

    mod1tasks.accept(itemId, STEP1, null);
    ArrayNode rootChildren = (ArrayNode) workflow.get("root").get("nodes");
    rootChildren.remove(0);
    workflows.editResponse(workflow, workflowId);

    findTaskToModerate(mod2tasks, itemId, STEP2);
    mod2tasks.accept(itemId, STEP2, null);
    mod2tasks.accept(itemId, STEP3, null);
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  @Test
  public void testReparent() {
    ObjectNode workflow = Workflows.json("Reparent workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR2);
    ObjectNode serial = Workflows.serial(SERIAL, "Serial", true);
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow, serial);
    Workflows.child(serial, task2);

    workflow = workflows.create(workflow);
    String workflowId = workflows.getId(workflow);
    String collUuid = createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflowId);

    String itemName = context.getFullName("Reparent item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);

    mod1tasks.accept(itemId, STEP1, null);

    ArrayNode rootChildren = (ArrayNode) workflow.get("root").get("nodes");
    task2 = (ObjectNode) ((ArrayNode) rootChildren.get(1).get("nodes")).remove(0);
    rootChildren.insert(0, task2);
    workflows.editResponse(workflow, workflowId);

    ObjectNode moderation = items.moderation(itemId);
    mod2tasks.accept(findTaskToModerate(mod2tasks, itemId, STEP2), null);
    moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  // Doing this used to throw constraint violations
  @Test
  public void testDeepDelete() throws Exception {
    ObjectNode workflow = Workflows.json("Deep delete workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR2);
    ObjectNode parallel = Workflows.parallel(PARALLEL, "Parallel", true);
    Workflows.rootChild(workflow, parallel);
    Workflows.child(parallel, task1);
    Workflows.child(parallel, task2);

    workflow = workflows.create(workflow);
    String workflowId = workflows.getId(workflow);
    String collUuid = createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflowId);

    String itemName = context.getFullName("Deep delete item");
    ItemId itemId = items.getId(items.create(Items.json(collUuid, "item/name", itemName), true));

    mod1tasks.accept(itemId, STEP1, null);

    ArrayNode rootChildren = (ArrayNode) workflow.get("root").get("nodes");
    rootChildren.removeAll();

    ObjectNode task3 = Workflows.task(STEP3, "Step 3", false, RestTestConstants.USERID_MODERATOR1);
    Workflows.rootChild(workflow, task3);
    workflows.editResponse(workflow, workflowId);

    ObjectNode moderation = items.moderation(itemId);
    mod1tasks.accept(findTaskToModerate(mod1tasks, itemId, STEP3), null);
    moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  @Test
  public void testPriorityChange() {
    ObjectNode workflow = Workflows.json("Priority workflow");
    ObjectNode para = Workflows.parallel(null, "Parallel", false);
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    task1.put("priority", 20);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR1);
    task2.put("priority", 30);
    Workflows.rootChild(workflow, para);
    Workflows.child(para, task1);
    Workflows.child(para, task2);

    workflow = workflows.create(workflow);
    String workflowId = workflows.getId(workflow);
    String collUuid = createSimpleCollection(RestTestConstants.SCHEMA_BASIC, workflowId);

    String itemName = context.getFullName("Priority item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);
    RequestSpecification searchRequest =
        mod1tasks.searchRequest('"' + itemName + '"', null, "priority", "50");
    ObjectNode results = mod1tasks.search(searchRequest);
    assertTrue(TaskRequests.taskOrder(itemId, STEP2, STEP1).apply(results), "Wrong task order");

    task1 = (ObjectNode) workflow.get("root").get("nodes").get(0).get("nodes").get(0);
    task1.put("priority", 40);
    workflows.editResponse(workflow, workflowId);
    mod1tasks.waitUntil(searchRequest, TaskRequests.taskOrder(itemId, STEP1, STEP2));
  }

  @Test
  public void testChangeCollection() {
    ObjectNode workflow = Workflows.json("Change1 workflow");
    ObjectNode workflow2 = Workflows.json("Change2 workflow");
    ObjectNode task1 = Workflows.task(STEP1, "Step 1", false, RestTestConstants.USERID_MODERATOR1);
    ObjectNode task2 = Workflows.task(STEP2, "Step 2", false, RestTestConstants.USERID_MODERATOR2);
    Workflows.rootChild(workflow, task1);
    Workflows.rootChild(workflow2, task2);

    String workflowId = workflows.createId(workflow);
    String workflowId2 = workflows.createId(workflow2);

    ObjectNode collection =
        CollectionJson.json(
            context.getFullName("Change collection"), RestTestConstants.SCHEMA_BASIC, workflowId);
    collection = collections.create(collection);
    String collUuid = collections.getId(collection);

    String itemName = context.getFullName("Change item");
    ObjectNode item = items.create(Items.json(collUuid, "item/name", itemName), true);
    ItemId itemId = items.getId(item);

    collection.with("workflow").put("uuid", workflowId2);
    collections.editId(collection);

    ObjectNode moderation = items.moderation(itemId);
    Assert.assertNotNull(ItemStatusAssertions.findStatus(moderation, STEP2));

    mod2tasks.findTaskToModerate(itemId, context.getNamePrefix(), STEP2);
    collection.remove("workflow");
    collections.editId(collection);
    items.untilModeration(itemId, ItemStatusAssertions.statusIs("live"));
  }

  private ObjectNode findTaskToModerate(TaskRequests tasks, ItemId itemId, String task) {
    return tasks.findTaskToModerate(itemId, task, task);
  }

  private String createSimpleCollection(String schemaUuid, String workflowUuid) {
    ObjectNode collection =
        CollectionJson.json(context.getFullName("Simple collection"), schemaUuid, workflowUuid);
    return collections.createId(collection);
  }

  @Override
  protected BaseEntityRequests createRequestsWithBuilder(RequestsBuilder builder) {
    return builder.workflows();
  }

  @Override
  protected ObjectNode createJsonForPrivs(String fullName) {
    return Workflows.json(fullName);
  }

  @Override
  protected ObjectNode createJsonForEdit(String fullName) {
    return Workflows.json(fullName);
  }

  @Override
  protected String getEditPrivilege() {
    return "EDIT_WORKFLOW";
  }

  @Override
  protected void assertExtraEdits(ObjectNode edited) {
    // nothing
  }

  @Override
  protected void extraEdits(ObjectNode client) {
    // nothing
  }
}
