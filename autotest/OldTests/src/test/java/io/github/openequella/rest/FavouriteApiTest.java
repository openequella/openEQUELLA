package io.github.openequella.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class FavouriteApiTest extends AbstractRestApiTest {
  private final String FAVOURITE_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/favourite";
  private final String ITEM_KEY = "8a9ea41c-e28d-45de-b0a5-d75bca48d701/1";

  @Test
  public void testAddFavourite() throws IOException {
    final PostMethod method = new PostMethod(FAVOURITE_API_ENDPOINT);
    final String[] tags = {"a", "b"};
    ObjectNode body = mapper.createObjectNode();
    body.put("itemID", ITEM_KEY);
    body.put("keywords", mapper.valueToTree(tags));
    body.put("isAlwaysLatest", "true");
    method.setRequestEntity(new StringRequestEntity(body.toString(), "application/json", "UTF-8"));
    final int statusCode = makeClientRequest(method);
    assertEquals(HttpStatus.SC_CREATED, statusCode);

    JsonNode response = mapper.readTree(method.getResponseBody());
    String[] keywords = mapper.readValue(response.get("keywords"), String[].class);
    assertEquals(ITEM_KEY, response.get("itemID").asText());
    assertTrue(response.get("isAlwaysLatest").asBoolean());
    assertArrayEquals(tags, keywords);
  }

  @Test
  public void testRemoveFavourite() throws IOException {
    final HttpMethod method = new DeleteMethod(FAVOURITE_API_ENDPOINT + "/" + ITEM_KEY);
    final int statusCode = makeClientRequest(method);
    assertEquals(HttpStatus.SC_NO_CONTENT, statusCode);
  }
}
