package com.tle.resttests.test.collection;

import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.tle.json.assertions.SchemaAssertions;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Schemas;
import com.tle.json.framework.Waiter;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.resttests.AbstractEntityApiEditTest;
import com.tle.resttests.util.RequestsBuilder;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class SchemaApiTest extends AbstractEntityApiEditTest {
  private static final String ORIGPATH_NAME = "/item/name";
  private static final String ORIGPATH_DESCRIPTION = "/item/description";
  private static final String NEWPATH_NAME = "/item/itembody/name";
  private static final String NEWPATH_DESCRIPTION = "/item/itembody/description";
  private String schemaId;
  private String collectionId;
  private String fullSchemaName;

  @Test
  public void create() throws IOException {
    fullSchemaName = context.getFullName("Test schema");
    ObjectNode schema = Schemas.json(fullSchemaName, ORIGPATH_NAME, ORIGPATH_DESCRIPTION);
    ObjectNode item = schema.with("definition").with("xml").with("item");
    item.with("name").put("_indexed", true);
    item.with("description").put("_indexed", true);
    schema = schemas.create(schema);
    SchemaAssertions.assertPaths(schema, ORIGPATH_NAME, ORIGPATH_DESCRIPTION);
    SchemaAssertions.assertNode(
        schema.get("definition").get("xml").get("item").get("name"), true, false, null);
    SchemaAssertions.assertNode(
        schema.get("definition").get("xml").get("item").get("description"), true, false, null);
    schemaId = schemas.getId(schema);
    collectionId =
        collections.createId(
            CollectionJson.json(context.getFullName("Schema collection"), schemaId, null));
  }

  @Test(dependsOnMethods = "create")
  public void deleteWhileInUse() throws IOException {
    schemas.delete(schemas.badRequest(), schemaId);
  }

  @Test(dependsOnMethods = "create")
  public void edit() throws IOException {
    String originalName = context.getFullName("Original name");
    String originalDesc = context.getFullName("Original desc");
    String newName = context.getFullName("New name");
    String newDesc = context.getFullName("New desc");
    ObjectNode item =
        items.create(
            Items.json(
                collectionId,
                ORIGPATH_NAME,
                originalName,
                ORIGPATH_DESCRIPTION,
                originalDesc,
                NEWPATH_NAME,
                newName,
                NEWPATH_DESCRIPTION,
                newDesc));

    ObjectNode schema = schemas.get(schemaId);
    schema.put("namePath", NEWPATH_NAME);
    schema.put("descriptionPath", NEWPATH_DESCRIPTION);
    schema.with("definition").with("xml").with("item").with("name").put("_indexed", false);
    schemas.editId(schema);
    schema = schemas.get(schemaId);
    SchemaAssertions.assertPaths(schema, NEWPATH_NAME, NEWPATH_DESCRIPTION);
    SchemaAssertions.assertNode(
        schema.get("definition").get("xml").get("item").get("name"), false, false, null);
    waitForNameDesc(items.getId(item), newName, newDesc);
  }

  @Test(dependsOnMethods = "create")
  public void list() {
    JsonNode results = schemas.list();
    JsonNode resultsNode = results.get("results");

    List<String> schemas = Lists.newArrayList();
    for (JsonNode result : resultsNode) {
      schemas.add(result.get("name").asText());
    }

    assertTrue(schemas.contains(fullSchemaName), "Schema not found in " + schemas);
  }

  private ObjectNode waitForNameDesc(final ItemId itemId, final String name, final String desc) {
    Waiter<String> waiter = new Waiter<String>("").withTimeout(60, TimeUnit.SECONDS);
    return waiter.until(
        new Function<String, ObjectNode>() {
          @Override
          public ObjectNode apply(String input) {
            ObjectNode item = items.get(itemId);
            if (name.equals(item.path("name").asText())
                && desc.equals(item.path("description").asText())) {
              return item;
            }
            return null;
          }
        });
  }

  @Override
  protected BaseEntityRequests createRequestsWithBuilder(RequestsBuilder builder) {
    return builder.schemas();
  }

  @Override
  protected ObjectNode createJsonForPrivs(String fullName) {
    return Schemas.basicJson(fullName);
  }

  @Override
  protected ObjectNode createJsonForEdit(String fullName) {
    return Schemas.basicJson(fullName);
  }

  @Override
  protected String getEditPrivilege() {
    return "EDIT_SCHEMA";
  }

  @Override
  protected void assertExtraEdits(ObjectNode edited) {
    // nothing
  }

  @Override
  protected void extraEdits(ObjectNode client) {
    // nothing
  }
}
