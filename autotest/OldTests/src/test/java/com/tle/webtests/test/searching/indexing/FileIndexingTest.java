package com.tle.webtests.test.searching.indexing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.settings.MimeEditorPage;
import com.tle.webtests.pageobject.settings.MimeSearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.List;
import org.testng.annotations.Test;

// TODO: refactor with a data source
@TestInstitution("fiveo")
public class FileIndexingTest extends AbstractCleanupTest {
  // DTEC 14899
  @Test
  public void pptIndexTest() {
    final String ITEM_NAME = context.getFullName("Powerpoint Indexing");
    final List<String> SEARCH_TERMS = Lists.newArrayList("funky", "Slide two", "numero uno");

    // Logon
    logon("AutoTest", "automated");

    // Set PPT mimetype freetext extractor
    SettingsPage settingsPage = new SettingsPage(context).load();
    MimeSearchPage mimePage = settingsPage.mimeSettings();
    mimePage.search("pptx");
    MimeEditorPage editMimePage = mimePage.editMime(1);
    editMimePage.selectTextExtractor("MS PowerPoint PPTX text extractor", true);
    editMimePage.save();

    // Contribute powerpoint
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Basic Items");
    wizardPage.editbox(1, ITEM_NAME);
    wizardPage.addSingleFile(3, "pptforindexing.pptx");
    AttachmentsPage attachmentsSection = wizardPage.save().publish().attachments();

    // Assert attachment
    assertTrue(attachmentsSection.attachmentExists("pptforindexing.pptx"));

    // Search for text within the presentation
    SearchPage searchPage = new SearchPage(context).load();

    // Ensure powerpoint text indexed
    for (String term : SEARCH_TERMS) {
      searchPage.search(term);
      assertEquals(searchPage.results().getResult(1).getTitle(), ITEM_NAME);
    }
  }

  @Test
  public void docxIndexTest() {
    final String ITEM_NAME = context.getFullName("DOCX Indexing");
    final List<String> SEARCH_TERMS =
        Lists.newArrayList("Lego", "somemadeupword", "whichyouwouldonlyfindhere");

    // Logon
    logon("AutoTest", "automated");

    // Set DOCX mimetype freetext extractor
    SettingsPage settingsPage = new SettingsPage(context).load();
    MimeSearchPage mimePage = settingsPage.mimeSettings();
    mimePage.search("docx");
    MimeEditorPage editMimePage = mimePage.editMime(1);
    editMimePage.selectTextExtractor("MS Word DOCX text extractor", true);
    editMimePage.save();

    // Contribute doc
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Basic Items");
    wizardPage.editbox(1, ITEM_NAME);
    wizardPage.addSingleFile(3, "docxforindexing.docx");
    AttachmentsPage attachmentsSection = wizardPage.save().publish().attachments();

    // Assert attachment
    assertTrue(attachmentsSection.attachmentExists("docxforindexing.docx"));

    // Search for text within the doc
    SearchPage searchPage = new SearchPage(context).load();

    // Ensure docx text indexed
    for (String term : SEARCH_TERMS) {
      searchPage.search(term);
      assertEquals(searchPage.results().getResult(1).getTitle(), ITEM_NAME);
    }
  }
}
