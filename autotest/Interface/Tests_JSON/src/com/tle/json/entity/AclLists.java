package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class AclLists extends BaseJSONCreator {
  public static ObjectNode userRule(String priv, boolean granted, boolean override, String userId) {
    return rule(priv, granted, override, userWho(userId));
  }

  public static String userWho(String userId) {
    return "U:" + userId;
  }

  public static String everyoneWho() {
    return "*";
  }

  public static ObjectNode rule(String priv, boolean granted, boolean override, String who) {
    ObjectNode rule = mapper.createObjectNode();
    rule.put("granted", granted);
    rule.put("override", override);
    rule.put("privilege", priv);
    rule.put("who", who);
    return rule;
  }
}
