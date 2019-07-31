package com.tle.webtests.test.viewing.actions;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("workflow")
public abstract class BaseCloneMove extends AbstractCleanupTest {
  private static final String ATTACHMENT_NAME = "page.html";
  protected static final String ORIGINAL_COLLECTION = "Simple 2 Step"; // $NON-NLS-1$

  public BaseCloneMove() {
    super();
  }

  @SuppressWarnings("nls")
  protected void verifyAttachment(SummaryPage summary, boolean exists) {
    if (!exists) {
      boolean noAtt =
          !summary.hasAttachmentsSection()
              || !summary.attachments().attachmentExists(ATTACHMENT_NAME);
      assertTrue(noAtt);
    } else {
      AttachmentsPage attachments = summary.attachments();
      assertTrue(
          attachments
              .viewAttachment(ATTACHMENT_NAME, new VerifyableAttachment(context))
              .isVerified());
    }
  }

  protected ItemAdminPage setupAdminPage() {
    ItemAdminPage itemAdminPage = new ItemAdminPage(context).load();
    itemAdminPage.all().setSort("datemodified");
    return itemAdminPage;
  }

  @SuppressWarnings("nls")
  protected SummaryPage createItem(String title) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 2 Step");
    wizard.editbox(1, title);
    wizard.addSingleFile(3, Attachments.get(ATTACHMENT_NAME));
    return wizard.save().submit();
  }
}
