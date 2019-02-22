package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class Schemas extends BaseJSONCreator {
  public static ObjectNode basicJson(String name) {
    ObjectNode schema = json(name, "/item/name", "/item/description");
    ObjectNode item = schema.with("definition").with("xml").with("item");
    ObjectNode nameNode = item.with("name");
    nameNode.put("_indexed", true);
    nameNode.put("_type", "text");
    ObjectNode descNode = item.with("description");
    descNode.put("_indexed", true);
    descNode.put("_type", "text");
    return schema;
  }

  public static ObjectNode json(String name, String namePath, String descriptionPath) {
    ObjectNode schema = mapper.createObjectNode();
    schema.put("name", name);
    schema.put("namePath", namePath);
    schema.put("descriptionPath", descriptionPath);
    return schema;
  }
}
