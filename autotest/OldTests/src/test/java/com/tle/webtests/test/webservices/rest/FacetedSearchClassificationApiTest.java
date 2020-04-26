package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;

import com.tle.common.Pair;
import java.util.List;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FacetedSearchClassificationApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "FacetedSearchApiTestClient";
  private static final String API_PATH = "api/settings/facetedsearch/classification/";
  private long classificationId;
  private ObjectNode validClassification;
  private ObjectNode invalidClassification;

  private static final String NAME = "good name";
  private static final String NEW_NAME = "better name";
  private static final long invalidId = 763311234511L;

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @BeforeClass
  @Override
  public void registerClients() throws Exception {
    super.registerClients();
    JsonNode node =
        mapper.readTree(
            getClass().getClassLoader().getResource("facetedsearchclassification.json"));

    validClassification = (ObjectNode) node.get("valid");
    invalidClassification = (ObjectNode) node.get("invalid");
  }

  @Test
  public void testCreateClassification() throws Exception {
    String uri = context.getBaseUrl() + API_PATH;
    HttpResponse response = postEntity(validClassification.toString(), uri, getToken(), false);
    assertEquals(201, response.getStatusLine().getStatusCode());
    JsonNode result = mapper.readTree(response.getEntity().getContent());
    classificationId = result.get("id").asLong();

    response = postEntity(invalidClassification.toString(), uri, getToken(), false);
    assertEquals(400, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testCreateClassification")
  public void testRetrieveClassification() throws Exception {
    final JsonNode classification = getEntity(validPath(), getToken());
    assertEquals(NAME, classification.get("name").asText());
  }

  @Test(dependsOnMethods = "testRetrieveClassification")
  public void testUpdateClassification() throws Exception {
    validClassification.put("name", NEW_NAME);
    HttpResponse response =
        putEntity(validClassification.toString(), validPath(), getToken(), false);
    assertEquals(200, response.getStatusLine().getStatusCode());
    JsonNode result = mapper.readTree(response.getEntity().getContent());
    assertEquals(NEW_NAME, result.get("name").asText());

    response = putEntity(invalidClassification.toString(), validPath(), getToken(), false);
    assertEquals(400, response.getStatusLine().getStatusCode());

    response = putEntity(validClassification.toString(), invalidPath(), getToken(), false);
    assertEquals(404, response.getStatusLine().getStatusCode());
  }

  @Test(dependsOnMethods = "testUpdateClassification")
  public void testDeleteClassification() throws Exception {
    HttpResponse response = deleteResource(validPath(), getToken());
    assertEquals(200, response.getStatusLine().getStatusCode());

    response = deleteResource(invalidPath(), getToken());
    assertEquals(404, response.getStatusLine().getStatusCode());
  }

  private String validPath() {
    return context.getBaseUrl() + API_PATH + classificationId;
  }

  private String invalidPath() {
    return context.getBaseUrl() + API_PATH + invalidId;
  }
}
