package com.tle.webtests.failalways;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractIntegrationTest;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class CancelWizardTest extends AbstractIntegrationTest {
  private static String USERNAME = "AutoTest";
  private static String PASSWORD = "automated";
  private static String SHAREDID = "contribute";
  private static String SECRET = "contribute";
  private static String ACTION = "selectOrAdd";
  private static String COLLECTION = "Basic Items";

  public CancelWizardTest() {
    setDeleteCredentials(USERNAME, PASSWORD);
  }

  @Test
  public void cancelFromSelection() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);
    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    ContributePage contribute = session.contribute();
    contribute.openWizard(COLLECTION).cancel(contribute);

    // ensure on contribute page
    assertTrue(contribute.get().hasCollection(COLLECTION));
  }

  @Test
  public void cancelFromOneCollectionSelection() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(
        ACTION,
        USERNAME,
        "",
        "contributionCollectionIds=b28f1ffe-2008-4f5e-d559-83c8acd79316",
        true);

    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    session.contributeSingle().cancel(session);

    // ensure on selection page
    assertTrue(isTextPresent("Contribute a new item"));
  }

  @Test(dependsOnMethods = "addItem")
  public void cancelFromEdit() {
    String fullName = context.getNamePrefix() + " an item";

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);
    SummaryPage view =
        itp.clickPostToUrlButton(new SelectionSession(context))
            .homeExactSearch(fullName)
            .viewFromTitle(fullName);
    view = view.adminTab().edit().cancel(view);

    // ensure on summary page
    assertEquals(view.getItemTitle(), fullName);
  }

  @Test(dependsOnMethods = "addItem")
  public void cancelFromNewVersion() {
    String fullName = context.getNamePrefix() + " an item";

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);
    SummaryPage view =
        itp.clickPostToUrlButton(new SelectionSession(context))
            .homeExactSearch(fullName)
            .viewFromTitle(fullName);
    ContributePage contributePage =
        view.adminTab().newVersion().cancel(new ContributePage(context));

    // ensure on contribute page
    assertTrue(contributePage.hasCollection(COLLECTION));
  }

  @Test(dependsOnMethods = "addItem")
  public void cancelFromRedraft() {
    String fullName = context.getNamePrefix() + " an item";

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);

    SummaryPage view =
        itp.clickPostToUrlButton(new SelectionSession(context))
            .homeExactSearch(fullName)
            .viewFromTitle(fullName);
    view = view.adminTab().redraft().cancel(view);

    // ensure on summary page
    assertEquals(view.getItemTitle(), fullName);
  }

  @Test
  public void addItem() {
    String fullName = context.getNamePrefix() + " an item";

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);

    WizardPageTab wizard =
        itp.clickPostToUrlButton(new SelectionSession(context)).contribute().openWizard(COLLECTION);

    wizard.editbox(1, fullName);
    wizard.save().publish();
  }
}
