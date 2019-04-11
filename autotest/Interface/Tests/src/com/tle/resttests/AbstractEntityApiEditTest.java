package com.tle.resttests;

import static com.tle.json.entity.AclLists.everyoneWho;
import static com.tle.json.entity.AclLists.rule;
import static com.tle.json.entity.AclLists.userWho;
import static com.tle.json.entity.BaseEntityJson.addRule;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class AbstractEntityApiEditTest extends AbstractEntityCreatorTest {
  protected BaseEntityRequests normalRequests;
  private BaseEntityRequests editPrivRequests;

  protected abstract BaseEntityRequests createRequestsWithBuilder(RequestsBuilder builder);

  protected abstract ObjectNode createJsonForPrivs(String fullName);

  protected abstract ObjectNode createJsonForEdit(String fullName);

  protected abstract String getEditPrivilege();

  protected abstract void assertExtraEdits(ObjectNode edited);

  protected abstract void extraEdits(ObjectNode client);

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    normalRequests = createRequestsWithBuilder(builder());
    editPrivRequests =
        createRequestsWithBuilder(builder().user(RestTestConstants.USERID_RESTNOPRIV));
  }

  @Test
  public void testBasicPrivs() {
    ObjectNode json = createJsonForPrivs(context.getFullName("Privs"));
    addRule(
        json, rule(getEditPrivilege(), true, false, userWho(RestTestConstants.USERID_RESTNOPRIV)));
    addRule(json, rule(getEditPrivilege(), false, false, everyoneWho()));
    json = normalRequests.create(json);
    normalRequests.editNoPermission(json);
    json.with("security").putArray("rules");
    editPrivRequests.editId(json);
    normalRequests.editId(json);
  }

  @Test
  public void testBasicEdit() {
    ObjectNode client = normalRequests.create(createJsonForEdit(context.getFullName("Editing")));
    extraEdits(client);
    client.put("name", "changed");
    client.put("description", "changed");
    client.remove("nameStrings");
    client.remove("descriptionStrings");
    // Cannot edit owner without import=true
    // client.with("owner").put("id", RestTestConstants.USERID_MODERATOR1);

    String uuid = normalRequests.editId(client);
    ObjectNode edited = normalRequests.get(uuid);
    assertExtraEdits(edited);
    // Assert.assertEquals(edited.with("owner").get("id").asText(),
    // RestTestConstants.USERID_MODERATOR1);
    Assert.assertEquals(edited.get("name").asText(), "changed");
    Assert.assertEquals(edited.get("description").asText(), "changed");
  }
}
