package com.tle.webtests.remotetest.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.GenericAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.GoogleBooksUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.iTunesUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class RemoteAttachmentControlTest extends AbstractCleanupTest {

  private static final String COLLECTION = "Navigation and Attachments";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void googleBooks() {
    String itemName = context.getFullName("Google Books");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    GoogleBooksUniversalControlType books =
        control.addResource(new GoogleBooksUniversalControlType(control));
    books.search("This is a Test").selectBook(1).setDisplayName("This is a Test").save();

    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("This is a Test"));

    wizard = item.adminTab().edit();
    control = wizard.universalControl(2);
    control
        .editResource(new GoogleBooksUniversalControlType(control), "This is a Test")
        .setDisplayName("A Book")
        .save();
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("A Book"));

    itemName = context.getFullName("Google Books 2");
    wizard = initialItem(itemName);

    control = wizard.universalControl(2);
    books = control.addResource(new GoogleBooksUniversalControlType(control));
    books.search("Google").addBooks(1, 2, 3, 4);
    item = wizard.save().publish();
    assertEquals(item.attachments().attachmentCount(), 4);
  }

  /** http://dev.equella.com/issues/7288 */
  @Test
  public void testGoogleBooksNoSelections() {
    logon("AutoTest", "automated");
    String itemName = context.getFullName("GoogleBooks - no selections");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    GoogleBooksUniversalControlType googleBooks =
        control.addResource(new GoogleBooksUniversalControlType(control));
    googleBooks.search("funny cat");
    Assert.assertFalse(googleBooks.canAdd(), "Able to add with no selections");

    UniversalControl close = googleBooks.close();
    close.getPage().cancel(new ContributePage(context));
  }

  @Test
  public void itunes() {
    String itemName = context.getFullName("iTunes");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    iTunesUniversalControlType itunes =
        control.addResource(new iTunesUniversalControlType(control));
    GenericAttachmentEditPage editPage =
        itunes.addTrack(
            "Texas A&M University", "CAMPUS COMMUNITY", "Camtasia Relay Test", "Group 1");
    editPage.setDisplayName("Something").save();
    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("Something"));

    wizard = item.adminTab().edit();
    control = wizard.universalControl(2);
    control
        .editResource(new iTunesUniversalControlType(control), "Something")
        .setDisplayName("A tune")
        .save();

    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists("A tune"));
  }

  /** http://dev.equella.com/issues/7288 */
  @Test
  public void testItunesNoSelections() {
    String itemName = context.getFullName("iTunes");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    iTunesUniversalControlType itunes =
        control.addResource(new iTunesUniversalControlType(control));

    Assert.assertFalse(itunes.canAdd(), "Able to add with no selections");

    UniversalControl close = itunes.close();
    close.getPage().cancel(new ContributePage(context));
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1, itemName);
    return wizard;
  }
}
