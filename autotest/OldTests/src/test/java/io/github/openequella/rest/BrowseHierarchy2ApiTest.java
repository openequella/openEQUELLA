package io.github.openequella.rest;

import static io.github.openequella.rest.JsonNodeHelper.getNode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class BrowseHierarchy2ApiTest extends AbstractRestApiTest {

  private static final Logger log = LoggerFactory.getLogger(BrowseHierarchy2ApiTest.class);
  private final String BROWSE_HIERARCHY_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/browsehierarchy2";
  private final String CLIENT_API_HIERARCHY_UUID = "43e60e9a-a3ed-497d-b79d-386fed23675c";
  // Topic name: A James
  private final String JAMES_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==";
  // Topic name: B Bob
  private final String BOB_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:QiBCb2I=";
  // Topic name: C Candy
  private final String CANDY_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:QyBDYW5keQ==";
  // Topic name: D, David
  private final String DAVID_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:RCwgRGF2aWQ=";
  // Topic name: F, Frank
  private final String FRANK_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:RiBGcmFuaw==";
  // Topic name: G Garry
  private final String GARRY_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:RyBHYXJyeQ==";
  // Topic name: Hobart
  private final String HOBART_HIERARCHY_UUID =
      "46249813-019d-4d14-b772-2a8ca0120c99:SG9iYXJ0,886aa61d-f8df-4e82-8984-c487849f80ff:QSBKYW1lcw==";
  private final String PARENT_HIERARCHY_UUID = "6135b550-ce1c-43c2-b34c-0a3cf793759d";
  private final String INVALID_HIERARCHY_UUID = "invalidUuid:123,456:!@";

  private final String INVALID_UUID_MESSAGE =
      "Failed to parse the compound UUID "
          + INVALID_HIERARCHY_UUID
          + ": Illegal base64 character 21";

  private final String CAL_BOOK_COLLECTION = "4c147089-cddb-e67c-b5ab-189614eb1463";
  private final String BASIC_ITEMS_COLLECTION = "b28f1ffe-2008-4f5e-d559-83c8acd79316";
  private final String SAVE_SCRIPT_COLLECTION = "c7194cd0-f586-49b6-9fcc-4b1c5237efd9";

  private final int ROOT_HIERARCHY_COUNT = 9;

  private final List<String> BOOK_COLLECTIONS =
      List.of(BASIC_ITEMS_COLLECTION, CAL_BOOK_COLLECTION);

  @Test(description = "Get all root hierarchies")
  public void browseRootHierarchies() throws IOException {
    JsonNode hierarchies = getRootHierarchies(null, 200);

    // Should be able to get all top level hierarchies
    assertEquals(hierarchies.size(), ROOT_HIERARCHY_COUNT);
    // Should be able to get all matched Items
    assertEquals(
        getTopic(hierarchies, CLIENT_API_HIERARCHY_UUID).get("matchingItemCount").asInt(), 12);
    // Should be able to get flag indicates if it has sub topics.
    assertTrue(getTopic(hierarchies, PARENT_HIERARCHY_UUID).get("hasSubTopic").asBoolean());
  }

  @Test(description = "Get root hierarchies with one collection filter")
  public void browseRootHierarchiesWithOneCollectionFilter() throws IOException {
    JsonNode hierarchies = getRootHierarchies(List.of(BASIC_ITEMS_COLLECTION), 200);

    // Should filter out virtual hierarchies not related to the collection.
    assertEquals(hierarchies.size(), ROOT_HIERARCHY_COUNT - 3);

    // Because these virtual topics only have one type of collection resources,
    // so filtering out resources by collection will make them remove from the result.
    assertFalse(hasHierarchy(JAMES_HIERARCHY_UUID, hierarchies));
    assertFalse(hasHierarchy(BOB_HIERARCHY_UUID, hierarchies));
    assertFalse(hasHierarchy(CANDY_HIERARCHY_UUID, hierarchies));

    assertTrue(hasHierarchy(DAVID_HIERARCHY_UUID, hierarchies));
    assertTrue(hasHierarchy(FRANK_HIERARCHY_UUID, hierarchies));

    // This virtual hierarchy has 2 types of collection resources, should still be there.
    assertTrue(hasHierarchy(GARRY_HIERARCHY_UUID, hierarchies));
    // The matching item count should be correct after filtering.
    assertEquals(getMatingItemCount(hierarchies, GARRY_HIERARCHY_UUID), 1);
  }

  @Test(description = "Get root hierarchies with collections filter")
  public void browseRootHierarchiesWithCollectionsFilter() throws IOException {
    JsonNode hierarchies = getRootHierarchies(BOOK_COLLECTIONS, 200);

    // Since the collections cover all virtual hierarchies, should still get all root hierarchies.
    assertEquals(hierarchies.size(), ROOT_HIERARCHY_COUNT);
    // The matching item count should be correct after filtering.
    assertEquals(getMatingItemCount(hierarchies, GARRY_HIERARCHY_UUID), 2);
  }

  @Test(description = "Get root hierarchies with low privilege user")
  public void browseRootHierarchiesWithLowPrivilegeUser() throws IOException {
    loginAsLowPrivilegeUser();
    JsonNode hierarchies = browseHierarchy(null, null, 200);
    // Should get an empty result.
    assertEquals(hierarchies.size(), 0);
    login();
  }

  @Test(description = "Get sub hierarchies")
  public void browseSubHierarchies() throws IOException {
    JsonNode hierarchies = browseHierarchy(PARENT_HIERARCHY_UUID, null, 200);
    assertEquals(hierarchies.size(), 3);
  }

  @Test(description = "Get sub hierarchies with collections filter")
  public void browseSubHierarchiesWithCollectionsFilter() throws IOException {
    JsonNode hierarchies = browseHierarchy(JAMES_HIERARCHY_UUID, BOOK_COLLECTIONS, 200);
    // Should return all sub hierarchies related to the collections.
    assertEquals(hierarchies.size(), 2);

    // Since none of the sub virtual hierarchies is related to the collection, should get an empty
    // result.
    JsonNode hierarchiesNone =
        browseHierarchy(JAMES_HIERARCHY_UUID, List.of(SAVE_SCRIPT_COLLECTION), 200);
    assertEquals(hierarchiesNone.size(), 0);
  }

  @Test(description = "Get sub hierarchies with low privilege user")
  public void browseSubHierarchiesWithLowPrivilegeUser() throws IOException {
    loginAsLowPrivilegeUser();
    JsonNode result = browseHierarchy(CLIENT_API_HIERARCHY_UUID, null, 403);
    assertEquals(
        getErrorMessage(result), "Permission denied to access topic " + CLIENT_API_HIERARCHY_UUID);
    login();
  }

  @Test(description = "Get sub hierarchies for hierarchy which has no sub hierarchies")
  public void browseEmptySubHierarchy() throws IOException {
    JsonNode hierarchies = browseHierarchy(CLIENT_API_HIERARCHY_UUID, null, 200);
    assertEquals(hierarchies.size(), 0);
  }

  @Test(description = "Get hierarchy topic with invalid compound UUID")
  public void browseSubHierarchiesWithInvalidUuid() throws IOException {
    JsonNode result = browseHierarchy(INVALID_HIERARCHY_UUID, null, 500);
    assertEquals(getErrorMessage(result), INVALID_UUID_MESSAGE);
  }

  @Test(description = "Get a hierarchy topic details")
  public void getHierarchyDetails() throws IOException {
    final String uuid = "bb9d5fbd-07e9-4e61-8eb6-e0c06ae39dfc";
    JsonNode results = browseHierarchyDetails(uuid, null, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), uuid);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 2);
    // Should be able to get all children info
    assertEquals(results.get("children").size(), 1);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get a hierarchy topic details with collection filter")
  public void getHierarchyDetailsWithCollectionsFilter() throws IOException {
    JsonNode results = browseHierarchyDetails(BOB_HIERARCHY_UUID, null, 200);
    // Should return all resources.
    assertEquals(getMatingItemCountFromSummary(results), 1);

    JsonNode filteredResults =
        browseHierarchyDetails(BOB_HIERARCHY_UUID, List.of(BASIC_ITEMS_COLLECTION), 200);
    // Should filter out resources not related to the collection.
    assertEquals(getMatingItemCountFromSummary(filteredResults), 0);
  }

  @Test(description = "Get a non-existent hierarchy topic")
  public void getNonExistentHierarchyDetails() throws IOException {
    final String uuid = "d0d58dca-6715-4496-b8c2-4aa970bb317c";
    browseHierarchyDetails(uuid, null, 404);
  }

  @Test(description = "Get hierarchy topic with invalid compound UUID")
  public void getHierarchyDetailsWithInvalidCompoundUuid() throws IOException {
    JsonNode result = browseHierarchyDetails(INVALID_HIERARCHY_UUID, null, 500);
    assertEquals(getErrorMessage(result), INVALID_UUID_MESSAGE);
  }

  @Test(description = "Get a virtual hierarchy topic")
  public void getVirtualHierarchyDetails() throws IOException {
    JsonNode results = browseHierarchyDetails(JAMES_HIERARCHY_UUID, null, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(getCompoundUuid(results), JAMES_HIERARCHY_UUID);
  }

  @Test(description = "Get hierarchy key resource item version")
  public void getItemVersion() throws IOException {
    final String BOOK_A_V2_UUID = "cadcd296-a4d7-4024-bb5d-6c7507e6872a";
    final String BOOK_B_UUID = "e35390cf-7c45-4f71-bb94-e6ccc1f09394";

    JsonNode keyResources =
        browseHierarchyDetails(JAMES_HIERARCHY_UUID, null, 200).get("keyResources");

    // Key resource should point to the correct version
    assertFalse(getKeyResource(keyResources, BOOK_A_V2_UUID, 2).get("isLatest").asBoolean());
    assertEquals(
        getKeyResource(keyResources, BOOK_A_V2_UUID, 2).get("item").get("version").asInt(), 2);
    // BOOK_B has 2 versions and as an "Always latest key resource" it should point to the latest
    // version 2.
    assertTrue(getKeyResource(keyResources, BOOK_B_UUID, 2).get("isLatest").asBoolean());
    assertEquals(
        getKeyResource(keyResources, BOOK_B_UUID, 2).get("item").get("version").asInt(), 2);
  }

  @Test(description = "Get a virtual hierarchy topic which name contains comma")
  public void getVirtualHierarchyDetailsWithComma() throws IOException {
    JsonNode results = browseHierarchyDetails(DAVID_HIERARCHY_UUID, null, 200);
    // Should be able to get the request hierarchy topic
    assertEquals(getCompoundUuid(results), DAVID_HIERARCHY_UUID);
  }

  @Test(description = "Get a sub virtual hierarchy")
  public void getSubVirtualDetailsHierarchy() throws IOException {
    JsonNode results = browseHierarchyDetails(HOBART_HIERARCHY_UUID, null, 200);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), HOBART_HIERARCHY_UUID);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 1);
    // Should be able to get all children info
    assertEquals(results.get("children").size(), 1);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get hierarchy IDs with key resource")
  public void getHierarchyIdsWithKeyResource() throws IOException {
    final String ITEM_UUID = "7e633e1d-e343-4e51-babc-403265c7b7c4";
    final GetMethod method =
        new GetMethod(BROWSE_HIERARCHY_API_ENDPOINT + "/key-resource/" + ITEM_UUID + "/1");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    JsonNode result = mapper.readTree(method.getResponseBodyAsStream());
    assertEquals(result.get(0).asText(), "43e60e9a-a3ed-497d-b79d-386fed23675c");
  }

  private JsonNode request(
      String urlPrefix, String compoundUuid, List<String> collectionUuids, int httpCode)
      throws IOException {
    String baseUrl =
        Optional.ofNullable(compoundUuid)
            .map(uuid -> BROWSE_HIERARCHY_API_ENDPOINT + urlPrefix + "/" + uuid)
            .orElse(BROWSE_HIERARCHY_API_ENDPOINT);

    String urlWithQueryMaybe =
        Optional.ofNullable(collectionUuids)
            .filter(list -> !list.isEmpty())
            .map(
                list ->
                    baseUrl
                        + "?"
                        + list.stream()
                            .map(c -> "collections=" + c)
                            .collect(Collectors.joining("&")))
            .orElse(baseUrl);

    final GetMethod method = new GetMethod(urlWithQueryMaybe);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, httpCode);
    return mapper.readTree(method.getResponseBodyAsStream());
  }

  private JsonNode getRootHierarchies(List<String> collectionUuids, int httpCode)
      throws IOException {
    return request("", null, collectionUuids, httpCode);
  }

  private JsonNode browseHierarchy(String compoundUuid, List<String> collectionUuids, int httpCode)
      throws IOException {
    return request("", compoundUuid, collectionUuids, httpCode);
  }

  private JsonNode browseHierarchyDetails(
      String compoundUuid, List<String> collectionUuids, int httpCode) throws IOException {
    return request("/details", compoundUuid, collectionUuids, httpCode);
  }

  // get specific topic from endpoint result
  private JsonNode getTopic(JsonNode result, String topicUuid) {
    return getNode((ArrayNode) result, "compoundUuid", topicUuid);
  }

  private JsonNode getKeyResource(JsonNode result, String itemUuid, int version) {
    return getNode(
        (ArrayNode) result,
        n ->
            n.get("item").findParents("uuid").stream()
                .filter(item -> item.get("version").asInt() == version)
                .findFirst()
                .map(item -> item.get("uuid"))
                .orElse(null),
        itemUuid);
  }

  private String getCompoundUuid(JsonNode result) {
    return result.get("summary").get("compoundUuid").asText();
  }

  private String getErrorMessage(JsonNode result) {
    return result.get("errors").get(0).get("message").asText();
  }

  private int getMatingItemCount(JsonNode result, String uuid) {
    JsonNode hierarchy = getNode((ArrayNode) result, n -> n.get("compoundUuid"), uuid);
    return hierarchy.get("matchingItemCount").asInt();
  }

  private int getMatingItemCountFromSummary(JsonNode result) {
    return result.get("summary").get("matchingItemCount").asInt();
  }

  private Boolean hasHierarchy(String uuid, JsonNode result) {
    return StreamSupport.stream(result.spliterator(), false)
        .anyMatch(h -> h.get("compoundUuid").asText().equals(uuid));
  }
}
