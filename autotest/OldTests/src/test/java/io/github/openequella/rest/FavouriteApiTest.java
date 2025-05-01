package io.github.openequella.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.Test;

public class FavouriteApiTest extends AbstractRestApiTest {
  private final String FAVOURITE_ITEM_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/favourite/item";
  private final String FAVOURITE_SEARCH_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/favourite/search";
  private final String ITEM_KEY = "8a9ea41c-e28d-45de-b0a5-d75bca48d701/1";
  private long bookmarkId = 0L;

  @Test
  public void testAddFavouriteItem() throws IOException {
    final PostMethod method = new PostMethod(FAVOURITE_ITEM_API_ENDPOINT);
    final String[] tags = {"a", "b"};
    ObjectNode body = mapper.createObjectNode();
    body.put("itemID", ITEM_KEY);
    body.put("keywords", mapper.valueToTree(tags));
    body.put("isAlwaysLatest", "true");
    method.setRequestEntity(new StringRequestEntity(body.toString(), "application/json", "UTF-8"));
    assertEquals(HttpStatus.SC_CREATED, makeClientRequest(method));

    JsonNode response = mapper.readTree(method.getResponseBodyAsStream());
    ObjectReader stringArrayReader = mapper.readerFor(new TypeReference<String[]>() {});
    String[] keywords = stringArrayReader.readValue((JsonNode) response.withArray("keywords"));

    assertEquals(ITEM_KEY, response.get("itemID").asText());
    assertTrue(response.get("isAlwaysLatest").asBoolean());
    assertArrayEquals(tags, keywords);
    assertNotNull(response.get("bookmarkID"));
    bookmarkId = response.get("bookmarkID").asLong();
  }

  @Test(dependsOnMethods = "testAddFavouriteItem")
  public void testRemoveFavouriteItem() throws IOException {
    final HttpMethod method = new DeleteMethod(FAVOURITE_ITEM_API_ENDPOINT + "/" + bookmarkId);
    assertEquals(HttpStatus.SC_NO_CONTENT, makeClientRequest(method));
    // Try to delete again and the response code should be 404 as the bookmark is already deleted.
    assertEquals(HttpStatus.SC_NOT_FOUND, makeClientRequest(method));
  }

  @Test
  public void testAddFavouriteSearch() throws IOException {
    String searchName = "testFavouriteSearch";
    String searchPath =
        "/page/search?searchOptions=%7B%22rowsPerPage%22%3A10%2C%22currentPage%22%3A0%2C%22sortOrder%22%3A%22RANK%22%2C%22rawMode%22%3Afalse%2C%22status%22%3A%5B%22LIVE%22%2C%22REVIEW%22%5D%2C%22searchAttachments%22%3Atrue%2C%22query%22%3A%22%22%2C%22collections%22%3A%5B%5D%2C%22lastModifiedDateRange%22%3A%7B%7D%2C%22mimeTypeFilters%22%3A%5B%5D%2C%22dateRangeQuickModeEnabled%22%3Atrue%7D";
    final PostMethod method = new PostMethod(FAVOURITE_SEARCH_API_ENDPOINT);
    ObjectNode body = mapper.createObjectNode();
    body.put("name", searchName);
    body.put("url", searchPath);
    method.setRequestEntity(new StringRequestEntity(body.toString(), "application/json", "UTF-8"));

    assertEquals(HttpStatus.SC_CREATED, makeClientRequest(method));
    JsonNode response = mapper.readTree(method.getResponseBodyAsStream());
    assertEquals(searchName, response.get("name").asText());
    assertEquals(searchPath, response.get("url").asText());
  }
}
