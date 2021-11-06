package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TokenisationApiTest extends AbstractRestApiTest {
  private final String TOKEN_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/search/token";

  @DataProvider
  private Object[][] textProvider() {
    return new Object[][] {
      {"book", new String[] {"book"}}, // single word
      {"books", new String[] {"book"}}, // stemming
      {"book portion", new String[] {"book", "portion"}}, // multiple words
      {"\"book portion\"", new String[] {"\"book portion\""}}, // single phrase
      {"the book portion", new String[] {"book", "portion"}}, // stop words
      {
        "\"book portion\" and hello world", new String[] {"\"book portion\"", "hello", "world"}
      }, // mixed
    };
  }

  @Test(description = "tokenise texts", dataProvider = "textProvider")
  public void tokenisationTest(String text, String[] expectedTokens) throws IOException {
    HttpMethod method = new GetMethod(TOKEN_API_ENDPOINT);
    NameValuePair query = new NameValuePair("text", text);

    method.setQueryString(new NameValuePair[] {query});

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    ArrayNode result = (ArrayNode) mapper.readTree(method.getResponseBody()).get("tokens");
    String[] tokens =
        StreamSupport.stream(result.spliterator(), false)
            .map(JsonNode::asText)
            .toArray(String[]::new);

    assertEquals(tokens, expectedTokens);
  }
}
