package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.cal.ActivationsSummaryPage;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.EditActivationPage;
import com.tle.webtests.pageobject.cal.ManageActivationsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.testng.annotations.Test;

public class CALBookActivationTest extends AbstractActivationsTest {

  public CALBookActivationTest() {
    super("SimpleBook");
  }

  @Test
  public void createBookForActivation() {
    setupBookWithISBN("Book", "ISBN-1").publish();
    createPortion("1", "Portion 1", "Book", 1, 5, 2);
    createPortion("2", "Portion 2", "Book", 6, 10, 1);
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void checkDuplicatedISBN() {
    setupBookWithISBN("Book-Dupe", "ISBN-1").publishDuplicate().cancel(new ContributePage(context));
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void testDateValidation() {
    CALSummaryPage summary = searchAndView("Book");
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(1, ATTACH1_FILENAME);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
    cal.set(Calendar.YEAR, 2040);
    cal.set(Calendar.MONTH, 3);
    cal.set(Calendar.DAY_OF_MONTH, 8);
    Date endDate = cal.getTime();
    activatePage.setCourse("A Simple Course");

    com.tle.webtests.pageobject.generic.component.Calendar untilDate = activatePage.getUntilDate();
    untilDate.dateEquals(cal);
    activatePage.cancel();
    activatePage = summary.activate(1, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    activatePage.setDatesHidden(getInvalidRange());
    activatePage.activateFailure();
    assertTrue(activatePage.isDateError());
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void testActivateSingle() {
    CALSummaryPage summary = searchAndView("Book");
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(1, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertFalse(activatePage.isDateError());
    assertFalse(activatePage.isViolation());
    assertTrue(summary.isActive(1, ATTACH1_FILENAME));
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void testActivateMultiple() {
    CALSummaryPage summary = searchAndView("Book");
    summary.selectSection(1, ATTACH1_FILENAME);
    summary.selectSection(2, ATTACH1_FILENAME);
    CALActivatePage<CALSummaryPage> activatePage = summary.activateSelected();
    activatePage.setCourse("A Simple Course");
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertFalse(activatePage.isDateError());
    assertFalse(activatePage.isViolation());
    assertTrue(summary.isActive(1, ATTACH1_FILENAME));
    assertTrue(summary.isActive(2, ATTACH1_FILENAME));
  }

  @Test
  // (dependsOnMethods = "createBookForActivation")
  public void testCopyrightTab() {
    createDifferentActivations();
    CALSummaryPage summary = searchAndView("Book");
    ActivationsSummaryPage copyrightTab = summary.activationsTab();
    assertEquals(copyrightTab.getStatus(0), "Active");
    assertEquals(copyrightTab.getStatus(1), "Pending");
    assertEquals(copyrightTab.getStatus(2), "Inactive");
    assertTrue(copyrightTab.containsLink(0, "mailto:courseowner@owner.com"));
    copyrightTab.delete(0);
    assertEquals(copyrightTab.getStatus(0), "Pending");
  }

  private void createDifferentActivations() {
    CALSummaryPage summary = searchAndView("Book");
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(1, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertTrue(summary.isActive(1, ATTACH1_FILENAME));

    activatePage = summary.activate(1, ATTACH2_FILENAME);
    activatePage.setCourse("A Simple Course");
    activatePage.setDates(getFutureRange());
    activatePage.activate();
    assertFalse(summary.isActive(1, ATTACH2_FILENAME));

    activatePage = summary.activate(2, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertTrue(summary.isActive(2, ATTACH1_FILENAME));

    ManageActivationsPage activations = new ManageActivationsPage(context).load();
    String portion2Name = context.getFullName("Portion 2");
    activations.search('"' + portion2Name + '"');
    assertTrue(activations.hasResults());
    activations.results().setChecked(portion2Name, true);
    assertTrue(activations.bulk().executeCommand("deactivate"));
  }

  @Test
  public void testCitationDisplayTemplate() {
    String citBook = "citation-book";
    String citPortion = "citation-portion";
    createBook(
        citBook, "100", "CAL Guide Books Collection - citation displayed", "EYE ESS BEE ENN");
    createPortion("IX", citPortion, citBook, 1, 6, 1);
    CALSummaryPage book = searchAndView(citBook);
    assertTrue(book.hasCitation());
    String citation = book.getCitation();
    assertTrue(citation.contains(citBook));
    assertFalse(citation.contains(citPortion));
    CALSummaryPage portion = searchAndView(citPortion);
    assertTrue(portion.hasCitation());
    citation = portion.getCitation();
    assertTrue(citation.contains(citPortion));
    assertTrue(citation.contains(citBook));
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void testEditActivation() {
    createDifferentActivations();
    logonWithNotice("cal_EDIT_ACTIVATION_ITEM", "``````");
    CALSummaryPage summary = searchAndView("Book");
    ActivationsSummaryPage copyrightTab = summary.activationsTab();
    assertTrue(copyrightTab.canEdit(0)); // active
    assertTrue(copyrightTab.canEdit(1)); // pending
    assertFalse(copyrightTab.canEdit(2)); // inactive
    // edit active
    String info = copyrightTab.getInfo(0);
    EditActivationPage editPage = copyrightTab.edit(0);
    assertTrue(editPage.fromAndCourseSelectorDisabled());
    copyrightTab = editPage.editActiveActivation(getFutureRange()[1]);
    assertNotEquals(info, copyrightTab.getInfo(0), "active activation not edited");
    // edit pending
    info = copyrightTab.getInfo(1);
    editPage = copyrightTab.edit(1);
    editPage.setDateRange(getNowRange());
    editPage.saveWithError();
    assertTrue(editPage.errorPresent());
    // original dates - change course
    copyrightTab = editPage.editPendingActivation("Sample Test Course", getFutureRange());
    assertNotEquals(info, copyrightTab.getInfo(1), "pending activation not edited");
  }

  @Test(dependsOnMethods = "createBookForActivation")
  public void testManageActivationsFilter() {
    createDifferentActivations();
    ManageActivationsPage manPage = new ManageActivationsPage(context).load();
    manPage.search(context.getNamePrefix() + " " + context.getSubPrefix());
    manPage.filterByStatus("All statuses");
    assertEquals(manPage.results().getTotalAvailable(), 3);
    manPage.filterByStatus("Active");
    assertEquals(manPage.results().getTotalAvailable(), 1);
    manPage.filterByStatus("Inactive");
    assertEquals(manPage.results().getTotalAvailable(), 1);
    manPage.filterByStatus("Pending");
    assertEquals(manPage.results().getTotalAvailable(), 1);
    manPage.resetFilters();
    assertEquals(manPage.results().getTotalAvailable(), 3);
  }
}
