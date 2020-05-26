package com.tle.webtests.test.webservices.rest;

import static org.junit.Assert.assertEquals;

import com.tle.common.Pair;
import java.util.List;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

public class MimeTypeApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "MimeTypeApiTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testRetrieveMimeTypes() throws Exception {
    final JsonNode initialFilters = getEntity(context.getBaseUrl() + "api/mimetype", getToken());
    assertEquals(153, initialFilters.size());
  }
}
