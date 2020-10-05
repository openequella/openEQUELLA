package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BaseEntityAPITest extends AbstractRestApiTest {
  private static final String COLLECTION_API_ENDPOINT =
      TEST_CONFIG.getInstitutionUrl() + "api/collection";

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
    final String PERMISSION = "SEARCH_COLLECTION";
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

      JsonNode result = mapper.readTree(method.getResponseBody());
      count += getResultLength(result);
      token = getTokenFromResponse(result);
    }
    // Of 13 Collections in the REST institution, one is not available for
    // the AutoTest account due to permission control.
    assertEquals(count, 12);
  }

  private int getResultLength(JsonNode result) {
    return result.get("results").size();
  }

  private String getTokenFromResponse(JsonNode result) {
    JsonNode token = result.get("resumptionToken");
    if (token != null) {
      return token.asText();
    }
    return null;
  }
}
