package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

public class Search2ApiTest extends AbstractRestApiTest {
  private static final String SEARCH_API_ENDPOINT = TEST_CONFIG.getInstitutionUrl() + "api/search2";

  @Test(description = "Search without parameters")
  public void noParamSearchTest() throws IOException {
    JsonNode result = doSearch(200);
    assertTrue(result.get("available").asInt() > 0);
  }

  @Test(description = "Search for a specific item")
  public void queryTest() throws IOException {
    JsonNode result =
        doSearch(200, new NameValuePair("query", "ActivationApiTest - Book Holding Clone"));
    assertEquals(result.get("available").asInt(), 1);
  }

  @Test(description = "Search for specific items by Item status")
  public void itemStatusTest() throws IOException {
    JsonNode result =
        doSearch(200, new NameValuePair("query", "loaf"), new NameValuePair("status", "LIVE"));
    assertEquals(result.get("available").asInt(), 3);
  }

  @Test(description = "Search for items from a specific collection")
  public void collectionTest() throws IOException {
    JsonNode result =
        doSearch(200, new NameValuePair("collections", "4c147089-cddb-e67c-b5ab-189614eb1463"));
    assertEquals(result.get("available").asInt(), 2);
  }

  @Test(description = "Search for items belonging to a specific owner")
  public void ownerTest() throws IOException {
    JsonNode result =
        doSearch(200, new NameValuePair("owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58"));
    assertTrue(result.get("available").asInt() > 0);
  }

  @Test(description = "Search by items' created date.")
  public void orderTest() throws IOException {
    JsonNode result = doSearch(200, new NameValuePair("order", "created"));
    assertEquals(
        result.get("results").get(0).get("uuid").asText(), "9b9bf5a9-c5af-490b-88fe-7e330679fad2");
  }

  @Test(description = "Search by items' modified date and the order of results is reversed.")
  public void reverseOrderTest() throws IOException {
    JsonNode result =
        doSearch(
            200, new NameValuePair("order", "modified"), new NameValuePair("reverseOrder", "true"));
    assertEquals(
        result.get("results").get(0).get("uuid").asText(), "072c40d8-c8a8-412d-8ad2-3ef188ea016d");
  }

  @Test(description = "Search within a specific date range")
  public void modifiedDateRangeTest() throws IOException {
    JsonNode result =
        doSearch(
            200,
            new NameValuePair("modifiedAfter", "2014-04-01"),
            new NameValuePair("modifiedBefore", "2014-04-30"));
    assertEquals(result.get("available").asInt(), 6);
  }

  @Test(description = "Limit the search result to include only 2 items")
  public void startLengthTest() throws IOException {
    JsonNode result =
        doSearch(200, new NameValuePair("start", "1"), new NameValuePair("length", "2"));
    assertEquals(result.get("results").size(), 2);
  }

  @Test(description = "Search by a specific where clause")
  public void whereClauseTest() throws IOException {
    JsonNode result =
        doSearch(
            200,
            new NameValuePair(
                "whereClause",
                "where /xml/item/itembody/name = 'ActivationApiTest - Book Holding Clone'"));
    assertEquals(result.get("available").asInt(), 1);
  }

  @Test(description = "Search by an invalid item status")
  public void invalidItemStatusSearch() throws IOException {
    doSearch(400, new NameValuePair("status", "ALIVE"));
  }

  @Test(description = "Search from a non-existing collection")
  public void invalidCollectionSearch() throws IOException {
    doSearch(404, new NameValuePair("collections", "bad collection"));
  }

  @Test(description = "Search by a invalid date format")
  public void invalidDateSearch() throws IOException {
    doSearch(400, new NameValuePair("modifiedAfter", "2020/05/05"));
  }

  private JsonNode doSearch(int expectedCode, NameValuePair... queryVals) throws IOException {
    final HttpMethod method = new GetMethod(SEARCH_API_ENDPOINT);
    if (queryVals != null) {
      method.setQueryString(queryVals);
    }
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, expectedCode);

    return mapper.readTree(method.getResponseBody());
  }
}
