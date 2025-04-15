package com.tle.webtests.test.cal;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.CALWizardPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractIntegrationTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import java.util.TimeZone;
import java.util.UUID;

@TestInstitution("cal")
public class AbstractCALTest extends AbstractIntegrationTest {
  private static final String JOURNAL_COLLECTION = "CAL Guide Journals Collection";
  private static final String PORTION_COLLECTION = "CAL Guide Portion Collection";
  protected static final String BOOK_COLLECTION = "CAL Guide Books Collection";
  public static final String ATTACH1_FILENAME = "page.html";
  public static final String ATTACH2_FILENAME = "page(2).html";

  private String getExpectedFilename(URL file, int offset) {
    String filename = AbstractWizardControlPage.filenameFromURL(file);
    if (offset == 0) {
      return filename;
    }
    int dotind = filename.indexOf('.');
    return filename.substring(0, dotind) + "(" + (offset + 1) + ")" + filename.substring(dotind);
  }

  public AbstractCALTest(String itemPrefix) {
    super(itemPrefix);
  }

  public AbstractCALTest() {
    super();
  }

  protected SummaryPage createBook(String bookName) {
    return createBook(bookName, "100");
  }

  protected CALWizardPage setupBookWithISBN(String bookName, String isbn) {
    return setupBook(bookName, "100", BOOK_COLLECTION, isbn);
  }

  protected SummaryPage createBookInCollection(String bookName, String collection) {
    return createBook(bookName, "100", collection, UUID.randomUUID().toString());
  }

  protected SummaryPage createBook(String bookName, String numPages) {
    return createBook(bookName, numPages, BOOK_COLLECTION, UUID.randomUUID().toString());
  }

  protected CALWizardPage setupBook(
      String bookName, String numPages, String collection, String isbn) {
    WizardPageTab wizardPage = new ContributePage(context).load().openWizard(collection);
    CALWizardPage calWizard = new CALWizardPage(context, wizardPage);
    calWizard.setHoldingTitle(context.getFullName(bookName));
    calWizard.addISBN(isbn);
    calWizard.setYearOfPublication("2009");
    calWizard.setPublisher("TLE");
    calWizard.setTotalPages(numPages);
    return calWizard;
  }

  protected SummaryPage createBook(
      String bookName, String numPages, String collection, String isbn) {
    return setupBook(bookName, numPages, collection, isbn).publish();
  }

  protected SummaryPage createPortion(
      String chapter, String title, String book, int firstPage, int lastPage, int numsections) {
    return createPortion(
        chapter, title, book, firstPage, lastPage, numsections, Attachments.get(ATTACH1_FILENAME));
  }

  protected SummaryPage createPortion(
      ContributePage contributePage,
      String chapter,
      String title,
      String book,
      int firstPage,
      int lastPage,
      int numsections) {
    return publishPortion(
        contributePage,
        chapter,
        title,
        book,
        firstPage,
        lastPage,
        numsections,
        Attachments.get(ATTACH1_FILENAME));
  }

  protected SummaryPage createPortion(
      String chapter,
      String title,
      String book,
      int firstPage,
      int lastPage,
      int numsections,
      URL filename) {
    return publishPortion(
        new ContributePage(context).load(),
        chapter,
        title,
        book,
        firstPage,
        lastPage,
        numsections,
        filename);
  }

  private SummaryPage publishPortion(
      ContributePage contributePage,
      String chapter,
      String title,
      String book,
      int firstPage,
      int lastPage,
      int numsections,
      URL filename) {
    book = context.getFullName(book);
    WizardPageTab wizardPage = contributePage.openWizard(PORTION_COLLECTION);
    CALWizardPage calWizardPage = new CALWizardPage(context, wizardPage);
    calWizardPage.selectBook(book);
    calWizardPage.setChapter(chapter);
    calWizardPage.setPortionTitle(title.isEmpty() ? "" : context.getFullName(title));
    addSections(calWizardPage, firstPage, lastPage, numsections, filename);
    return calWizardPage.publish();
  }

  private void addSections(
      CALWizardPage calWizardPage, int firstPage, int lastPage, int numsections, URL filename) {
    int numPages = lastPage - firstPage + 1;
    int perSection = numPages / numsections;
    for (int i = 0; i < numsections; i++) {
      if (i > 0) {
        calWizardPage.addSection();
      }
      int newLast = i == numsections - 1 ? lastPage : firstPage + perSection - 1;
      calWizardPage.setRange(i, firstPage + "-" + newLast);
      calWizardPage.uploadSectionFile(i, filename, getExpectedFilename(filename, i));
      firstPage += perSection;
    }
  }

  protected AdminTabPage searchAndViewAdmin(String itemName) {
    itemName = context.getFullName(itemName);
    return new ItemAdminPage(context).load().viewItem(itemName).adminTab();
  }

  protected CALSummaryPage searchAndView(String itemName) {
    itemName = context.getFullName(itemName);
    return new ItemAdminPage(context).load().viewItem(itemName).cal();
  }

  /**
   * Uses Caladmin's time zone, America/Chicago
   *
   * @return
   */
  protected java.util.Calendar[] getNowRange() {
    return getNowRange(TimeZone.getTimeZone("America/Chicago"));
  }

  protected java.util.Calendar[] getNowRange(TimeZone zone) {
    return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(zone, false, false);
  }

  /**
   * Uses Caladmin's time zone, America/Chicago
   *
   * @return
   */
  protected java.util.Calendar[] getFutureRange() {
    return getFutureRange(TimeZone.getTimeZone("America/Chicago"));
  }

  protected java.util.Calendar[] getFutureRange(TimeZone zone) {
    return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(zone, false, true);
  }

  protected java.util.Calendar[] getInvalidRange() {
    return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(
        TimeZone.getTimeZone("Etc/UTC"), true, false);
  }

  @SuppressWarnings("nls")
  protected SummaryPage createJournal(String title, String volume) {
    return createJournal(title, volume, JOURNAL_COLLECTION);
  }

  @SuppressWarnings("nls")
  protected SummaryPage createJournal(String title, String volume, String collection) {
    WizardPageTab wizardPage = new ContributePage(context).load().openWizard(collection);
    CALWizardPage calWizard = new CALWizardPage(context, wizardPage);
    calWizard.setISSN(UUID.randomUUID().toString());
    calWizard.setHoldingTitle(context.getFullName(title));
    calWizard.setJournalNotes("simple notes");
    calWizard.setJournalVolume(volume);
    return calWizard.publish();
  }

  protected SummaryPage createJournalPortion(
      String title, String topic, String journal, int firstPage, int lastPage, int numsections) {
    return createJournalPortion(
        title, topic, journal, firstPage, lastPage, numsections, Attachments.get(ATTACH1_FILENAME));
  }

  @SuppressWarnings("nls")
  protected SummaryPage createJournalPortion(
      String title,
      String topic,
      String journal,
      int firstPage,
      int lastPage,
      int numsections,
      URL filename) {
    journal = context.getFullName(journal);
    WizardPageTab wizardPage = new ContributePage(context).load().openWizard(PORTION_COLLECTION);

    CALWizardPage calWizardPage = new CALWizardPage(context, wizardPage);
    calWizardPage.selectJournal(journal);
    calWizardPage.addAuthor("Author1");
    calWizardPage.setPortionTitle(context.getFullName(title));

    calWizardPage.addTopic(topic);
    addSections(calWizardPage, firstPage, lastPage, numsections, filename);
    return calWizardPage.publish();
  }
}
