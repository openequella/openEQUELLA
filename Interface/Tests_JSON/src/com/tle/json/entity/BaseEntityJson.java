package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class BaseEntityJson extends BaseJSONCreator {
  public static void addAllRule(ObjectNode security, ObjectNode rule) {
    security.withArray("rules").add(rule);
  }

  public static void addRule(ObjectNode entity, ObjectNode rule) {
    entity.with("security").withArray("rules").add(rule);
  }
}
