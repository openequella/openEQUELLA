package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.common.Pair;
import java.net.URI;
import java.util.List;
import org.testng.annotations.Test;

public class DynaCollectionApiTest extends AbstractRestApiTest {
  private static final String API_DYNACOLL_PATH = "api/dynacollection";

  /**
   * Note this test assumes the pre-existence of a Dynamic Collection with exactly this name, and it
   * applicability restricted to search(Usage), with 4 items within its definition, 2 of those items
   * with date_last_modified being before march 2013
   */
  private static final String OAUTH_CLIENT_ID = "DynaCollectionApiTestClient";

  /**
   * Test assumes existence of manual virtual value dyna collection with this UUID and 'giant' as
   * one of its virtual values. (Also required by SearchApiTest)
   */
  public static final String KNOWN_MANUAL_VIRTUAL_DYNACOLL_UUID =
      "637314db-75b6-4d8e-81ea-41a1c64a9e9a";

  public static final String GIANT = "giant";
  private static final String KNOWN_MANUAL_VIRTUAL_VALUE_GIANT =
      SearchApiTest.SEARCH_API_TEST
          + "ClientDynamic ("
          + GIANT
          + ")"; // also "average", "miniature"

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void dynaCollSearchTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_DYNACOLL_PATH);

    // confirm that a search returns our expected pre-existing dynamic
    // collection
    JsonNode result = getEntity(uri.toString(), token);
    int available = result.get("available").asInt();
    assertTrue(available >= 1);
    assertNotNull(containsInSearchBeanResult(result, OAUTH_CLIENT_ID, "name"));

    // repeat the search, but limit to usage = 'search' only
    result = getEntity(uri.toString(), token, "usage", "search");
    available = result.get("available").asInt();
    assertTrue(available >= 1);
    assertNotNull(containsInSearchBeanResult(result, OAUTH_CLIENT_ID, "name"));

    // NB: ' ...?usage=searchUsage ' is also fine
    result = getEntity(uri.toString(), token, "usage", "searchUsage");
    available = result.get("available").asInt();
    assertTrue(available >= 1);
    assertNotNull(containsInSearchBeanResult(result, OAUTH_CLIENT_ID, "name"));

    // repeat the search, now limit to usage = 'harvester' only
    result = getEntity(uri.toString(), token, "usage", "harvester");
    available = result.get("available").asInt();
    assertTrue(available == 0);
  }

  @Test(dependsOnMethods = {"dynaCollSearchTest"})
  public void dynaCollGetSingleDynaCol() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri =
        new URI(
            context.getBaseUrl() + API_DYNACOLL_PATH + '/' + KNOWN_MANUAL_VIRTUAL_DYNACOLL_UUID);

    JsonNode result = getEntity(uri.toString(), token);

    // append the uuid so as to get a single dynamic collection
    assertEquals(result.get("uuid").asText(), KNOWN_MANUAL_VIRTUAL_DYNACOLL_UUID);
    assertEquals(result.get("name").asText(), SearchApiTest.OAUTH_CLIENT_ID + "Dynamic");
    assertTrue(result.get("usages").size() == 1);
    assertEquals(result.get("usages").get(0).asText(), "searchUsage");

    result = getEntity(uri.toString() + ':' + GIANT, token);

    String expandedVirualisedName = result.get("name").asText();
    assertEquals(
        expandedVirualisedName,
        KNOWN_MANUAL_VIRTUAL_VALUE_GIANT,
        "Bad result:" + result.toString());
  }
}
