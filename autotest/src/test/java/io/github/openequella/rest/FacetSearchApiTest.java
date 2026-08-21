package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("facet")
public class FacetSearchApiTest extends AbstractRestApiTest {

  private final String NODE_KEYWORD = "/item/keywords/keyword";
  private final NameValuePair SINGLE_NODE = new NameValuePair("nodes", NODE_KEYWORD);
  private final String NODE_CATEGORY_NAME = "/item/category/@name";
  private final String COLLECTION_PROGRAMMING = "20d40571-e577-4cd7-a12d-d46e5cefcd3f";
  private final String COLLECTION_HARDWARE = "b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e";
  private final String SEARCH_FACET_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/search/facet";
  private final String TERM_ATMEL = "atmel";
  private final String TERM_JVM = "jvm";
  private final String TERM_LISP = "lisp";
  private final String TERM_MULTI = "multi-paradigm";

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(FacetSearchApiTest.class);
    }
    return testConfig;
  }

  @Test
  public void testSingleNode() throws IOException {
    JsonNode result = search(SINGLE_NODE);
    assertResult(result);
  }

  @Test
  public void testMultipleNodes() throws IOException {
    JsonNode result = search(new NameValuePair("nodes", NODE_KEYWORD + "," + NODE_CATEGORY_NAME));
    assertResult(result);
    assertTermPresent(result, "8080,cisc");
  }

  @Test
  public void testSingleCollection() throws IOException {
    JsonNode result = search(SINGLE_NODE, new NameValuePair("collections", COLLECTION_HARDWARE));
    assertResult(result);
    assertTermPresent(result, TERM_ATMEL);
    assertTermAbsent(result, TERM_JVM);
  }

  @Test
  public void testMultipleCollections() throws IOException {
    JsonNode result =
        search(
            SINGLE_NODE,
            new NameValuePair("collections", COLLECTION_HARDWARE + "," + COLLECTION_PROGRAMMING));
    assertResult(result);
    assertTermPresent(result, TERM_ATMEL);
    assertTermPresent(result, TERM_JVM);
  }

  @Test
  public void testOwnerId() throws IOException {
    JsonNode result = search(SINGLE_NODE, new NameValuePair("owner", "TLE_ADMINISTRATOR"));
    assertResult(result);

    JsonNode emptyResult =
        search(SINGLE_NODE, new NameValuePair("owner", "f9ec8b09-cf64-44ff-8a0a-08a8f2f9272a"));
    assertNumberOfResults(emptyResult, 0);
  }

  @Test
  public void testQuery() throws IOException {
    JsonNode result = search(SINGLE_NODE, new NameValuePair("q", "scala"));
    assertResult(result);
    assertTermPresent(result, TERM_JVM);
  }

  @Test
  public void testMimeType() throws IOException {
    JsonNode result = search(SINGLE_NODE, new NameValuePair("mimeTypes", "text/plain"));
    assertResult(result);
    assertTermPresent(result, "Zilog");
  }

  @Test
  public void testMusts() throws IOException {
    JsonNode result =
        search(
            SINGLE_NODE, new NameValuePair("musts", "uuid:8adfcbad-28a7-43af-823c-287279119869"));
    assertNumberOfResults(result, 3);
    assertTermPresent(result, TERM_JVM);
    assertTermPresent(result, TERM_LISP);
    assertTermPresent(result, TERM_MULTI);
  }

  @Test
  public void testNonLiveItem() throws IOException {
    JsonNode noShowAllResult = search(SINGLE_NODE, new NameValuePair("showall", "false"));
    assertResult(noShowAllResult);
    assertTermAbsent(noShowAllResult, "draft");

    JsonNode showAllResult = search(SINGLE_NODE, new NameValuePair("showall", "true"));
    assertTermPresent(showAllResult, "draft");
  }

  @DataProvider(name = "dateRanges")
  public Object[][] dateRanges() {
    int maxResults = 24;
    String startDate = "2020-08-01";
    String endDate = "2020-08-18";
    return new Object[][] {
      // Four item should be returned if searching attachments is enabled or otherwise return 3
      // items.
      {startDate, null, maxResults},
      {null, endDate, maxResults},
      {startDate, endDate, maxResults},
      {endDate, null, 3},
    };
  }

  @Test(dataProvider = "dateRanges")
  public void testDateRanges(String startDate, String endDate, int expectedResults)
      throws IOException {
    JsonNode result =
        search(
            SINGLE_NODE,
            new NameValuePair("modifiedAfter", startDate),
            new NameValuePair("modifiedBefore", endDate));
    assertNumberOfResults(result, expectedResults);
  }

  private JsonNode search(NameValuePair... params) throws IOException {
    final GetMethod method = new GetMethod(SEARCH_FACET_API_ENDPOINT);
    method.setQueryString(params);
    int statusCode = makeClientRequest(method);
    assertEquals(HttpStatus.SC_OK, statusCode);
    return mapper.readTree(method.getResponseBodyAsStream());
  }

  private void assertResult(JsonNode result) {
    assertTrue(result.get("results").size() > 0);
  }

  private void assertNumberOfResults(JsonNode result, int count) {
    assertEquals(result.get("results").size(), count);
  }

  private void assertTermPresent(JsonNode result, String term) throws IOException {
    boolean termPresent =
        StreamSupport.stream(result.get("results").spliterator(), false)
            .anyMatch(node -> term.equals(node.get("term").asText()));
    assertTrue(termPresent);
  }

  private void assertTermAbsent(JsonNode result, String term) throws IOException {
    boolean termAbsent =
        StreamSupport.stream(result.get("results").spliterator(), false)
            .noneMatch(node -> term.equals(node.get("term").asText()));
    assertTrue(termAbsent);
  }
}
