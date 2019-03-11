package com.tle.resttests.test.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.Groups;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.entity.Roles;
import com.tle.json.entity.Schemas;
import com.tle.json.entity.Users;
import com.tle.json.requests.AclRequests;
import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.GroupRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.RoleRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.SearchRequests;
import com.tle.json.requests.UserRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.UserRequestsBuilder;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "eps")
public class DynamicAclTest extends AbstractEntityCreatorTest {
  private CollectionRequests collections;
  private SchemaRequests schemas;
  private ItemRequests items;
  private ItemRequests itemsDY;
  private SearchRequests searchAT;
  private SearchRequests searchDY;
  private UserRequests users;
  private GroupRequests groups;
  private RoleRequests roles;
  private AclRequests acls;

  private String USER;
  private String GROUP;
  private String ROLE;

  private static final String USER_REVOKE_ITEM = "DynamicAclTest - user item";
  private static final String GROUP_REVOKE_ITEM = "DynamicAclTest - group item";
  private static final String ROLE_GRANT_ITEM = "DynamicAclTest - role item";

  private String collectionUuid;
  private String schemaId;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    RequestsBuilder builder = builder().user(RestTestConstants.USERID_AUTOTEST);
    collections = builder.collections();
    schemas = builder.schemas();
    items = builder.items();
    searchAT = builder.searches();
    acls = builder.acls();

    UserRequestsBuilder builder2 = new UserRequestsBuilder(this);
    users = builder2.users();
    groups = builder2.groups();
    roles = builder2.roles();

    USER = users.getId(users.create(Users.json("dynacl", "dynamic", "acl", "", "equella")));
    GROUP = groups.getId(groups.create(Groups.json(null, context.getFullName("group"), USER)));
    ROLE = roles.getId(roles.create(Roles.json(null, context.getFullName("role"), "U:" + USER)));

    searchDY = builder.user(USER).searches();
    itemsDY = builder.user(USER).items();
  }

  @Test
  public void createCollection() {
    ObjectNode schema =
        Schemas.json(context.getFullName("schema"), "/item/name", "/item/description");
    ObjectNode item = schema.with("definition").with("xml").with("item");
    item.with("name").put("_indexed", true);
    item.with("description").put("_indexed", true);
    item.with("userToRevoke");
    item.with("userToRevoke2");
    item.with("groupToRevoke");
    item.with("roleToGrant");
    schemaId = schemas.getId(schemas.create(schema));
    ObjectNode collection = CollectionJson.json(context.getFullName("collection"), schemaId, null);
    ArrayNode dynamicRules = collection.with("security").putArray("dynamicRules");
    dynamicRules.add(dynamicRule("U", "/item/userToRevoke", "DISCOVER_ITEM", false));
    dynamicRules.add(dynamicRule("G", "/item/groupToRevoke", "EDIT_ITEM", false));
    dynamicRules.add(dynamicRule("R", "/item/roleToGrant", "ARCHIVE_ITEM", true));
    collectionUuid = collections.getId(collections.create(collection));
  }

  @Test(dependsOnMethods = "createCollection")
  public void revokeUser() {
    // As Autotest:
    ObjectNode item =
        Items.json(collectionUuid, "/item/name", USER_REVOKE_ITEM, "/item/userToRevoke", USER);
    ItemId itemID = items.getId(items.create(item, 45));
    // Wait for index, make sure it's there
    searchAT.waitForIndex(itemID, USER_REVOKE_ITEM);

    // As somone else:
    // Check can't see the item
    getTokens().invalidate(USER);
    ObjectNode searchResults = searchDY.search("\"" + USER_REVOKE_ITEM + "\"");
    Assert.assertEquals(
        searchResults.get("available").asInt(), 0, "Shouldn't have been able to see the item");

    // As AutoTest:
    // Change item
    ObjectNode v2 =
        Items.json(
            collectionUuid,
            "/item/name",
            USER_REVOKE_ITEM,
            "/item/userToRevoke",
            RestTestConstants.USERID_MODERATOR1);
    v2.put("uuid", itemID.getUuid());
    v2.put("version", 1);
    items.editId(v2);

    // As someone else:
    // Check can see it now
    getTokens().invalidate(USER);
    searchDY.waitForIndex(itemID, USER_REVOKE_ITEM);
  }

  @Test(dependsOnMethods = "createCollection")
  public void revokeGroup() {
    // As Autotest:
    ObjectNode item =
        Items.json(collectionUuid, "/item/name", GROUP_REVOKE_ITEM, "/item/groupToRevoke", GROUP);
    ItemId itemID = items.getId(items.create(item, 45));
    // Wait for index, make sure it's there
    searchAT.waitForIndex(itemID, GROUP_REVOKE_ITEM);

    // As somone else:
    // Check can't edit the item
    getTokens().invalidate(USER);
    item =
        Items.json(
            collectionUuid, "/item/name", "Something different", "/item/groupToRevoke", GROUP);
    item.put("uuid", itemID.getUuid());
    item.put("version", 1);
    itemsDY.editNoPermission(item);
  }

  @Test(dependsOnMethods = "createCollection")
  public void grantRole() {
    removeArchivePriv(ROLE);
    // As Autotest:
    ObjectNode item = Items.json(collectionUuid, "/item/name", ROLE_GRANT_ITEM);
    ItemId itemID = items.getId(items.create(item, 45));
    // Wait for index, make sure it's there
    searchAT.waitForIndex(itemID, ROLE_GRANT_ITEM);

    // As someone else:
    // Check can't archive the item
    getTokens().invalidate(USER);
    itemsDY.action(itemsDY.accessDeniedRequest(), itemID, "archive");

    // As AutoTest:
    // Change item
    item = Items.json(collectionUuid, "/item/name", ROLE_GRANT_ITEM, "/item/roleToGrant", ROLE);
    item.put("uuid", itemID.getUuid());
    item.put("version", 1);
    items.editId(item);

    // As someone else:
    // Check can see it now
    getTokens().invalidate(USER);
    itemsDY.action(itemID, "archive");
  }

  @Test(dependsOnMethods = "createCollection")
  public void collectionChange() throws InterruptedException {
    // Create schema/collection
    ObjectNode collection =
        CollectionJson.json(context.getFullName("changeCollection"), schemaId, null);
    ArrayNode dynamicRules = collection.with("security").putArray("dynamicRules");
    dynamicRules.add(dynamicRule("U", "/item/userToRevoke", "DISCOVER_ITEM", false));
    String collectionUuid = collections.getId(collections.create(collection));
    String itemName = context.getFullName("collectionChangeItem");
    // Add item revoking DISCOVER for other user
    ItemId itemId =
        items.getId(
            items.create(
                Items.json(
                    collectionUuid,
                    "/item/name",
                    itemName,
                    "/item/userToRevoke",
                    USER,
                    "/item/userToRevoke2",
                    RestTestConstants.USERID_AUTOTEST),
                45));
    // Can see
    itemName = "\"" + itemName + "\"";
    searchAT.waitForIndex(itemId, itemName);
    // Other user can't see
    notVisible(searchDY, itemName, USER, itemId);
    // Change path to something else
    dynamicRules.removeAll();
    dynamicRules.add(dynamicRule("U", "/item/userToRevoke2", "DISCOVER_ITEM", false));
    collection.put("uuid", collectionUuid);
    collections.editId(collection);
    // Other user can see

    searchDY.waitForIndex(itemId, itemName);
    // Can't see
    notVisible(searchAT, itemName, RestTestConstants.USERID_AUTOTEST, itemId);
    // Delete the rule
    dynamicRules.removeAll();
    collections.editId(collection);
    // Can see
    searchAT.waitForIndex(itemId, itemName);
    searchDY.waitForIndex(itemId, itemName);
  }

  public void notVisible(SearchRequests toUse, String itemName, String userId, ItemId itemId) {
    getTokens().invalidate(userId);
    ObjectNode searchResults = toUse.search(itemName);
    Assert.assertEquals(
        searchResults.get("available").asInt(), 0, "Shouldn't have been able to see the item");
  }

  private void removeArchivePriv(String role) {
    ObjectNode instRules = acls.list();
    for (JsonNode rule : instRules.get("entries")) {
      if (rule.get("privilege").asText().equals("ARCHIVE_ITEM")) {
        String who = rule.get("who").asText();
        ObjectNode obj = (ObjectNode) rule;
        obj.put("who", who.trim() + " R:" + ROLE + " NOT AND ");
        break;
      }
    }
    acls.edit(instRules);
  }

  private ObjectNode dynamicRule(String type, String path, String priv, boolean grant) {
    ObjectNode rule = MAPPER.createObjectNode();
    rule.put("name", UUID.randomUUID().toString());
    rule.put("path", path);
    rule.put("type", type);
    ArrayNode targetList = rule.putArray("targetList");
    ObjectNode entry = targetList.objectNode();
    entry.put("granted", grant);
    entry.put("privilege", priv);
    targetList.add(entry);
    return rule;
  }
}
