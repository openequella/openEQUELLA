package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.common.Pair;
import java.net.URI;
import java.util.List;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

/**
 * Tests both edit-hierarchy and browse-hierarchy API's.
 *
 * @author larry
 */
public class HierarchyApiTest extends AbstractRestApiTest {
  /** Note this test assumes the pre-existence of a root-level hierarchy with exactly this name */
  private static final String OAUTH_CLIENT_ID = "HierarchyApiTestClient";

  private static final String DETECTABLE_DELETABLE_PREFIX =
      OAUTH_CLIENT_ID + "_delete me if you see me";
  private static final String API_EDIT_TASK_PATH = "api/hierarchy";
  private static final String API_BROWSE_TASK_PATH = "api/browsehierarchy";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void hierarchySearchTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_EDIT_TASK_PATH);

    JsonNode result = getEntity(uri.toString(), token);
    assertNotNull(containsInSearchBeanResult(result, OAUTH_CLIENT_ID, "name"));
  }

  @Test
  public void createHierarchiesTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_EDIT_TASK_PATH);

    // create a root level hierarchy
    String jsonString =
        "{ "
            + "\"name\" : \""
            + DETECTABLE_DELETABLE_PREFIX
            + "_a\","
            + "\"shortDescription\" : \" deletable short description\","
            + "\"longDescription\" : \"Meat Loaf: my greatest regret (Demetriou)\", "
            + "\"showResults\" : false,"
            + "\"inheritFreetext\" : false, "
            + "\"freetext\" : \"Meat Loaf\","
            + "\"hideSubtopicsWithNoResults\" : true"
            + " }";
    HttpResponse postResponse = postEntity(jsonString, uri.toString(), token, false);
    int postStatus = postResponse.getStatusLine().getStatusCode();
    assertTrue(postStatus == 200 || postStatus == 201);

    JsonNode jsonResponse = mapper.readTree(postResponse.getEntity().getContent());

    String uuidOfParent = jsonResponse.get("uuid").asText();
    // create a child of the first
    jsonString =
        "{ "
            + "\"name\" : \""
            + DETECTABLE_DELETABLE_PREFIX
            + "_b\","
            + "\"shortDescription\" : \"b is the child of a (at first)\","
            + "\"longDescription\" : \"Meat Loaf: my greatest regret (Jim Sharman)\", "
            + "\"showResults\" : false,"
            + "\"inheritFreetext\" : false, "
            + "\"freetext\" : \"Rocky Horror\","
            + "\"hideSubtopicsWithNoResults\" : true,"
            + "\"parent\" : { \"uuid\" :  \""
            + uuidOfParent
            + "\" },\"virtualisationPath\" : \"/item/itembody/abstract\",\"virtualisationId\" :"
            + " \"contributedValuesVirtualiser\",\"attributes\" : [ { \"key\" :"
            + " \"manualListVirtualiser\",\"value\" : \"<list> <string>no one knows</string>"
            + " <string>no one cares</string> </list>\" } ]  }";
    postResponse = postEntity(jsonString, uri.toString(), token, false);

    postStatus = postResponse.getStatusLine().getStatusCode();
    assertTrue(postStatus == 200 || postStatus == 201, "actual postResponse Code: " + postStatus);
  }

  @Test(dependsOnMethods = {"createHierarchiesTest"})
  public void editHierarchiesTest() throws Exception {}

  /**
   * We have primed our persistent root-level hierarchy with the query "hierarchyApiTest", and there
   * are 12 persistent items in the 'rest' institution matching that search. Similarly, there are 3
   * items matching a search on "Meat Loaf", and 2 on "Rocky Horror", noting that temporary
   * hierarchies have been created with those searches in the previous 'dependsOn...' test.
   *
   * @throws Exception
   */
  @Test(dependsOnMethods = {"createHierarchiesTest"})
  public void browseHierarchiesTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_BROWSE_TASK_PATH);

    JsonNode result = getEntity(uri.toString(), token);
    JsonNode hierarchyNode = containsInSearchBeanResult(result, OAUTH_CLIENT_ID, "name");
    URI browseUri =
        new URI(
            context.getBaseUrl() + API_BROWSE_TASK_PATH + '/' + hierarchyNode.get("uuid").asText());

    result = getEntity(browseUri.toString(), token, "start", 4, "length", 7);
    JsonNode searchBeanNode = result.get("searchResults");

    assertEquals(4, searchBeanNode.get("start").asInt());
    assertEquals(7, searchBeanNode.get("length").asInt());
    assertEquals(12, searchBeanNode.get("available").asInt());

    result = getEntity(uri.toString(), token);
    hierarchyNode = containsInSearchBeanResult(result, DETECTABLE_DELETABLE_PREFIX + "_a", "name");
    JsonNode hierarchyLink =
        hierarchyNode.get("links") != null ? hierarchyNode.get("links").get("self") : null;
    assertNotNull(hierarchyLink);
    browseUri = new URI(hierarchyLink.asText());
    // retrieve the target (parent) hierarchy individually, in order to see its child topics
    result = getEntity(browseUri.toString(), token);
    JsonNode hierarchyLinkToChildren = result.get("subTopics");
    assertTrue(
        hierarchyLinkToChildren != null && hierarchyLinkToChildren.size() >= 1, "no childTopics");
    hierarchyLinkToChildren = hierarchyLinkToChildren.get(0);
    String nameOfChild = hierarchyLinkToChildren.get("name").asText();
    assertEquals(nameOfChild, DETECTABLE_DELETABLE_PREFIX + "_b");
    URI childUri = new URI(hierarchyLinkToChildren.get("links").get("self").asText());

    result = getEntity(childUri.toString(), token);
    searchBeanNode =
        result.get("searchResults") != null ? result.get("searchResults").get("results") : null;
    assertNotNull(searchBeanNode);
    int numVirtualisedItems = searchBeanNode.size();
    // We're actually expecting 2 items to contain 'Rocky Horror'
    assertTrue(
        numVirtualisedItems == 2, "only found " + numVirtualisedItems + " virtualised items");
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_EDIT_TASK_PATH);

    JsonNode result = getEntity(uri.toString(), token);
    if (result.get("length").asInt() > 0) {
      JsonNode allResults = result.get("results");
      for (int i = 0; i < allResults.size(); ++i) {
        JsonNode aResult = allResults.get(i);
        String name = aResult.get("name").asText();
        if (name.startsWith(DETECTABLE_DELETABLE_PREFIX)) {
          String uuid = aResult.get("uuid").asText();
          deleteResource(uri.toString() + '/' + uuid, token);
        }
      }
    }
    super.cleanupAfterClass();
  }
}
