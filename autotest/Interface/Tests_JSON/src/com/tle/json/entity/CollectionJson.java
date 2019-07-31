package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class CollectionJson extends BaseJSONCreator {
  public static ObjectNode json(String name, String schemaUuid, String workflowUuid) {
    ObjectNode collection = mapper.createObjectNode();
    collection.put("name", name);
    collection.with("schema").put("uuid", schemaUuid);
    collection.with("workflow").put("uuid", workflowUuid);
    return collection;
  }

  public static void addStatusRule(ObjectNode security, String status, ObjectNode rule) {
    security.with("statuses").withArray(status).add(rule);
  }
}
