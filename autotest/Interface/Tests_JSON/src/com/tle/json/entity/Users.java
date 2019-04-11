package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.hash.Hash;
import com.tle.json.BaseJSONCreator;
import java.util.UUID;

@SuppressWarnings("nls")
public class Users extends BaseJSONCreator {
  public static ObjectNode json(
      String username, String firstName, String lastName, String emailAddress, String password) {
    return json(null, username, firstName, lastName, emailAddress, password);
  }

  public static ObjectNode json(
      String uuid,
      String username,
      String firstName,
      String lastName,
      String emailAddress,
      String password) {
    if (uuid == null) {
      uuid = UUID.randomUUID().toString();
    }
    ObjectNode user = mapper.createObjectNode();
    user.put("id", uuid);
    user.put("username", username);
    user.put("firstName", firstName);
    user.put("lastName", lastName);
    user.put("emailAddress", emailAddress);
    ObjectNode exportNode = mapper.createObjectNode();
    if (password != null) {
      String pwdHash = Hash.hashPassword(password);
      exportNode.put("passwordHash", pwdHash);
      exportNode.put("exportVersion", "1.0");
      user.put("_export", exportNode);
    }
    return user;
  }
}
