package com.tle.resttests.test.item;

import static com.tle.json.assertions.ItemStatusAssertions.findStatus;
import static com.tle.resttests.util.RestTestConstants.COLLECTION_ATTACHMENTS;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.assertions.ItemStatusAssertions;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.requests.ItemRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RestTestConstants;
import java.io.IOException;
import org.testng.annotations.Test;

public class ItemApiActionsTest extends AbstractRestAssuredTest {
  private ItemRequests items;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    items = builder().items();
  }

  private ItemId createSimpleItem() throws IOException {
    ObjectNode item = items.create(COLLECTION_ATTACHMENTS);
    asserter.assertStatus(item, "live");
    return items.getId(item);
  }

  @Test
  public void testRedraft() throws IOException {
    ItemId itemId = createSimpleItem();
    ObjectNode item = items.action(itemId, "redraft");
    asserter.assertStatus(item, "draft");
  }

  @Test
  public void testArchive() throws IOException {
    ItemId itemId = createSimpleItem();
    ObjectNode item = items.action(itemId, "archive");
    asserter.assertStatus(item, "archived");
    item = items.action(itemId, "reactivate");
    asserter.assertStatus(item, "live");
  }

  @Test
  public void testSuspend() throws IOException {
    ItemId itemId = createSimpleItem();
    ObjectNode item = items.action(itemId, "suspend");
    asserter.assertStatus(item, "suspended");
    item = items.action(itemId, "resume");
    asserter.assertStatus(item, "live");
  }

  @Test
  public void testRestore() throws IOException {
    ItemId itemId = createSimpleItem();
    items.delete(itemId, false);
    ObjectNode item = items.get(itemId);
    asserter.assertStatus(item, "deleted");
    item = items.action(itemId, "restore");
    asserter.assertStatus(item, "live");
  }

  // EQUELLA not handling workflows at the moment
  @Test(groups = "eps")
  public void testReset() throws IOException {
    ObjectNode item = items.create(RestTestConstants.COLLECTION_MODERATE);
    asserter.assertStatus(item, "live");
    ItemId itemId = items.getId(item);

    // Edit and turn on moderation
    PropBagEx metadata = new PropBagEx();
    metadata.setNode("item/controls/checkboxes", true);
    item.put("metadata", metadata.toString());
    items.editId(item);
    item = items.get(itemId);
    asserter.assertStatus(item, "live");

    item = items.action(itemId, "reset");
    asserter.assertStatus(item, "moderating");
  }

  // EQUELLA not handling workflows at the moment
  @Test(groups = "eps")
  public void testReview() throws IOException {
    ObjectNode item = items.create(RestTestConstants.COLLECTION_MODERATE);
    asserter.assertStatus(item, "live");
    ItemId itemId = items.getId(item);

    // Edit and turn on moderation
    PropBagEx metadata = new PropBagEx();
    metadata.setNode("item/controls/checkboxes", true);
    item.put("metadata", metadata.toString());
    items.editId(item);
    item = items.get(itemId);
    asserter.assertStatus(item, "live");

    item = items.action(itemId, "review");
    asserter.assertStatus(item, "review");
  }

  // EQUELLA not handling workflows at the moment
  @Test(groups = "eps")
  public void testSubmit() throws IOException {
    ObjectNode item =
        Items.json(RestTestConstants.COLLECTION_MODERATE, "item/controls/checkboxes", true);
    item = items.create(item, false, "draft", true);
    asserter.assertStatus(item, "draft");
    ItemId itemId = items.getId(item);

    String submitMessage = "Submit Message";
    item = items.submit(itemId, submitMessage);
    ObjectNode moderation = items.moderation(itemId);
    ItemStatusAssertions.assertStatus(moderation, "moderating");
    ItemStatusAssertions.assertComment(
        findStatus(moderation), 0, submitMessage, getDefaultUser(), "submit");
  }
}
