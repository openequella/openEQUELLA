package io.github.openequella.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BaseEntityAPITest extends AbstractRestApiTest {
  private final String COLLECTION_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/collection";
  private final String PERMISSION = "SEARCH_COLLECTION";

  @DataProvider(name = "initialResumptionTokens")
  public static Object[][] tokens() {
    return new Object[][] {
      {"0:5"}, {"0:10"}, {"0:20"},
    };
  }

  @Test(
      dataProvider = "initialResumptionTokens",
      description = "Use Collection as the testing entity.")
  public void getEntities(String initialToken) throws IOException {
    final HttpMethod method = new GetMethod(COLLECTION_API_ENDPOINT);
    int count = 0;
    String token = initialToken;
    // Keep sending requests with resumption tokens until no more tokens are returned.
    while (token != null) {
      final NameValuePair[] queryParams = new NameValuePair[2];
      queryParams[0] = new NameValuePair("privilege", PERMISSION);
      queryParams[1] = new NameValuePair("resumption", token);
      method.setQueryString(queryParams);

      int statusCode = makeClientRequest(method);
      assertEquals(statusCode, HttpStatus.SC_OK);

      JsonNode result = mapper.readTree(method.getResponseBodyAsStream());
      count += getResultLength(result);
      token = getTokenFromResponse(result);
    }
    // Of 13 Collections in the REST institution, one is not available for
    // the AutoTest account due to permission control.
    assertEquals(count, 12);
  }

  @Test
  public void getFullInformation() throws IOException {
    final HttpMethod method = new GetMethod(COLLECTION_API_ENDPOINT);
    final NameValuePair[] queryParams = new NameValuePair[3];
    queryParams[0] = new NameValuePair("privilege", PERMISSION);
    queryParams[1] = new NameValuePair("full", "true");
    queryParams[2] = new NameValuePair("q", "Basic");
    method.setQueryString(queryParams);

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, HttpStatus.SC_OK);

    JsonNode results = getResultList(mapper.readTree(method.getResponseBodyAsStream()));

    // There are two Collections named 'Basic xxx'. Full information of the first one
    // is accessible whereas that of the second is not accessible.
    // The purpose of checking 'security' is to confirm whether full details is returned
    // because 'security' is only available when an entity's full detail is accessible.
    final String BASIC_ITEM = "Basic Items";
    final String BASIC_ITEM_MODERATION = "Basic Items moderation";
    List<String> checkedNode = new ArrayList<>();
    for (final JsonNode node : results) {
      if (node.get("name").asText().equals(BASIC_ITEM)) {
        assertNull(node.get("security"));
        checkedNode.add(BASIC_ITEM);
      } else {
        assertNotNull(node.get("security"));
        checkedNode.add(BASIC_ITEM_MODERATION);
      }
    }
    // Ensure above two assertions have been executed.
    assertEquals(checkedNode.size(), 2);
  }

  private int getResultLength(JsonNode result) {
    return getResultList(result).size();
  }

  private String getTokenFromResponse(JsonNode result) {
    JsonNode token = result.get("resumptionToken");
    if (token != null) {
      return token.asText();
    }
    return null;
  }

  private JsonNode getResultList(JsonNode result) {
    return result.get("results");
  }
}
