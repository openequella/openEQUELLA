package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

public class BrowseHierarchy2ApiTest extends AbstractRestApiTest {
  private final String BROWSE_HIERARCHY_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/browsehierarchy2";
  private final ObjectMapper mapper = new ObjectMapper();

  @Test(description = "Get all hierarchies")
  public void browseHierarchy() throws IOException {
    final GetMethod method = new GetMethod(BROWSE_HIERARCHY_API_ENDPOINT);
    int statusCode = makeClientRequest(method);

    assertEquals(statusCode, 200);

    JsonNode hierarchies = mapper.readTree(method.getResponseBody());

    // Should be able to get all top level hierarchies
    assertEquals(hierarchies.size(), 4);
    // The first top level topic should have 14 Items including key resources.
    assertEquals(hierarchies.get(0).get("matchingItemCount").asInt(), 14);
    // The fourth top level topic should have 1 sub topic.
    assertEquals(hierarchies.get(3).get("subHierarchyTopics").size(), 1);
  }
}
