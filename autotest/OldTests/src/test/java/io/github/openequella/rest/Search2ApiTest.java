package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Search2ApiTest extends AbstractRestApiTest {
  private final String SEARCH_API_ENDPOINT = getTestConfig().getInstitutionUrl() + "api/search2";
  private final String VIRTUAL_HIERARCHY_TOPIC =
      "886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==";
  private final String NORMAL_HIERARCHY_TOPIC = "6135b550-ce1c-43c2-b34c-0a3cf793759d";

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
    assertEquals(getAvailable(result), 8);
  }

  @Test(description = "Search for items belonging to a specific owner")
  public void ownerTest() throws IOException {
    JsonNode result =
        doSearch(200, null, new NameValuePair("owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58"));
    assertTrue(getAvailable(result) > 0);
  }

  @Test(description = "Search for items and order them by their created date.")
  public void orderTest() throws IOException {
    doSearch(200, null, new NameValuePair("order", "created"));
    // TODO check if the sort even had an effect - maybe compare date of first vs last
  }

  @Test(description = "Search by items' created date.")
  public void orderModerationItemsTest() throws IOException {
    // order=task_lastaction&status=REJECTED&status=MODERATING&status=REVIEW
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair("order", "task_lastaction"),
            new NameValuePair("status", "MODERATING"),
            new NameValuePair("status", "REJECTED"),
            new NameValuePair("status", "REVIEW"));
    final int available = getAvailable(result);

    assertTrue(available > 1);

    final List<String> lastActionDates =
        buildResultsStream(result)
            .map(
                r ->
                    Optional.ofNullable(r.get("moderationDetails"))
                        .flatMap(details -> Optional.ofNullable(details.get("lastActionDate"))))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(JsonNode::isTextual)
            .map(JsonNode::textValue)
            .collect(Collectors.toList());

    assertEquals(
        lastActionDates.size(), available, "Number of dates does not match number of results!");

    // sorting order of 'task_lastaction' should see the first item have the oldest date and the
    // last action have the most recent date.
    final String oldestDate = lastActionDates.get(0);
    final String mostRecentDate = lastActionDates.get(lastActionDates.size() - 1);

    // Being ISO dates we can just use simple string comparison
    assertEquals(
        lastActionDates.stream()
            .filter(isoString -> oldestDate.compareToIgnoreCase(isoString) > 0)
            .count(),
        0);
    assertEquals(
        lastActionDates.stream()
            .filter(isoString -> mostRecentDate.compareToIgnoreCase(isoString) < 0)
            .count(),
        0);
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

  @Test(description = "Test that attachment details can be optionally excluded")
  public void excludeAttachments() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair("mimeTypes", "application/pdf"),
            new NameValuePair("includeAttachments", "false"));
    // Make sure we've got items to test with
    assertTrue(getAvailable(result) > 0);

    // First, let's make sure we only have items with attachments
    final Predicate<JsonNode> hasOneOrMoreAttachments =
        item ->
            Optional.of(item.get("attachmentCount"))
                .map(JsonNode::asInt)
                .map(n -> n > 0)
                .orElse(false);
    assertEquals(
        buildResultsStream(result).filter(hasOneOrMoreAttachments).count(),
        getAvailable(result),
        "Expected only items with attachments.");

    // Next, make sure all the attachments have been excluded - i.e. no 'attachments' element
    final Predicate<JsonNode> hasNoAttachments = item -> item.get("attachments") == null;
    assertEquals(
        buildResultsStream(result).filter(hasNoAttachments).count(),
        getAvailable(result),
        "All items should have NO 'attachments' element.");
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

  @Test(description = "Search result should include DRM status for Items that have DRM")
  public void drmStatus() throws IOException {
    JsonNode result = doSearch(200, "ItemApiViewTest - DRM and versioning v2");
    assertNotNull(result.get("results").get(0).get("drmStatus"));
  }

  @Test(description = "Search result should include moderation details for Items in moderation")
  public void moderationDetails() throws IOException {
    JsonNode result =
        doSearch(200, "MyResourcesApiTest - Moderating", new NameValuePair("status", "MODERATING"));
    assertNotNull(result.get("results").get(0).get("moderationDetails"));
  }

  @Test(
      description = "Search result should include Scrapbook tags as a Display field for Scrapbook")
  public void scrapbookTags() throws IOException {
    JsonNode result =
        doSearch(200, "scrapbookItemTestTitlemypages", new NameValuePair("status", "PERSONAL"));
    ArrayNode displayField = (ArrayNode) result.get("results").get(0).get("displayFields");
    assertEquals(displayField.get(0).get("html").asText(), "key word");
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
        Optional.ofNullable(result.get("results"))
            .map(results -> results.get(0))
            .map(item -> item.get("attachments"))
            .orElseThrow(
                () -> new IllegalStateException("Failed to access attachments for validation"));

    final Predicate<JsonNode> isYouTubeAttachment =
        attachment ->
            Optional.ofNullable(attachment.get("attachmentType"))
                .map(JsonNode::asText)
                .map(typeString -> typeString.compareTo("custom/youtube") == 0)
                .orElse(false);
    final Predicate<JsonNode> hasExternalId =
        attachment ->
            Optional.ofNullable(attachment.get("links"))
                .map(links -> links.get("externalId"))
                .isPresent();

    final long validAttachments =
        StreamSupport.stream(attachments.spliterator(), false)
            .filter(isYouTubeAttachment)
            .filter(hasExternalId)
            .count();

    assertEquals(validAttachments, 1);
  }

  @Test(description = "Search for item with no thumbnail")
  public void noThumbnailTest() throws IOException {
    String itemName = "SearchApiTest - Basic";

    JsonNode result = doSearch(200, itemName);
    assertEquals(getAvailable(result), 1);
    assertFalse(getThumbnailDetails(result, itemName).isPresent());
  }

  @Test(description = "Search for item with full thumbnail details")
  public void thumbnailTest() throws IOException {
    String itemName = "ItemApiViewTest - All attachments";

    JsonNode result = doSearch(200, itemName);
    assertEquals(getAvailable(result), 1);

    Optional<JsonNode> thumbnailDetails = getThumbnailDetails(result, itemName);
    assertTrue(thumbnailDetails.isPresent());

    JsonNode details = thumbnailDetails.get();
    for (String fieldName : Arrays.asList("attachmentType", "mimeType", "link")) {
      assertNotNull(details.get(fieldName));
    }
  }

  @Test(description = "Search for a hierarchy topic result")
  public void hierarchyTopic() throws IOException {
    JsonNode result = doSearch(200, null, new NameValuePair("hierarchy", NORMAL_HIERARCHY_TOPIC));
    assertEquals(getAvailable(result), 56);
  }

  @Test(description = "Search for a non-existent hierarchy topic result")
  public void hierarchyTopicNotFound() throws IOException {
    doSearch(404, null, new NameValuePair("hierarchy", "non-existent"));
  }

  @Test(description = "Search for a virtual hierarchy topic result")
  public void virtualHierarchyTopic() throws IOException {
    JsonNode result = doSearch(200, null, new NameValuePair("hierarchy", VIRTUAL_HIERARCHY_TOPIC));
    assertEquals(getAvailable(result), 2);
  }

  @Test(
      description =
          "Encoded hierarchy compound UUID should be decoded to get the correct search result")
  public void virtualHierarchyTopicEncodedHierarchyUuid() throws IOException {
    JsonNode result = doSearch(200, null, new NameValuePair("hierarchy", VIRTUAL_HIERARCHY_TOPIC));
    assertEquals(getAvailable(result), 2);
  }

  @Test(
      description =
          "Search with criteria defined in a virtual hierarchy topic with additional query")
  public void virtualHierarchyTopicByQuery() throws IOException {
    JsonNode result =
        doSearch(200, "Book A", new NameValuePair("hierarchy", VIRTUAL_HIERARCHY_TOPIC));
    assertEquals(getAvailable(result), 2);
  }

  @Test(description = "Search with criteria defined in a hierarchy topic with owner")
  public void hierarchyTopicByOwner() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair("hierarchy", NORMAL_HIERARCHY_TOPIC),
            new NameValuePair("owner", "adfcaf58-241b-4eca-9740-6a26d1c3dd58"));

    assertEquals(getAvailable(result), 47);
  }

  @Test(description = "Search with criteria defined in a virtual hierarchy topic with date range")
  public void subVirtualHierarchyTopicByDate() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair(
                "hierarchy",
                "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw=="),
            new NameValuePair("modifiedBefore", "2023-01-01"));
    assertEquals(getAvailable(result), 0);
  }

  @Test(description = "Search hierarchy with duplicated criteria which should be ignored")
  public void hierarchyTopicIgnoredParam() throws IOException {
    JsonNode result =
        doSearch(
            200,
            null,
            new NameValuePair("hierarchy", NORMAL_HIERARCHY_TOPIC),
            new NameValuePair("collections", "non-existing"),
            new NameValuePair("status", "ARCHIVED"));
    assertEquals(getAvailable(result), 56);
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

  private Stream<JsonNode> buildResultsStream(JsonNode result) {
    return Optional.ofNullable(result.get("results"))
        .map(JsonNode::spliterator)
        .map(spliterator -> StreamSupport.stream(spliterator, false))
        .orElseThrow(() -> new IllegalStateException("Missing 'results' element in 'result'"));
  }

  private Optional<JsonNode> getThumbnailDetails(JsonNode result, String itemName) {
    Predicate<JsonNode> resultByUuid =
        (r) ->
            Optional.ofNullable(r.get("name"))
                .filter(JsonNode::isTextual)
                .map(JsonNode::textValue)
                .map(id -> id.compareToIgnoreCase(itemName) == 0)
                .orElse(false);

    return buildResultsStream(result)
        .filter(resultByUuid)
        .findFirst()
        .flatMap(r -> Optional.ofNullable(r.get("thumbnailDetails")));
  }
}
