package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class GlobalAcls extends BaseJSONCreator {
  public static ObjectNode addGlobalRule(ObjectNode ruleList, ObjectNode rule) {
    if (ruleList == null) {
      ruleList = mapper.createObjectNode();
    }
    ruleList.withArray("entries").add(rule);
    return ruleList;
  }
}
