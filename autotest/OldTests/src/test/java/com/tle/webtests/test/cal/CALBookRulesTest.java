package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.cal.ActivationsSummaryPage;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALOverridePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.CALWizardPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class CALBookRulesTest extends AbstractActivationsTest {
  private static final String COURSE = "A Simple Course";

  public CALBookRulesTest() {
    super("BookRules");
  }

  @Test
  public void createBookForRulesCheck() {
    createBook("Book");
    createPortion("1", "Portion 1", "Book", 1, 5, 1);
    createPortion("2", "Portion 2", "Book", 6, 10, 1);
    createPortion("3", "Portion 3", "Book", 11, 90, 2);
  }

  @Test(dependsOnMethods = "createBookForRulesCheck")
  public void moreThan10Fail() throws Exception {
    CALSummaryPage summary = searchAndView("Book");
    summary.activateDefault(1, ATTACH1_FILENAME, COURSE);
    assertTrue(summary.isActive(1, ATTACH1_FILENAME));
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(3, ATTACH1_FILENAME);
    activatePage.setCourse(COURSE);
    activatePage.activateViolation();
    activatePage.okViolation();
    assertTrue(summary.isInactive(3, ATTACH1_FILENAME));
  }

  @Test(dependsOnMethods = "createBookForRulesCheck")
  public void moreThan10SameChapter() throws Exception {
    CALSummaryPage summary = searchAndView("Book");
    summary.selectSection(3, ATTACH1_FILENAME);
    summary.selectSection(3, ATTACH2_FILENAME);
    CALActivatePage<CALSummaryPage> activatePage = summary.activateSelected();
    activatePage.setCourse(COURSE);
    activatePage.activate();
    assertTrue(summary.isActive(3, ATTACH1_FILENAME));
    assertTrue(summary.isActive(3, ATTACH2_FILENAME));
  }

  @Test(dependsOnMethods = "createBookForRulesCheck")
  public void testActivationsWithoutChapterNumber() throws Exception {
    context.setSubPrefix("NoChapter");

    // Portions with no chapter numbers
    createBook("Book");
    createPortion("", "Portion 1", "Book", 1, 5, 2);
    createPortion("", "", "Book", 6, 10, 1);

    CALSummaryPage summary = searchAndView("Book");
    String itemName = context.getFullName("Portion 1");
    assertTrue(summary.isInactive(itemName, ATTACH1_FILENAME));
    assertTrue(summary.isInactive("Unnamed", ATTACH1_FILENAME));

    // activate items to be less than 10%
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(itemName, ATTACH1_FILENAME);
    activatePage.setCourse(COURSE);
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertTrue(summary.isActive(itemName, ATTACH1_FILENAME));

    activatePage = summary.activate(itemName, ATTACH2_FILENAME);
    activatePage.setDates(getNowRange());
    activatePage.setCourse(COURSE);
    activatePage.activate();
    assertTrue(summary.isActive(itemName, ATTACH2_FILENAME));

    // assert the section holding from Portion1
    summary = searchAndView("Portion 1");
    String bookName = context.getFullName("Book");
    assertEquals(summary.getHoldingName(), bookName);

    AdminTabPage adminTab = summary.adminTab();
    CALWizardPage calPage = new CALWizardPage(context, adminTab.edit());
    calPage.setRange(0, "6-15");
    assertTrue(calPage.saveWithViolation().isLoaded());

    // NO VIOLATION and active
    summary = searchAndView("Book");
    assertTrue(summary.isActive(itemName, ATTACH1_FILENAME));
    assertTrue(summary.isActive(itemName, ATTACH2_FILENAME));
  }

  @Test(dependsOnMethods = "createBookForRulesCheck")
  public void testBookPageCountOfZero() {
    context.setSubPrefix("NoPages");
    // Book with zero pages
    createBook("Book2", "0");
    createPortion("1", "Portion 1", "Book2", 1, 5, 2);
    createPortion("2", "Portion 2", "Book2", 6, 10, 1);
    CALSummaryPage summary = searchAndView("Book2");
    assertFalse(summary.isTotalShowing());

    // No validation applies; can activate any item
    CALActivatePage<CALSummaryPage> activatePage = summary.activate(1, ATTACH1_FILENAME);
    activatePage.setCourse(COURSE);
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertTrue(summary.isActive(1, ATTACH1_FILENAME));

    activatePage = summary.activate(1, ATTACH2_FILENAME);
    activatePage.setCourse(COURSE);
    activatePage.setDates(getNowRange());
    activatePage.activate();
    assertTrue(summary.isActive(1, ATTACH2_FILENAME));

    activatePage = summary.activate(2, ATTACH1_FILENAME);
    activatePage.setDates(getNowRange());
    activatePage.setCourse(COURSE);
    activatePage.activate();
    assertTrue(summary.isActive(2, ATTACH1_FILENAME));
  }

  @Test
  public void testRomanNumerals() {
    createBook("romanBook", "MMCXXVII"); // 2127
    CALSummaryPage roman = searchAndView("romanBook");
    assertEquals(2127, roman.getTotalPagesFromSummary(), "Roman numerals didn't match");
  }

  @Test
  public void testPercentageOverride() {
    logonWithNotice("cal_COPYRIGHT_OVERRIDE", "``````");
    final String OR_BOOK = "override-book";
    final String OR_PORTION = "override-portion";
    createBook(OR_BOOK, "50");
    createPortion("", OR_PORTION, OR_BOOK, 1, 25, 1);
    CALSummaryPage cal = searchAndView(OR_BOOK);
    CALActivatePage<CALSummaryPage> activatePage =
        cal.activate(context.getFullName(OR_PORTION), ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    CALOverridePage overridePage = activatePage.activateWithOverride();
    overridePage.setReason("lmao imma activate this anyway, try and stop me #rekt");
    cal = overridePage.clickContinue();
    assertTrue(cal.isActive(context.getFullName(OR_PORTION), ATTACH1_FILENAME));
    // TODO test the override message somehow?
  }

  @Test
  public void testPagesAvailable() {
    final String BOOK = "pages-available";
    createBook(BOOK, "100");
    createPortion("1", "portion1", BOOK, 1, 5, 1);
    createPortion("2", "portion2", BOOK, 10, 30, 1);
    CALSummaryPage calSummary = searchAndView(BOOK);
    // 10% of 100 (10 out of 30 available)
    assertEquals(calSummary.getPagesAvailable(), 10);
    CALActivatePage<CALSummaryPage> activatePage = calSummary.activate(1, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    calSummary = activatePage.activate();
    // 5 active, 5 left
    assertEquals(calSummary.getPagesAvailable(), 5);
    ActivationsSummaryPage acts = calSummary.activationsTab();
    acts.delete(0);
    calSummary = searchAndView(BOOK);
    activatePage = calSummary.activate(2, ATTACH1_FILENAME);
    activatePage.setCourse("A Simple Course");
    calSummary = activatePage.activate();
    // 20 active, 0 left
    assertEquals(calSummary.getPagesAvailable(), 0);
  }
}
