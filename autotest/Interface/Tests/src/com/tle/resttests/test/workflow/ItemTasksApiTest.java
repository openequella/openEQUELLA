package com.tle.resttests.test.workflow;

import static com.tle.json.assertions.ItemStatusAssertions.findStatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.ItemId;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import java.io.IOException;
import org.testng.annotations.Test;

// EQUELLA cannot yet run this test since we don't support the workflow endpoint
// to create a workflow to accept/reject with
@Test(groups = "eps")
public class ItemTasksApiTest extends AbstractRestAssuredTest {
  private static final String TASK1_UUID = "806c8b72-d53b-49e1-8cf1-60b474d0f0ec";
  private TaskRequests tasks;
  private ItemRequests items;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder();
    tasks = builder.tasks();
    items = builder.items();
  }

  private ItemId createItemWithName(String name) throws IOException {
    return items.createId(
        RestTestConstants.COLLECTION_NOTIFICATIONS, true, "item/name", context.getFullName(name));
  }

  @Test
  public void acceptItem() throws Exception {
    ItemId acceptItemId = createItemWithName("Accept");
    ObjectNode task = findTaskToModerate(acceptItemId);
    tasks.accept(task, null);
    ObjectNode moderation = items.moderation(acceptItemId);
    ItemStatusAssertions.assertStatus(moderation, "live");
  }

  private ObjectNode findTaskToModerate(ItemId itemId) {
    return tasks.findTaskToModerate(itemId, context.getNamePrefix(), TASK1_UUID);
  }

  @Test
  public void rejectItem() throws Exception {
    ItemId rejectItemId = createItemWithName("Reject");
    ObjectNode task = findTaskToModerate(rejectItemId);
    String message = "I didn't like it";
    tasks.reject(task, message, null);
    ObjectNode moderation = items.moderation(rejectItemId);
    ItemStatusAssertions.assertRejection(moderation, message, getDefaultUser());
  }

  @Test
  public void commentItem() throws Exception {
    ItemId commentItemId = createItemWithName("Comment");
    ObjectNode task = findTaskToModerate(commentItemId);
    tasks.comment(task, "This is a comment");
    ObjectNode moderation = items.moderation(commentItemId);
    ItemStatusAssertions.assertComment(
        findStatus(moderation, TASK1_UUID), 0, "This is a comment", getDefaultUser(), "comment");
  }
}
