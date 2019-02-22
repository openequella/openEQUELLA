package com.tle.resttests.test.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.util.RestTestConstants;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BoostTitleApiTest extends AbstractItemApiTest {
  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>("BoostTest", RestTestConstants.USERID_AUTOTEST));
  }

  @Test
  public void createAndCheck() throws IOException {
    String fullName =
        context.getFullName("Boosted " + UUID.randomUUID().toString().substring(0, 8));
    ItemId unboostedId =
        addDeletable(
            createItem(
                createItemJsonWithValues(COLLECTION_BASIC, "/item/description", fullName)
                    .toString(),
                getToken(),
                "waitforindex",
                true));
    ItemId boostedId =
        addDeletable(
            createItem(
                createItemJsonWithValues(COLLECTION_BASIC, "/item/name", fullName).toString(),
                getToken(),
                "waitforindex",
                true));

    List<ItemId> expected = ImmutableList.of(boostedId, unboostedId);

    List<ItemId> itemIds = Lists.newArrayList();
    JsonNode results =
        getEntity(
            context.getBaseUrl() + "api/search",
            getToken(),
            "q",
            '"' + fullName + '"',
            "order",
            "relevance");
    for (JsonNode result : results.get("results")) {
      String uuid = result.get("uuid").asText();
      int version = result.get("version").asInt();
      itemIds.add(new ItemId(uuid, version));
    }
    Assert.assertEquals(itemIds, expected, "Wrong order return by search");
  }
}
