package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.oauth.OAuthClientEditorPage;
import com.tle.webtests.pageobject.oauth.OAuthSettingsPage;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("rest")
public class OAuthClientEditorTest extends AbstractCleanupTest {
  // For the purposes of the test lets assume acg = redirect url set
  // && implicit grant = default redirect
  private static final String ACG_NAME = "OAuthClientEditorTest - Authorisation code grant";
  private static final String ACG_CID =
      "OAuthClientEditorTest - Authorisation code grant, Client ID";
  // Before you try, no it doesn't really exist :)
  private static final String ACG_URL = "http://www.somebogusredirecturl.com";
  private static final String IG_NAME = "OAuthClientEditorTest - Implicit grant";
  private static final String IG_CID = "OAuthClientEditorTest - Implicit grant, Client ID";
  private static final String CCG_NAME = "OAuthClientEditorTest - Client credentials grant";

  String savedSecret;

  /**
   * @see Redmine: #7227
   */
  // Most of the REST tests do this indirectly, but let's give it a good
  // thrash here
  @Test
  public void setupClients() {
    logon("autotest", "automated");
    OAuthSettingsPage oauthSP = new OAuthSettingsPage(context).load();

    // General error testing
    OAuthClientEditorPage editPage = oauthSP.newClient();
    assertEquals(editPage.getName(), "", "The name was not initialized as blank");
    assertPageIsNone(editPage);
    editPage.saveWithErrors();
    assertTrue(editPage.isNameError());
    assertTrue(editPage.isFlowError());
    editPage.setName(ACG_NAME);
    editPage.saveWithErrors();
    assertTrue(editPage.isFlowError());
    assertFalse(editPage.isNameError());

    // ACG
    assertTrue(isUuid(editPage.getClientId()), "Client id was not automatically set");
    editPage.setClientId(ACG_CID);

    assertTrue(isUuid(editPage.getSecret()), "Client secret was not automatically set");
    String secret = editPage.getSecret();
    editPage.regenerateSecret();
    assertNotEquals(
        secret,
        editPage.getSecret(),
        "Either the client secret was not regenerated or two random Uuid's were equal (not"
            + " likely)");
    savedSecret = editPage.getSecret();

    assertEquals(editPage.getSelectedFlow(), "");
    editPage.setFlow("acg");
    assertPageIsACG(editPage);
    editPage.saveWithErrors();
    assertTrue(editPage.isUrlError());

    editPage.setRedirectUrl(ACG_URL);
    oauthSP = editPage.save();
    editPage = oauthSP.editClient(ACG_NAME);
    assertPageIsACG(editPage);
    assertEquals(editPage.getName(), ACG_NAME);
    assertEquals(editPage.getClientId(), ACG_CID);
    assertEquals(editPage.getSecret(), savedSecret);

    oauthSP = editPage.cancel();

    // IG
    editPage = oauthSP.newClient();
    editPage.setFlow("ig");

    editPage.setName(IG_NAME);
    editPage.setClientId(ACG_CID);
    editPage.saveWithErrors();
    assertTrue(editPage.isClientIdError()); // Unique constraint
    editPage.setClientId(IG_CID);
    editPage.setDefaultUrl();

    assertPageIsIG(editPage);

    oauthSP = editPage.save();
    editPage = oauthSP.editClient(IG_NAME);

    assertPageIsIG(editPage);

    oauthSP = editPage.cancel();

    // CCG
    editPage = oauthSP.newClient();
    editPage.setFlow("ccg");

    assertPageIsCCG(editPage);

    editPage.setName(CCG_NAME);

    editPage.saveWithErrors();
    assertTrue(editPage.isUserError());
    editPage.setUser("AutoTest");

    oauthSP = editPage.save();
    editPage = oauthSP.editClient(CCG_NAME);

    assertPageIsCCG(editPage);
    assertEquals(editPage.getUser(), "AutoTest");

    oauthSP = editPage.cancel();
  }

  private boolean isUuid(String clientId) {
    return (clientId.length() == 36 && clientId.charAt(8) == '-');
  }

  private void assertPageIsACG(OAuthClientEditorPage editPage) {
    assertEquals(editPage.getSelectedFlow(), "acg");
    assertFalse(editPage.canSetChooseUrl());
    assertFalse(editPage.isDefaultUrl());
    assertTrue(editPage.canSetUrl());
    assertFalse(editPage.canSetUser());
  }

  private void assertPageIsIG(OAuthClientEditorPage editPage) {
    assertEquals(editPage.getSelectedFlow(), "ig");
    assertTrue(editPage.canSetChooseUrl());
    assertTrue(editPage.isDefaultUrl());
    assertFalse(editPage.canSetUrl());
    assertFalse(editPage.canSetUser());
  }

  private void assertPageIsCCG(OAuthClientEditorPage editPage) {
    assertEquals(editPage.getSelectedFlow(), "ccg");
    assertFalse(editPage.canSetChooseUrl());
    assertFalse(editPage.canSetUrl());
    assertTrue(editPage.canSetUser());
  }

  private void assertPageIsNone(OAuthClientEditorPage editPage) {
    assertEquals(editPage.getSelectedFlow(), "");
    assertFalse(editPage.canSetChooseUrl());
    assertFalse(editPage.canSetUrl());
    assertFalse(editPage.canSetUser());
  }

  @Override
  protected void cleanupAfterClass() {
    logon("autotest", "automated");
    OAuthSettingsPage oauthSP = new OAuthSettingsPage(context).load();
    if (oauthSP.clientExists(ACG_NAME)) {
      oauthSP.deleteClient(ACG_NAME);
    }
    if (oauthSP.clientExists(IG_NAME)) {
      oauthSP.deleteClient(IG_NAME);
    }
    if (oauthSP.clientExists(CCG_NAME)) {
      oauthSP.deleteClient(CCG_NAME);
    }
  }
}
