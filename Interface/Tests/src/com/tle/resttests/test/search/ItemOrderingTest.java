package com.tle.resttests.test.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.json.entity.Items;
import com.tle.json.requests.SearchRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RestTestConstants;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ItemOrderingTest extends AbstractEntityCreatorTest {
  private static SearchRequests searches;

  @Override
  public void customisePageContext() {
    searches = builder().searches();
    items = builder().items();
  }

  @Test
  public void testOrdering() {
    List<String> names = Lists.newArrayList("Aardvark", "bat", "Cat", "dog");

    for (String name : names) {
      ObjectNode item =
          Items.jsonXml(
              RestTestConstants.COLLECTION_BASIC,
              "<xml><item><name>" + context.getNamePrefix(name) + "</name></item></xml>");
      items.create(item, true);
    }

    ObjectNode search = searches.search(searches.searchRequest(context.getNamePrefix(), "name"));
    String name = null;

    for (JsonNode result : search.get("results")) {
      Assert.assertTrue(
          name == null || name.compareToIgnoreCase(result.get("name").asText()) < 0,
          result.get("name").asText() + " came before " + name);
      name = result.get("name").asText();
    }
  }
}
