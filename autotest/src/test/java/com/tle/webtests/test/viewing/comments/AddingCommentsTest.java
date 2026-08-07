package com.tle.webtests.test.viewing.comments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.CommentsSection;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class AddingCommentsTest extends AbstractCleanupTest {
  private static final String COMMENT_HTML =
      "<b>This is in bold</b><br/><h1>This is a heading</h1>"
          + "<script>alert('this should not show in a pop-#up message box!');</script>";
  private static final String COMMENT_THREE = "this is a three star comment";
  private static final String COMMENT_TWO = "this is a two star comment";
  private static final String COMMENT_ONE = "this is a one star comment";

  public AddingCommentsTest() {
    setDeleteCredentials("admin", "``````");
  }

  /** DTEC-14406, DTEC-14541 and DTEC-14686 */
  @Test
  public void addthecomments() {
    logon("admin", "``````");
    // Log in and contribute an item.
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Basic collection for searching");
    String itemFullName = context.getFullName("item");
    wizard.editbox(1, itemFullName);
    CommentsSection commentsTab = wizard.save().publish().commentsSection();
    // Add some comments to it and check that they've added correctly.
    commentsTab.addComment("", 0);
    assertFalse(commentsTab.hasComments());

    // Enter some text
    commentsTab.addComment(COMMENT_ONE, 1);
    assertEquals(commentsTab.ratingForComment(COMMENT_ONE), 1);

    commentsTab.addComment(COMMENT_TWO, 2);
    assertEquals(commentsTab.ratingForComment(COMMENT_TWO), 2);
    assertEquals(commentsTab.getCommentOwner(COMMENT_TWO), "ad min [admin]");

    // Try adding one anonymously and check that the user name credited with
    // the comment is "Anonymous"
    commentsTab.addAnonymousComment(COMMENT_THREE, 3);
    assertTrue(commentsTab.isAnonymousComment(COMMENT_THREE));

    // Check that html is escaped
    commentsTab.addComment(COMMENT_HTML, 1);
    assertTrue(commentsTab.containsComment(COMMENT_HTML));

    // Ensure that we can't add a comment with no rating or text
    assertFalse(commentsTab.isAddButtonClickable());

    // Add one without any text, and just a star rating.
    commentsTab.addComment("", 5);
    assertEquals(commentsTab.ratingForComment(""), 5);

    commentsTab.deleteComment("");
    assertFalse(commentsTab.containsComment(""));

    logout();
  }

  /**
   * DTEC-14544. Tests the ADD/VIEW/DELETE_COMMENT privileges. Works by having metadata based ACLs
   * that revoke access based on the item name.
   */
  @Test
  public void checkPrivileges() {
    logon("admin", "``````");

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Basic collection for searching");
    wizard.editbox(1, context.getFullName("NO-VIEW-COMMENT"));
    SummaryPage view = wizard.save().publish();
    CommentsSection comments = view.commentsSection();

    assertFalse(comments.hasComments());
    // True for "doGet" here since we can't see the comment to wait for the
    // loading of!
    comments.addComment(COMMENT_ONE, 1, true);
    assertFalse(comments.hasComments());

    wizard = view.edit();
    wizard.editbox(1, context.getFullName("NO-ADD-COMMENT"));
    view = wizard.saveNoConfirm();
    comments = view.commentsSection();

    assertTrue(comments.isAddingCommentsUnavailable());
    assertTrue(comments.containsComment(COMMENT_ONE));

    wizard = view.edit();
    wizard.editbox(1, context.getFullName("NO-DELETE-COMMENT"));
    view = wizard.saveNoConfirm();
    comments = view.commentsSection();

    assertTrue(comments.containsComment(COMMENT_ONE));
    assertFalse(comments.canDeleteComment(COMMENT_ONE));

    wizard = view.edit();
    wizard.editbox(1, context.getFullName("comment test"));
    view = wizard.saveNoConfirm();
    comments = view.commentsSection();

    comments.deleteComment(COMMENT_ONE);
    assertFalse(comments.hasComments());

    logout();
  }
}
