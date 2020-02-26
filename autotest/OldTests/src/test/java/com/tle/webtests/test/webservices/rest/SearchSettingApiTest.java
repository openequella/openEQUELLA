package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  private static final String API_SEARCH_PAGE_PATH = "api/settings/searchpage";
  private static final String SHOW_NON_LIVE = "showNonLive";
  private static final String DISABLE_CLOUD_SEARCHING = "disableCloudSearching";
  private static final String AUTHENTICATE_FEEDS = "authenticateFeeds";
  private static final String DISABLE_GALLERY_VIEWS = "disableGalleryViews";
  private static final String DISABLE_IMAGE = "disableImage";
  private static final String DISABLE_VIDEO = "disableVideo";
  private static final String DISABLE_FILE_COUNT = "disableFileCount";
  private static final String DEFAULT_SORT_ORDER = "defaultSortOrder";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testSeachPageSettings() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    final String uri = PathUtils.urlPath(context.getBaseUrl(), API_SEARCH_PAGE_PATH);

    // Load and check initial settings
    final JsonNode initialSettings = getEntity(uri, token);
    assertFalse(initialSettings.get(SHOW_NON_LIVE).asBoolean());
    assertFalse(initialSettings.get(DISABLE_CLOUD_SEARCHING).asBoolean());
    assertFalse(initialSettings.get(AUTHENTICATE_FEEDS).asBoolean());
    assertFalse(initialSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_IMAGE).asBoolean());
    assertFalse(initialSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_VIDEO).asBoolean());
    assertFalse(initialSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_FILE_COUNT).asBoolean());
    assertNull(initialSettings.get(DEFAULT_SORT_ORDER));

    // Update settings
    final ObjectNode newGallerySettings = mapper.createObjectNode();
    newGallerySettings.put(DISABLE_IMAGE, true);
    newGallerySettings.put(DISABLE_VIDEO, true);
    newGallerySettings.put(DISABLE_FILE_COUNT, true);

    final ObjectNode newSearchPageSettings = mapper.createObjectNode();
    newSearchPageSettings.put(SHOW_NON_LIVE, true);
    newSearchPageSettings.put(DISABLE_CLOUD_SEARCHING, true);
    newSearchPageSettings.put(AUTHENTICATE_FEEDS, true);
    newSearchPageSettings.put(DISABLE_GALLERY_VIEWS, newGallerySettings);
    newSearchPageSettings.put(DEFAULT_SORT_ORDER, "RATING");

    HttpResponse response = putEntity(newSearchPageSettings.toString(), uri, token, true);
    assertEquals(response.getStatusLine().getStatusCode(), 204);

    // Load and check the updated settings
    final JsonNode updatedSettings = getEntity(uri, token);
    assertTrue(updatedSettings.get(SHOW_NON_LIVE).asBoolean());
    assertTrue(updatedSettings.get(DISABLE_CLOUD_SEARCHING).asBoolean());
    assertTrue(updatedSettings.get(AUTHENTICATE_FEEDS).asBoolean());
    assertTrue(updatedSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_IMAGE).asBoolean());
    assertTrue(updatedSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_VIDEO).asBoolean());
    assertTrue(updatedSettings.get(DISABLE_GALLERY_VIEWS).get(DISABLE_FILE_COUNT).asBoolean());
    assertEquals(updatedSettings.get(DEFAULT_SORT_ORDER).asText(), "RATING");
  }
}
