package com.tle.webtests.test.users;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.files.Attachments;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("ldap")
public class LDAPTest extends AbstractSessionTest {
  @DataProvider
  public Object[][] institutions() {
    return new Object[][] {{"ldap"}};
  }

  private InMemoryDirectoryServer ldapServer;
  private String LDAP_USER_ANDREW = "andrew.gibb";
  private String LDAP_USER_AARON = "aaron.holland";
  private String LDAP_USER_TEST = "test.user";
  private String LDAP_USER_NICHOLAS = "nicholas.read";
  private String LDAP_USER_NICK = "nick.charles";
  private String LDAP_PASSWORD = "Tle160cst";

  @BeforeClass
  @Override
  public void setupContext(ITestContext testContext) throws IOException {
    super.setupContext(testContext);
    startLdap();
  }

  @Test(dataProvider = "institutions")
  public void ldapLogon(String inst) {
    PageContext context = newContext(inst);
    logon(context, LDAP_USER_ANDREW, LDAP_PASSWORD);

    SearchPage sp = new SearchPage(context).load();
    sp.setOwnerFilter(LDAP_USER_ANDREW);
    assertTrue(sp.isOwnerSelected(LDAP_USER_ANDREW));
    logout(context);

    LoginPage lp = new LoginPage(context).load();
    lp = lp.loginWithError(LDAP_USER_ANDREW, "fail");
    assertEquals(lp.getLoginFailure(), "Sorry, the details you supplied were invalid.");
  }

  // Simple group based permissions
  @Test(dataProvider = "institutions")
  public void ldapPermissions(String inst) {
    PageContext context = newContext(inst);
    logon(context, LDAP_USER_ANDREW, LDAP_PASSWORD);
    SearchPage sp = new SearchPage(context).load();
    ItemListPage results = sp.search("cool");
    assertEquals(results.getResult(1).getTitle(), "LDAPTest - Cool");
    logout(context);

    logon(context, LDAP_USER_AARON, LDAP_PASSWORD);
    sp = new SearchPage(context).load();
    sp.search("cool");
    assertFalse(sp.hasResults());
    results = sp.search("uncool");
    assertEquals(results.getResult(1).getTitle(), "LDAPTest - Uncool");
    logout(context);

    logon(context, LDAP_USER_TEST, LDAP_PASSWORD);
    sp = new SearchPage(context).load();
    sp.search("cool");
    assertFalse(sp.hasResults());
    sp.search("uncool");
    assertFalse(sp.hasResults());
    results = sp.search("");
    assertEquals(results.getResult(1).getTitle(), "LDAPTest - All");
    logout(context);
  }

  @Test
  // Nested group based permission
  public void ldapNestedPermissions() {
    // TODO use open LDAP
    PageContext context = newContext("ldapad");
    logon(context, LDAP_USER_NICK, LDAP_PASSWORD);
    SearchPage sp = new SearchPage(context).load();
    assertTrue(sp.hasResults());
    assertEquals(
        sp.search("LDAPTest - Groupception").getResult(1).getTitle(), "LDAPTest - Groupception");
    logout(context);
  }

  @Test
  public void ldapNestedGroupRoles() {
    // TODO use open LDAP
    PageContext context = newContext("ldapad");
    logon(context, LDAP_USER_NICK, LDAP_PASSWORD);
    SearchPage sp = new SearchPage(context).load();
    assertTrue(sp.hasResults());
    ItemListPage ilp = sp.search("LDAPTest - Role*");
    assertTrue(ilp.doesResultExist("LDAPTest - Role1"));
    assertTrue(ilp.doesResultExist("LDAPTest - Role2"));
    logout(context);
  }

  // Select user controls limited by groups
  @Test(dataProvider = "institutions")
  public void testSearchUsers(String inst) {
    PageContext context = newContext(inst);
    logon(context, LDAP_USER_ANDREW, LDAP_PASSWORD);
    WizardPageTab openWizard = new MenuSection(context).get().clickContribute("Basic Items");

    // Cool group
    SelectUserDialog userDialog = openWizard.selectUser(3).openDialog();
    assertTrue(userDialog.search("andrew").containsUsername(LDAP_USER_ANDREW));
    assertTrue(userDialog.searchWithoutMatch(LDAP_USER_AARON));
    assertTrue(userDialog.searchWithoutMatch(LDAP_USER_NICHOLAS));
    openWizard = userDialog.cancel(openWizard);

    // Uncool group
    userDialog = openWizard.selectUser(4).openDialog();
    assertTrue(userDialog.search("aaron").containsUsername(LDAP_USER_AARON));
    assertTrue(userDialog.searchWithoutMatch("andrew"));
    openWizard = userDialog.cancel(openWizard);

    // No group
    userDialog = openWizard.selectUser(5).openDialog();
    assertTrue(userDialog.search("test").containsUsername(LDAP_USER_TEST));
    assertTrue(userDialog.search("william").containsUsername("william.bowling"));

    logout(context);
  }

  // acls, itemdefinition, properties
  @Test
  public void testViaAdvancedScripts() {
    // TODO: open ldap and roles(?)
    final List<String> charliesGroups = Arrays.asList("dev", "support");
    PageContext context = newContext("ldapad");
    logon(context, LDAP_USER_NICK, LDAP_PASSWORD);
    WizardPageTab openWizard = new MenuSection(context).get().clickContribute("Basic Items");
    openWizard.editbox(1, getClass().getSimpleName() + " testing");
    SummaryPage summary = openWizard.save().publish();
    List<String> values = summary.getValuesByCustomDisplay();
    // search groups for "alpha" group
    assertEquals(values.get(0), "dev");
    // members of alpha and it's child groups
    assertEqualsNoOrder(
        values.get(1).split(", "),
        new String[] {"[Andrew Gibb [andrew.gibb]", "Nick Charles [nick.charles]]"});
    // first group returned that nick is a member of
    assertTrue(charliesGroups.contains(values.get(2)));
    // is nick a member of this group (always)
    assertTrue(Boolean.valueOf(values.get(3)));
    // what groups is nick a member of
    assertEqualsNoOrder(values.get(4).split(", "), charliesGroups.toArray());
    logout();
  }

  private void startLdap() {
    try {
      InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=edalex,dc=com");
      config.addAdditionalBindCredentials("cn=equella", "password");
      config.setBaseDNs("dc=edalex,dc=com");
      config.setSchema(null);
      config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", 10389));
      ldapServer = new InMemoryDirectoryServer(config);
      ldapServer.importFromLDIF(true, Attachments.get("ldap_test.ldif").getPath());
      ldapServer.startListening();
    } catch (LDAPException e) {
      throw new RuntimeException("Failed to start LDAP server");
    }
  }

  @AfterClass
  @Override
  public void finishedClass(ITestContext testContext) throws Exception {
    super.finishedClass(testContext);
    ldapServer.shutDown(true);
  }
}
