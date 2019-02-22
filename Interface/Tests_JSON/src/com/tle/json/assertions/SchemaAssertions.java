package com.tle.json.assertions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaAssertions {

  public static void assertPaths(JsonNode schema, String namePath, String descriptionPath) {
    assertEquals(schema.get("namePath").asText(), namePath);
    assertEquals(schema.get("descriptionPath").asText(), descriptionPath);
  }

  public static void assertNode(JsonNode node, boolean indexed, boolean field, String type) {
    assertBoolean(node, "_indexed", indexed);
    assertBoolean(node, "_field", field);
    JsonNode typeNode = node.get("_type");
    if (type != null) {
      assertEquals(typeNode.asText(), type);
    } else {
      assertNull(typeNode);
    }
  }

  private static void assertBoolean(JsonNode node, String field, boolean indexed) {
    JsonNode jfield = node.get(field);
    if (indexed) {
      assertEquals(jfield.asBoolean(), true);
    } else {
      assertNull(jfield);
    }
  }
}
