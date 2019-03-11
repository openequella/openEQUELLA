package com.tle.resttests.test.user;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.Users;
import com.tle.json.requests.UserRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.UserRequestsBuilder;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UserApiTest extends AbstractRestAssuredTest {
  private UserRequests users;
  private UserRequests usersAsGuest;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    UserRequestsBuilder builder = new UserRequestsBuilder(this);
    users = builder.users();
    usersAsGuest = builder.users(this);
  }

  @Test
  public void crudUser() throws Exception {
    ObjectNode user =
        Users.json("SomeNewUser", "Some", "User", "some@user.com", "superSecretPassword");
    usersAsGuest.createFail(usersAsGuest.accessDeniedRequest(), user);
    ObjectNode newUser = users.create(user);
    assertUser(newUser, user);

    newUser.put("emailAddress", "changed@email.com");
    usersAsGuest.editRequest(usersAsGuest.accessDeniedRequest(), newUser);
    users.editId(newUser);

    String newUserId = users.getId(newUser);
    ObjectNode editedUser = users.get(newUserId);
    assertEquals(editedUser.get("emailAddress").asText(), newUser.get("emailAddress").asText());

    newUser.put("id", UUID.randomUUID().toString());

    RequestSpecification badRequest = users.badRequest();
    RequestSpecification editRequest = users.editRequest(badRequest, newUser);
    users.editResponse(editRequest, newUserId);

    editedUser = users.get(newUserId);
    assertEquals(editedUser.get("id").asText(), user.get("id").asText());

    newUser.remove("id");
    users.editResponse(newUser, newUserId);

    usersAsGuest.delete(usersAsGuest.accessDeniedRequest(), newUserId);
    users.delete(newUserId);

    users.getResponse(users.notFoundRequest(), newUserId);
  }

  @Test
  public void searchUser() throws Exception {
    ObjectNode user =
        Users.json(
            "AUserToSearch",
            "Searchable",
            "User",
            "searchable@user.com",
            "anotherSuperSecretPassword");

    users.create(user);
    if (isEquella()) {
      // TODO: equella user searching
    } else {
      ObjectNode error = users.search("AUserToSearch");

      Assert.assertEquals(error.get("code").intValue(), 501);
      Assert.assertEquals(error.get("error").textValue(), "Not Implemented");
      Assert.assertEquals(
          error.get("error_description").textValue(), "Searching for users is no longer possible");
    }

    ObjectNode searchUser = users.getByUsername("AUserToSearch");
    assertUser(searchUser, user);
  }

  private static void assertUser(JsonNode actualUser, JsonNode expectedUser) {
    assertEquals(actualUser.get("id").asText(), expectedUser.get("id").asText());
    assertEquals(actualUser.get("username").asText(), expectedUser.get("username").asText());
    assertEquals(actualUser.get("firstName").asText(), expectedUser.get("firstName").asText());
    assertEquals(actualUser.get("lastName").asText(), expectedUser.get("lastName").asText());
    assertEquals(
        actualUser.get("emailAddress").asText(), expectedUser.get("emailAddress").asText());
    assertNull(actualUser.get("password"), "Should not return password");
  }

  @Override
  protected String getDefaultUser() {
    // Guest
    return null;
  }
}
