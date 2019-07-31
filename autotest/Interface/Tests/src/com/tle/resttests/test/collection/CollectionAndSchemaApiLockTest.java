package com.tle.resttests.test.collection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.resttests.AbstractEntityApiLockTest;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class CollectionAndSchemaApiLockTest extends AbstractEntityApiLockTest {
  private String token;
  private String schemaUuid;
  private String schemaUri;

  private String collectionUuid;
  private String collectionUri;

  private void createEntities() throws Exception {
    token = requestToken(OAUTH_CLIENT_ID);
    ObjectNode schema = mapper.createObjectNode();
    schema.put("name", "A schema");
    schema.put("namePath", "/item/name");
    schema.put("descriptionPath", "/item/description");
    schema.with("definition").with("xml").with("item").with("name").put("_indexed", true);

    HttpResponse response =
        postEntity(schema.toString(), context.getBaseUrl() + "api/schema", token, true);
    assertResponse(response, 201, "Expected schema to be created");
    schemaUri = response.getFirstHeader("Location").getValue();
    ObjectNode newSchema = (ObjectNode) getEntity(schemaUri, token);
    schemaUuid = newSchema.get("uuid").asText();

    ObjectNode collection = mapper.createObjectNode();
    collection.put("name", "A Collection");
    collection.with("schema").put("uuid", schemaUuid);

    response =
        postEntity(collection.toString(), context.getBaseUrl() + "api/collection", token, true);
    assertResponse(response, 201, "Expected collection to be created");
    collectionUri = response.getFirstHeader("Location").getValue();
    ObjectNode newCollection = (ObjectNode) getEntity(collectionUri, token);
    collectionUuid = newCollection.get("uuid").asText();
  }

  @Test
  public void edit() throws Exception {
    createEntities();
    testLocks(token, schemaUri);
    testLocks(token, collectionUri);
  }

  @AfterMethod(alwaysRun = true)
  public void cleanupSchema() throws IOException {
    if (collectionUuid != null) {
      deleteCollection(collectionUuid, token);
    }
    if (schemaUuid != null) {
      deleteSchema(schemaUuid, token);
    }
  }
}
