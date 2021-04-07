package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Search2ApiTest extends AbstractRestApiTest {
  private final String SEARCH_API_ENDPOINT = getTestConfig().getInstitutionUrl() + "api/search2";

  @Test(description = "Search without parameters")
  public void noParamSearchTest() throws IOException {
    JsonNode result = doSearch(200, null);
    assertTrue(getAvailable(result) > 0);
  }

  @Test(description = "Search for a specific item")
  public void queryTest() throws IOException {
    JsonNode result = doSearch(200, "ActivationApiTest - Book Holding Clone");
    final Set<String> highlights =
        StreamSupport.stream(result.get("highlight").spliterator(), false)
            .map(JsonNode::asText)
            .collect(Collectors.toSet());

    assertEquals(getAvailable(result), 1);
    assertEquals(
        highlights, new HashSet<>(Arrays.asList("ActivationApiTest", "Book", "Holding", "Clone")));
  }

  @Test(description = "Search for specific items by Item status")
  public void itemStatusTest() throws IOException {
    JsonNode result = doSearch(200, "loaf", new NameValuePair("status", "LIVE"));
    assertEquals(getAvailable(result), 3);
  }

  @Test(description = "Search for items from a specific collection")
  public void collectionTest() throws IOException {
    JsonNode result =
        doSearch(
            200, null, new NameValuePair("collections", "4c147089-cddb-e67c-b5ab-189614eb1463"));
    assertEquals(getAvailable(result), 2);
  }

  @Test(description = "Search for items belonging to a specific owner")
  public void ownerTest() throws IOException {
    JsonNode result =
        doSearch(200, null, new NameValuePair("owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58"));
    assertTrue(getAvailable(result) > 0);
  }

  @Test(description = "Search by items' created date.")
  public void orderTest() throws IOException {
    doSearch(200, null, new NameValuePair("order", "created"));
  }

  @Test(description = "Search by items' modified date and the order of results is reversed.")
  public void reverseOrderTest() throws IOException {
    doSearch(
        200,
        null,
        new NameValuePair("order", "modified"),
        new NameValuePair("reverseOrder", "true"));
  }

  @Test(description = "Search within a specific date range")
  public void modifiedDateRangeTest() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair("modifiedAfter", "2014-04-01"),
            new NameValuePair("modifiedBefore", "2014-04-30"));
    assertEquals(getAvailable(result), 6);
  }

  @Test(description = "Limit the search result to include only 2 items")
  public void startLengthTest() throws IOException {
    JsonNode result =
        doSearch(200, null, new NameValuePair("start", "1"), new NameValuePair("length", "2"));
    assertEquals(result.get("results").size(), 2);
  }

  @Test(description = "Search for a term that is found inside an attachment")
  public void termFoundInAttachmentTest() throws IOException {
    // Perform a search for a term found inside item attachment content
    JsonNode itemResult = doSearch(200, "frogs").get("results").get(0);
    assertTrue(itemResult.get("keywordFoundInAttachment").asBoolean());
  }

  @Test(description = "Search for a term that is not found inside an attachment")
  public void noTermFoundInAttachmentTest() throws IOException {
    // Perform a search for a term found inside item metadata
    JsonNode itemResult =
        doSearch(200, "Keyword found in attachment test item").get("results").get(0);
    assertFalse(itemResult.get("keywordFoundInAttachment").asBoolean());
  }

  @Test(description = "Search by a specific where clause")
  public void whereClauseTest() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair(
                "whereClause",
                "where /xml/item/itembody/name = 'ActivationApiTest - Book Holding Clone'"));
    assertEquals(getAvailable(result), 1);
  }

  @Test(description = "Search by an invalid item status")
  public void invalidItemStatusSearch() throws IOException {
    doSearch(404, null, new NameValuePair("status", "ALIVE"));
  }

  @Test(description = "Search from a non-existing collection")
  public void invalidCollectionSearch() throws IOException {
    doSearch(404, null, new NameValuePair("collections", "bad collection"));
  }

  @Test(description = "Search by a invalid date format")
  public void invalidDateSearch() throws IOException {
    doSearch(400, null, new NameValuePair("modifiedAfter", "2020/05/05"));
  }

  @DataProvider(name = "searchAttachmentsData")
  public static Object[][] searchAttachmentsData() {
    return new Object[][] {
      // Four item should be returned if searching attachments is enabled or otherwise return 3
      // items.
      {true, 4}, {false, 3}
    };
  }

  @Test(
      description = "Include or exclude attachments in a search",
      dataProvider = "searchAttachmentsData")
  public void notSearchAttachments(boolean searchAttachment, int expectNumber) throws IOException {
    JsonNode result =
        doSearch(
            200,
            "size",
            new NameValuePair("searchAttachments", Boolean.toString(searchAttachment)));
    assertEquals(getAvailable(result), expectNumber);
  }

  @Test(description = "Search for a known MIME type")
  public void validMimeTypeSearch() throws IOException {
    JsonNode result = doSearch(200, null, new NameValuePair("mimeTypes", "text/plain"));
    assertEquals(getAvailable(result), 6);
  }

  @Test(description = "Search for a known MIME type with has no items")
  public void absentMimeTypeSearch() throws IOException {
    JsonNode result =
        doSearch(200, null, new NameValuePair("mimeTypes", "application/illustrator"));
    assertEquals(getAvailable(result), 0);
  }

  @Test(description = "Filter search by a list of 'musts'")
  public void validMustsSearch() throws IOException {
    JsonNode result = doSearch(200, null, new NameValuePair("musts", "moderating:true"));
    assertEquals(getAvailable(result), 9);

    result =
        doSearch(
            200,
            null,
            new NameValuePair("musts", "moderating:true"),
            new NameValuePair("musts", "uuid:ab16b5f0-a12e-43f5-9d8b-25870528ad41"),
            new NameValuePair("musts", "uuid:24b977ec-4df4-4a43-8922-8ca6f82a296a"));
    assertEquals(getAvailable(result), 2);

    result =
        doSearch(
            200, null, new NameValuePair("musts", "uuid:ab16b5f0-a12e-43f5-9d8b-25870528ad41"));
    assertEquals(getAvailable(result), 1);
  }

  @DataProvider(name = "badMustExpressions")
  public static Object[][] badMustExpressions() {
    return new Object[][] {
      {"tooFewDelimiter"}, {"too:many:delimiter"}, {":emptyField"},
      {"emptyValue:"}, {":"}, {""}
    };
  }

  @Test(
      description = "Report error with incorrectly formatted 'musts' expressions",
      dataProvider = "badMustExpressions")
  public void invalidMustsSearch(String badMust) throws IOException {
    doSearch(400, null, new NameValuePair("musts", badMust));
  }

  @Test(description = "Ensure the 'externalId' link is present in YouTube attachments.")
  public void ensureExternalIdIsPresent() throws IOException {
    final JsonNode result = doSearch(200, null, new NameValuePair("musts", "videothumb:true"));
    assertEquals(getAvailable(result), 1); // Confirm we've kind of got what we expect

    final JsonNode attachments =
        Optional.of(result.get("results"))
            .map(results -> results.get(0))
            .map(item -> item.get("attachments"))
            .orElseThrow(
                () -> new IllegalStateException("Failed to access attachments for validation"));

    final Iterable<JsonNode> attachmentsIterable = attachments::getElements;
    final Predicate<JsonNode> isYouTubeAttachment =
        attachment ->
            Optional.of(attachment.get("attachmentType"))
                .map(JsonNode::asText)
                .map(typeString -> typeString.compareTo("custom/youtube") == 0)
                .orElse(false);
    final Predicate<JsonNode> hasExternalId =
        attachment ->
            Optional.of(attachment.get("links")).map(links -> links.get("externalId")).isPresent();

    final long validAttachments =
        StreamSupport.stream(attachmentsIterable.spliterator(), false)
            .filter(isYouTubeAttachment)
            .filter(hasExternalId)
            .count();

    assertEquals(validAttachments, 1);
  }

  private JsonNode doSearch(int expectedCode, String query, NameValuePair... queryVals)
      throws IOException {
    final HttpMethod method = new GetMethod(SEARCH_API_ENDPOINT);
    final List<NameValuePair> queryParams = new LinkedList<>();

    if (query != null) {
      queryParams.add(new NameValuePair("query", query));
    }
    if (queryVals != null) {
      queryParams.addAll(Arrays.asList(queryVals));
    }
    method.setQueryString(queryParams.toArray(new NameValuePair[0]));

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, expectedCode);

    return mapper.readTree(method.getResponseBody());
  }

  private int getAvailable(JsonNode result) {
    return result.get("available").asInt();
  }
}
