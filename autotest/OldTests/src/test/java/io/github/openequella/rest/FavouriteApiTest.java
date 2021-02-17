package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.annotations.Test;

public class FavouriteApiTest extends AbstractRestApiTest {
  private final String FAVOURITE_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/favourite";
  private final String ITEM_KEY = "8a9ea41c-e28d-45de-b0a5-d75bca48d701/1";

  @Test
  public void testAddFavourite() throws IOException {
    final HttpMethod method = new PostMethod(FAVOURITE_API_ENDPOINT);
    final NameValuePair[] queryVals = {
      new NameValuePair("itemID", ITEM_KEY),
      new NameValuePair("tags", "a,b,c"),
      new NameValuePair("latest", "true")
    };
    method.setQueryString(queryVals);
    final int statusCode = makeClientRequest(method);
    assertEquals(HttpStatus.SC_CREATED, statusCode);
  }

  @Test
  public void testRemoveFavourite() throws IOException {
    final HttpMethod method = new DeleteMethod(FAVOURITE_API_ENDPOINT + "/" + ITEM_KEY);
    final int statusCode = makeClientRequest(method);
    assertEquals(HttpStatus.SC_OK, statusCode);
  }
}
