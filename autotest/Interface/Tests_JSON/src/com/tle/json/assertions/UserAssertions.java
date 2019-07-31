package com.tle.json.assertions;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public class UserAssertions {
  public static void assertGroup(JsonNode group, String uuid, String name, String... members) {
    if (uuid != null) {
      assertEquals(group.get("id").asText(), uuid);
    }
    assertEquals(group.get("name").asText(), name);
    Set<String> expectSet = Sets.newHashSet(Arrays.asList(members));
    Set<String> userSet = Sets.newHashSet();
    if (!expectSet.isEmpty()) {
      for (JsonNode user : group.get("users")) {
        userSet.add(user.asText());
      }
      assertEquals(userSet, expectSet);
    }
  }

  public static void assertRole(JsonNode group, String uuid, String name, String expression) {
    if (uuid != null) {
      assertEquals(group.get("id").asText(), uuid);
    }
    assertEquals(group.get("name").asText(), name);
    assertEquals(group.get("expression").asText(), expression);
  }
}
