package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.tle.common.Pair;
import com.tle.common.PathUtils;
import java.util.List;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class SearchSettingApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "SearchSettingApiTestClient";
  private static final String API_SEARCH_SETTINGS_PATH = "api/settings/search";
  private static final String API_CLOUD_SETTINGS_PATH = "api/settings/search/cloud";
  private static final String API_SEARCH_FILTER_PATH = "api/settings/search/filter";

  private static final String DEFAULT_SORT_ORDER = "defaultSearchSort";
  private static final String SHOW_NON_LIVE = "searchingShowNonLiveCheckbox";
  private static final String AUTHENTICATE_FEEDS = "authenticateFeedsByDefault";

  private static final String DISABLE_IMAGE = "searchingDisableGallery";
  private static final String DISABLE_VIDEO = "searchingDisableVideos";
  private static final String DISABLE_FILE_COUNT = "fileCountDisabled";

  private static final String TITLE_BOOST = "titleBoost";
  private static final String DESCRIPTION_BOOST = "descriptionBoost";
  private static final String ATTACHMENT_BOOST = "attachmentBoost";

  private static final String URL_LEVEL = "urlLevel";

  private static final String DISABLE_CLOUD = "disabled";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testSearchSettings() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_SEARCH_SETTINGS_PATH);

    // Load and check initial settings
    final JsonNode initialSearchSettings = getEntity(uri, token);

    assertNull(initialSearchSettings.get(DEFAULT_SORT_ORDER));
    assertFalse(initialSearchSettings.get(SHOW_NON_LIVE).asBoolean());
    assertFalse(initialSearchSettings.get(AUTHENTICATE_FEEDS).asBoolean());

    assertFalse(initialSearchSettings.get(DISABLE_IMAGE).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_VIDEO).asBoolean());
    assertFalse(initialSearchSettings.get(DISABLE_FILE_COUNT).asBoolean());

    assertEquals(initialSearchSettings.get(TITLE_BOOST).asInt(), 5);
    assertEquals(initialSearchSettings.get(DESCRIPTION_BOOST).asInt(), 3);
    assertEquals(initialSearchSettings.get(ATTACHMENT_BOOST).asInt(), 2);

    assertEquals(initialSearchSettings.get(URL_LEVEL).asInt(), 0);

    // Update settings
    final ObjectNode newSearchSettings = mapper.createObjectNode();

    newSearchSettings.put(DEFAULT_SORT_ORDER, "RATING");
    newSearchSettings.put(SHOW_NON_LIVE, true);
    newSearchSettings.put(AUTHENTICATE_FEEDS, true);

    newSearchSettings.put(DISABLE_IMAGE, true);
    newSearchSettings.put(DISABLE_VIDEO, true);
    newSearchSettings.put(DISABLE_FILE_COUNT, true);

    newSearchSettings.put(TITLE_BOOST, 4);
    newSearchSettings.put(DESCRIPTION_BOOST, 4);
    newSearchSettings.put(ATTACHMENT_BOOST, 4);

    newSearchSettings.put(URL_LEVEL, 1);

    HttpResponse response = putEntity(newSearchSettings.toString(), uri, token, true);
    assertEquals(response.getStatusLine().getStatusCode(), 204);

    // Load and check the updated settings
    final JsonNode updatedSearchSettings = getEntity(uri, token);

    assertEquals(updatedSearchSettings.get(DEFAULT_SORT_ORDER).asText(), "RATING");
    assertTrue(updatedSearchSettings.get(SHOW_NON_LIVE).asBoolean());
    assertTrue(updatedSearchSettings.get(AUTHENTICATE_FEEDS).asBoolean());

    assertTrue(updatedSearchSettings.get(DISABLE_IMAGE).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_VIDEO).asBoolean());
    assertTrue(updatedSearchSettings.get(DISABLE_FILE_COUNT).asBoolean());

    assertEquals(updatedSearchSettings.get(TITLE_BOOST).asInt(), 4);
    assertEquals(updatedSearchSettings.get(DESCRIPTION_BOOST).asInt(), 4);
    assertEquals(updatedSearchSettings.get(ATTACHMENT_BOOST).asInt(), 4);

    assertEquals(updatedSearchSettings.get(URL_LEVEL).asInt(), 1);
  }

  @Test
  public void testCloudSettings() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_CLOUD_SETTINGS_PATH);

    final JsonNode initialCloudSettings = getEntity(uri, token);
    assertFalse(initialCloudSettings.get(DISABLE_CLOUD).asBoolean());

    final ObjectNode newCloudSettings = mapper.createObjectNode();
    newCloudSettings.put(DISABLE_CLOUD, true);
    HttpResponse response = putEntity(newCloudSettings.toString(), uri, token, true);
    assertEquals(response.getStatusLine().getStatusCode(), 204);

    final JsonNode updatedCloudSettings = getEntity(uri, token);
    assertTrue(updatedCloudSettings.get(DISABLE_CLOUD).asBoolean());
  }

  @Test
  public void testSearchFilter() throws Exception {
    final String NAME = "name";
    final String MIMI_TYPES = "mimeTypes";
    final String IMAGE_FILTER = "image filter";
    final String JPEG = "image/jpeg";
    final String PNG = "image/png";
    final String PDF_FILTER = "PDF filter";
    final String PDF = "application/pdf";
    final String ID = "id";

    String token = requestToken(OAUTH_CLIENT_ID);
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_SEARCH_FILTER_PATH);

    // Load initial search filters
    final JsonNode initialFilters = getEntity(uri, token);
    assertEquals(initialFilters.size(), 0);

    // Create a search filter
    HttpResponse response =
        postEntity(null, uri, token, true, NAME, IMAGE_FILTER, MIMI_TYPES, JPEG, MIMI_TYPES, PNG);
    assertEquals(response.getStatusLine().getStatusCode(), 201);

    // Load filters again
    final JsonNode filters = getEntity(uri, token);
    assertEquals(filters.size(), 1);
    String filterId = filters.get(0).get(ID).asText();
    assertNotNull(filterId);

    // Update filter
    String newfilterUri = uri + "/" + filterId;
    response = putEntity(null, newfilterUri, token, true, NAME, PDF_FILTER, MIMI_TYPES, PDF);
    assertEquals(response.getStatusLine().getStatusCode(), 204);

    // Load this filter again
    final JsonNode filter = getEntity(newfilterUri, token);
    assertEquals(filter.get(ID).asText(), filterId);
    assertEquals(filter.get(NAME).asText(), PDF_FILTER);
    assertEquals(filter.get(MIMI_TYPES).get(0).asText(), PDF);

    // Delete filter
    response = deleteResource(newfilterUri, token);
    assertEquals(response.getStatusLine().getStatusCode(), 200);
  }
}
