package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.common.Pair;
import java.net.URI;
import java.util.List;
import org.testng.annotations.Test;

public class SchemaApiTest extends AbstractRestApiTest {
  private static final String OAUTH_CLIENT_ID = "SchemaApiTest";
  private static final String API_TASK_PATH = "api/schema";

  // the simple schema
  private static final String KNOWN_SIMPLE_SCHEMA_UUID = "71a27a31-d6b0-4681-b124-6db410ed420b";

  /**
   * @see com.tle.webtests.test.webservices.rest.AbstractRestApiTest#addOAuthClients(java.util.List)
   */
  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testRestrieveSchemas() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);
    JsonNode result = getEntity(uri.toString(), token);
    assertNotNull(containsInSearchBeanResult(result, KNOWN_SIMPLE_SCHEMA_UUID, "uuid"));

    result = getEntity(uri.toString() + '/' + KNOWN_SIMPLE_SCHEMA_UUID, token);
  }
}
