package com.tle.webtests.test.workflow;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.tasklist.ModerationCommentsPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.ModerationHistoryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

/*
 * This tests workflow comments
 */
@TestInstitution("workflow")
public class MessageToModeratorTest extends AbstractCleanupTest {

  @Test
  public void messageToModeratorShortCommentTest() {
    String fairlyShortComment1 = "Fairly short comment";
    String fairlyShortComment2 = "Fairly short comment again";
    String fairlyShortComment3 = "Fairly short comment returns";

    assertTrue(
        messageToModeratorBasicTest(fairlyShortComment1, fairlyShortComment2, fairlyShortComment3));
  }

  @Test
  public void messageToModeratorLongCommentTest() {
    String enormousComment1 =
        "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt"
            + " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud"
            + " exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute"
            + " irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla"
            + " pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia"
            + " deserunt mollit anim id est laborum.";

    String enormousComment2 =
        "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt"
            + " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud"
            + " exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute"
            + " irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla"
            + " pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia"
            + " deserunt mollit anim id est laborum. Number 2.";

    String enormousComment3 =
        "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt"
            + " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud"
            + " exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute"
            + " irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla"
            + " pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia"
            + " deserunt mollit anim id est laborum. Number 3.";

    assertTrue(messageToModeratorBasicTest(enormousComment1, enormousComment2, enormousComment3));
  }

  @Test
  public void messageToModeratorBlankCommentTest() {
    assertTrue(messageToModeratorBasicTest("", "", ""));
  }

  public boolean messageToModeratorBasicTest(
      String moderationComment, String approvalComment, String rejectionComment) {

    // Submit item with comment
    String itemFullName = context.getFullName("item");
    String expectedFirst = "Commented on at task Start (Show comment)";
    String expectedApproval = "Accepted for task Step 1 (Show comment)";
    String expectedRejection = "Rejected at task Step 2 back to task Step 1 (Show comment)";

    logon("admin", "``````");

    WizardPageTab wizard = new ContributePage(context).load().openWizard("Simple 3 Step");
    wizard.editbox(1, itemFullName);
    ConfirmationDialog confirm = wizard.save();
    confirm.cancel(wizard);
    confirm = wizard.save();
    confirm.addModerationComment(moderationComment);
    confirm.submit();

    assertTrue(checkComments(moderationComment, expectedFirst));

    // Approve and re-check
    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
    mv.acceptToMessagePage().cancel();
    mv.acceptToMessagePage().acceptWithMessage(approvalComment);

    assertTrue(checkComments(approvalComment, expectedApproval));

    if (rejectionComment == null || rejectionComment.isEmpty()) return true;

    taskList = new TaskListPage(context).load();
    mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
    mv.reject().cancel();
    mv.reject().rejectWithMessage(rejectionComment, null);

    checkComments(rejectionComment, expectedRejection);

    taskList = new TaskListPage(context).load();
    mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
    ModerationCommentsPage mcp = mv.moderationComments();

    // Check colours (div classes)
    assertEquals(mcp.getCommentClass(moderationComment).toLowerCase(), "modcomment ");
    assertEquals(mcp.getCommentClass(approvalComment).toLowerCase(), "modcomment approval");
    assertEquals(mcp.getCommentClass(rejectionComment).toLowerCase(), "modcomment rejection");

    logout();
    return true;
  }

  public boolean checkComments(String comment, String expectedMessage) {
    boolean blank = (comment == null || comment.isEmpty());
    String itemFullName = context.getFullName("item");

    ItemAdminPage filterListPage = new ItemAdminPage(context);
    filterListPage.load();
    ModerationHistoryPage history = filterListPage.viewItem(itemFullName).history();
    history.setShowAllDetails(true);
    String s = history.eventAtIndex(1);
    if (blank) {
      assertNotEquals(s, expectedMessage, "Unexpected message");
    } else {
      assertEquals(s, expectedMessage, "Unexpected message");
      assertEquals(history.commentAtIndex(1), comment, "Unexpected moderation comment content");
    }

    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(itemFullName).moderate(itemFullName);
    ModerationCommentsPage mcp = mv.moderationComments();
    if (blank) {
      assertFalse(mcp.containsComment(comment), "Moderation comments contain " + comment);
    } else {
      assertTrue(mcp.containsComment(comment), "Moderation comments do not contain " + comment);
    }

    return true;
  }
}
