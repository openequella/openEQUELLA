package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.cal.ActivationsSummaryPage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.ManageActivationsPage;
import com.tle.webtests.pageobject.searching.BulkResultsPage;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CALPrivilegeTest extends AbstractCALTest {
  private static final String CONTRIBUTED_BOOK = "Book";
  private static final String DEACTIVATE_BULKOP = "deactivate";
  private static final String DELETE_BULKOP = "delete";
  private static final String PORTION_NAME = "Portion 1";
  private static final String PORTION_2_NAME = "Portion 2";
  private static final String ACTIVE_ATTACHMENT = "methodology_template.docx"; // $NON-NLS-1$
  private static final String BOOK_NAME = "Basic Book for CAL Functionality Testing"; // $NON-NLS-1$
  private static final String DEFAULT_COURSE = "A Simple Course";

  public CALPrivilegeTest() {
    setDeleteCredentials("caladmin", "``````");
  }

  @Test
  public void contribute() {
    logon("caladmin", "``````");
    createBook(CONTRIBUTED_BOOK);
    createPortion("1", PORTION_NAME, CONTRIBUTED_BOOK, 1, 10, 1);
    createPortion("2", PORTION_2_NAME, CONTRIBUTED_BOOK, 11, 20, 1);
  }

  @Override
  @BeforeMethod
  public void setupSubcontext(Method testMethod) {
    context.setNamePrefix(namePrefix);
  }

  @SuppressWarnings("nls")
  @Test
  public void testActivatePrivilege() {
    context.setNamePrefix(null);
    new LoginPage(context).load().login("cal_COPYRIGHT_ITEM", "``````");
    assertTrue(searchAndView(BOOK_NAME).canActivate(1, "Issues_List.doc"));
    new LoginPage(context).load().login("cal_DELETE_ACTIVATION_ITEM", "``````");
    assertFalse(searchAndView(BOOK_NAME).canActivate(1, "Issues_List.doc"));
  }

  @SuppressWarnings("nls")
  @Test
  public void testViewPrivilege() {
    context.setNamePrefix(null);
    new LoginPage(context).load().login("cal_VIEW_ACTIVATION_ITEM", "``````");
    CALSummaryPage summaryPage = searchAndView(BOOK_NAME);
    assertTrue(summaryPage.isActive(2, ACTIVE_ATTACHMENT));
    ActivationsSummaryPage activationsTab = summaryPage.activationsTab();
    assertEquals(activationsTab.getStatus(0), "Active");

    new LoginPage(context).load().login("cal_COPYRIGHT_ITEM", "``````");
    assertFalse(searchAndView(BOOK_NAME).isActivationsAvailable());
  }

  @SuppressWarnings("nls")
  @Test
  public void testDeletePrivilege() {
    context.setNamePrefix(null);
    new LoginPage(context).load().login("cal_VIEW_DELETE_ACTIVATION_ITEM", "``````");
    CALSummaryPage summaryPage = searchAndView(BOOK_NAME);
    assertTrue(summaryPage.isActive(2, ACTIVE_ATTACHMENT));
    ActivationsSummaryPage activationsTab = summaryPage.activationsTab();
    assertTrue(activationsTab.canDelete(0));

    new LoginPage(context).load().login("cal_VIEW_ACTIVATION_ITEM", "``````");
    summaryPage = searchAndView(BOOK_NAME);
    assertTrue(summaryPage.isActive(2, ACTIVE_ATTACHMENT));
    activationsTab = summaryPage.activationsTab();
    assertFalse(activationsTab.canDelete(0));
  }

  @Test(dependsOnMethods = "testBulkDelete")
  public void testDeactivatePrivilege() {
    new LoginPage(context).load().login("cal_COPYRIGHT_ITEM", "``````");
    CALSummaryPage summaryPage = searchAndView(CONTRIBUTED_BOOK);
    summaryPage.activateDefault(1, ATTACH1_FILENAME, DEFAULT_COURSE);
    assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

    String portionFullName = context.getFullName(PORTION_NAME);
    // Fail to deactivate
    new LoginPage(context).load().login("cal_VIEW_ACTIVATION_ITEM", "``````");
    ManageActivationsPage activations = new ManageActivationsPage(context).load();
    activations.search('"' + portionFullName + '"');
    assertTrue(activations.hasResults());
    assertTrue(activations.results().isActive(portionFullName));
    activations.results().setChecked(portionFullName, true);
    assertTrue(
        activations
            .bulk()
            .executeCommandFailure(DEACTIVATE_BULKOP)
            .errorsContain("You do not have the required privileges [DEACTIVATE_ACTIVATION_ITEM]"));

    // Do the real deactivate
    new LoginPage(context).load().login("cal_VIEW_DEACTIVATE_ACTIVATION_ITEM", "``````");
    activations = new ManageActivationsPage(context).load();
    activations.search('"' + portionFullName + '"');
    assertTrue(activations.hasResults());
    assertTrue(activations.results().isActive(portionFullName));
    activations.results().setChecked(portionFullName, true);
    assertTrue(activations.bulk().executeCommand(DEACTIVATE_BULKOP));
    assertFalse(activations.results().isActive(portionFullName));
  }

  @SuppressWarnings("nls")
  @Test(dependsOnMethods = {"testViewInactivePortions", "testViewLinkedPortions"})
  public void testBulkDelete() throws Exception {
    new LoginPage(context).load().login("cal_COPYRIGHT_ITEM", "``````");
    CALSummaryPage summaryPage = searchAndView(CONTRIBUTED_BOOK);
    summaryPage.activateDefault(1, ATTACH1_FILENAME, DEFAULT_COURSE);
    assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

    String portionFullName = context.getFullName(PORTION_NAME);

    // Fail to delete
    new LoginPage(context).load().login("cal_VIEW_ACTIVATION_ITEM", "``````");
    ManageActivationsPage activations = new ManageActivationsPage(context).load();
    activations.search('"' + portionFullName + '"');
    assertTrue(activations.results().isActive(portionFullName));
    activations.results().setChecked(portionFullName, true);
    BulkResultsPage bulkResults = activations.bulk().executeCommandFailure(DELETE_BULKOP);
    assertTrue(
        bulkResults.errorsContain(
            "You do not have the required privileges [DELETE_ACTIVATION_ITEM]"),
        "Expected error message 'You do not have the required privileges [DELETE_ACTIVATION_ITEM]'"
            + " not found");
    activations = bulkResults.close(activations);
    assertTrue(activations.results().isActive(portionFullName));

    // Successful delete
    new LoginPage(context).load().login("cal_VIEW_DELETE_ACTIVATION_ITEM", "``````");
    activations = new ManageActivationsPage(context).load();
    activations.search('"' + portionFullName + '"');
    assertTrue(activations.results().isActive(portionFullName));
    activations.results().setChecked(portionFullName, true);
    assertTrue(activations.bulk().executeCommand(DELETE_BULKOP));
    assertFalse(activations.hasResults());
  }

  @Test(dependsOnMethods = "contribute")
  public void testViewInactivePortions() {
    logon("caladmin", "``````");
    CALSummaryPage book = searchAndView(CONTRIBUTED_BOOK);
    assertFalse(book.isActive(1, ATTACH1_FILENAME));
    assertFalse(book.canViewSection(1, ATTACH1_FILENAME));
    logonWithNotice("cal_VIEW_INACTIVE_PORTIONS", "``````");
    book = searchAndView(CONTRIBUTED_BOOK);
    assertFalse(book.isActive(1, ATTACH1_FILENAME));
    assertTrue(book.canViewSection(1, ATTACH1_FILENAME));
  }

  @Test(dependsOnMethods = "contribute")
  public void testViewLinkedPortions() {
    // Actually testing denying of this priv
    logon("caladmin", "``````");
    CALSummaryPage calSummary = searchAndView(CONTRIBUTED_BOOK);
    assertTrue(calSummary.viewPortionLinkPresent(1, ATTACH1_FILENAME));
    calSummary = searchAndView(PORTION_NAME);
    assertTrue(calSummary.otherPortionsLinksPresent());
    logonWithNotice("cal_VIEW_LINKED_PORTIONS", "``````");
    calSummary = searchAndView(CONTRIBUTED_BOOK);
    assertFalse(calSummary.viewPortionLinkPresent(1, ATTACH1_FILENAME));
    calSummary = searchAndView(PORTION_2_NAME);
    assertFalse(calSummary.otherPortionsLinksPresent());
  }
}
