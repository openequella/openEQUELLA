package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

public class BrowseHierarchy2ApiTest extends AbstractRestApiTest {
  private final String BROWSE_HIERARCHY_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/browsehierarchy2";
  private final String JAMES_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff:A James";

  @Test(description = "Get all hierarchies")
  public void browseHierarchy() throws IOException {
    final String CLIENT_API_HIERARCHY_UUID = "43e60e9a-a3ed-497d-b79d-386fed23675c";
    final String PARENT_HIERARCHY_UUID = "6135b550-ce1c-43c2-b34c-0a3cf793759d";

    JsonNode hierarchies = request(null);

    // Should be able to get all top level hierarchies
    assertEquals(hierarchies.size(), 7);
    // Should be able to get all matched Items
    assertEquals(
        getTopic(hierarchies, CLIENT_API_HIERARCHY_UUID).get("matchingItemCount").asInt(), 12);
    // Should be able to get all sub topics.
    assertEquals(getTopic(hierarchies, PARENT_HIERARCHY_UUID).get("subHierarchyTopics").size(), 1);
  }

  @Test(description = "Get a hierarchy topic")
  public void getHierarchy() throws IOException {
    final String uuid = "bb9d5fbd-07e9-4e61-8eb6-e0c06ae39dfc";
    JsonNode results = request(uuid);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), uuid);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 2);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get a virtual hierarchy topic")
  public void getVirtualHierarchy() throws IOException {
    final String ENCODED_JAMES_HIERARCHY_UUID = "886aa61d-f8df-4e82-8984-c487849f80ff%3AA%20James";

    JsonNode results = request(ENCODED_JAMES_HIERARCHY_UUID);
    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), JAMES_HIERARCHY_UUID);
    // Key resource should point to the correct version
    assertEquals(results.get("keyResources").get(0).get("version").asInt(), 2);
  }

  @Test(description = "Get a sub virtual hierarchy")
  public void getSubVirtualHierarchy() throws IOException {
    final String HOBART_HIERARCHY_UUID =
        "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A James";
    final String ENCODED_HOBART_HIERARCHY_UUID =
        "46249813-019d-4d14-b772-2a8ca0120c99%3AHobart%2C886aa61d-f8df-4e82-8984-c487849f80ff%3AA%20James";

    JsonNode results = request(ENCODED_HOBART_HIERARCHY_UUID);

    // Should be able to get the request hierarchy topic
    assertEquals(results.get("summary").get("compoundUuid").asText(), HOBART_HIERARCHY_UUID);
    // Should be able to get all parents info
    assertEquals(results.get("parents").size(), 1);
    // Should be able to get all key resources
    assertEquals(results.get("keyResources").size(), 2);
  }

  @Test(description = "Get hierarchy IDs with key resource")
  public void getHierarchyIdsWithKeyResource() throws IOException {
    final String ITEM_UUID = "cadcd296-a4d7-4024-bb5d-6c7507e6872a";
    final GetMethod method =
        new GetMethod(BROWSE_HIERARCHY_API_ENDPOINT + "/key-resource/" + ITEM_UUID + "/2");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    JsonNode result = mapper.readTree(method.getResponseBody());
    assertEquals(result.get(0).asText(), JAMES_HIERARCHY_UUID);
  }

  private JsonNode request(String compoundUuid) throws IOException {
    String url =
        Optional.ofNullable(compoundUuid)
            .map(uuid -> BROWSE_HIERARCHY_API_ENDPOINT + "/" + uuid)
            .orElse(BROWSE_HIERARCHY_API_ENDPOINT);

    final GetMethod method = new GetMethod(url);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);
    return mapper.readTree(method.getResponseBody());
  }

  // get specific topic from endpoint result
  private JsonNode getTopic(JsonNode result, String topicUuid) {
    Optional<JsonNode> topic =
        Streams.stream(result.elements())
            .filter(
                t ->
                    Optional.ofNullable(t.get("compoundUuid"))
                        .map(JsonNode::asText)
                        .map(uuid -> uuid.equals(topicUuid))
                        .orElse(false))
            .findFirst();
    return topic.orElseThrow();
  }
}
