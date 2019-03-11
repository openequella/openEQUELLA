package com.tle.webtests.test.users;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.test.AbstractSessionTest;
import java.net.URLEncoder;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class TokenLoginTest extends AbstractSessionTest {

  @Test
  public void testLoginWithVerifyRole() throws Exception {
    logon("tokenuser", "``````");
    HomePage home = new HomePage(context).load();
    assertFalse(home.containsLink("Test Role Link", "http://www.google.com"));
    logout();

    String token = TokenSecurity.createSecureToken("tokenuser", "sso_ip", "sso_ip", null);
    openHome(token);
    assertTrue(home.get().containsLink("Test Role Link", "http://www.google.com"));
  }

  private void openHome(String token) throws Exception {
    context
        .getDriver()
        .get(context.getBaseUrl() + "home.do?token=" + URLEncoder.encode(token, "UTF-8"));
  }

  @Test
  public void testLoginWithGroup() throws Exception {
    logon("tokenuser2", "``````");
    HomePage home = new HomePage(context).load();
    assertFalse(home.containsLink("Test Role Link", "http://www.google.com"));
    logout();
    String token = TokenSecurity.createSecureToken("tokenuser2", "sso_group", "sso_group", null);
    openHome(token);
    assertTrue(home.get().containsLink("Test Role Link", "http://www.google.com"));
    logout();
    token = TokenSecurity.createSecureToken("tokenuser", "sso_group", "sso_group", null);
    openHome(token);
    assertTrue(isTextPresent("You do not have permissions to use this token"));
  }
}
