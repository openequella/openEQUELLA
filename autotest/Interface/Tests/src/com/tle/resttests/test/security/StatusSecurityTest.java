package com.tle.resttests.test.security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.AclLists;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.Items;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import org.testng.annotations.Test;

public class StatusSecurityTest extends AbstractEntityCreatorTest {
  @Override
  public void customisePageContext() {
    RequestsBuilder builder = builder().user(RestTestConstants.USERID_AUTOTEST);
    items = builder.items();
    collections = builder.collections();
  }

  @Test
  public void statusTest() {
    String itemName = "StatusSecurityTest";

    ObjectNode coll =
        CollectionJson.json(
            context.getFullName("StatusCollection"), RestTestConstants.SCHEMA_BASIC, null);
    CollectionJson.addStatusRule(
        coll.with("security"), "draft", AclLists.rule("EDIT_ITEM", false, false, "*"));
    coll = collections.create(coll);
    String collId = collections.getId(coll);
    ObjectNode item =
        items.create(
            Items.jsonXml(
                collId,
                "<xml><item><name>" + context.getFullName(itemName) + "</name></item></xml>"),
            false,
            "draft",
            true);
    Items.editMetadata(item, "item/name", context.getFullName("Edited"));
    items.editNoPermission(item);
    coll.with("security").with("statuses").withArray("draft").removeAll();
    collections.editId(coll);
    items.editId(item);
  }
}
