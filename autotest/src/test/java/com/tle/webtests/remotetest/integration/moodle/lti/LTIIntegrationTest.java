package com.tle.webtests.remotetest.integration.moodle.lti;

import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleAddExternalToolPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleExternalToolConfigPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleExternalToolPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSelectionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.QTIQuizPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LTIIntegrationTest extends AbstractParallelMoodleTest {
  private static final String LTI_LINK_NAME = "Test basic item";
  private final Map<Integer, String> LTI_OAUTH_MAP =
      ImmutableMap.<Integer, String>builder()
          .put(25, "b90fa6ea-15fe-4640-939a-b7341fc5a4bc")
          .put(26, "8aeda259-b311-4c45-9e7e-075a1b041559")
          .put(27, "c9edb02e-4ab8-4ce0-9d34-350c86aaa57b")
          .put(28, "a67e8529-89f5-471d-959f-c0f432e78714")
          .build();

  private static final String EXTERNAL_TOOL_NAME = "EQUELLA";
  private static final String COURSE_NAME = "Test Course 1";

  private static final String QTI_ITEM = "QTI_ITEM";
  private final Map<Integer, String> QTU_OAUTH_MAP =
      ImmutableMap.<Integer, String>builder()
          .put(25, "ea71c193-4251-4121-a0a3-5170b874ba1c")
          .put(26, "304ef014-fa0b-4a61-ae9d-923419b5a677")
          .put(27, "de85d31e-a65f-4b5c-9134-b8fb919973d2")
          .put(28, "1ce5becd-a051-4962-a126-092a684daa84")
          .build();
  private static final String QTI_QUIZ_URL =
      "items/5f63ad95-acc1-42e0-b1d2-81442ea485f9/1/?.vi=qtiTestViewer&attachment.uuid=3b22a99f-90a4-4269-ad3d-f67148394c59";

  public LTIIntegrationTest() {
    setDeleteCredentials("AutoTest", "automated");
  }

  @Test
  public void setupExternalTool() {
    // Add EQUELLA Tool and ensure it is added
    MoodleLoginPage loginPage = new MoodleLoginPage(context).load();
    loginPage.logon("admin", "admin");
    MoodleExternalToolPage extToolPage = new MoodleExternalToolPage(context).load();
    while (extToolPage.hasExternalTool(EXTERNAL_TOOL_NAME)) {
      extToolPage.deleteExternalTool(EXTERNAL_TOOL_NAME);
    }
    final String LTI_OAUTH_ID = "LTI_" + getMoodleVersion();
    extToolPage.addExternalTool(
        EXTERNAL_TOOL_NAME,
        context.getBaseUrl(),
        LTI_OAUTH_ID,
        LTI_OAUTH_MAP.get(getMoodleVersion()),
        true);

    Assert.assertTrue(extToolPage.hasExternalTool(EXTERNAL_TOOL_NAME));
  }

  @Test(dependsOnMethods = "setupExternalTool")
  public void basicLTITest() {
    logout();
    // Add link
    MoodleCoursePage mcp =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    mcp.setEditing(true);

    MoodleAddExternalToolPage aetp = mcp.addExternalTool(2);
    mcp =
        aetp.setupExternalTool(
            LTI_LINK_NAME,
            EXTERNAL_TOOL_NAME,
            context.getBaseUrl() + "items/21ff03e5-ade5-432d-a938-5c59ab0260b0/1/",
            COURSE_NAME);

    // Check for link
    Assert.assertTrue(
        mcp.hasLTIResource(2, LTI_LINK_NAME), "Lti resource not found or too many found");

    // Click link and check for errors
    MoodleResourcePage mrp = mcp.clickLTIResource(2, LTI_LINK_NAME);
    SummaryPage summaryTabPage = mrp.switchToItem(new SummaryPage(context));
    Assert.assertEquals(summaryTabPage.getItemTitle(), "ItemVersion1");
    mrp.returnToMoodle();
    mcp = mrp.backToCoursePage();

    // Edit link input incorrect secret. Save
    MoodleExternalToolPage metp = new MoodleExternalToolPage(context).load();
    MoodleExternalToolConfigPage mtcp = metp.editExternalTool(EXTERNAL_TOOL_NAME);
    mtcp.setSharedSecret("wrong");
    metp = mtcp.save();

    mcp = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    mrp = mcp.clickLTIResource(2, LTI_LINK_NAME);
    ErrorPage ep = new ErrorPage(context);
    mrp.switchToItem(ep);
    Assert.assertEquals(
        ep.getDetail(), "Bad request: Signature is invalid. Please check key and secret");
  }

  @Test(
      dependsOnMethods = {"basicLTITest", "doQTIOverLTITest", "openToolViaEquellaTest"},
      alwaysRun = true)
  public void deleteTool() {
    MoodleLoginPage loginPage = new MoodleLoginPage(context).load();
    MoodleCoursePage mcp = loginPage.logon("admin", "admin").clickCourse(COURSE_NAME);
    mcp.setEditing(true);
    while (mcp.hasLTIResource(2, LTI_LINK_NAME)) {
      mcp.deleteLTIResource(2, LTI_LINK_NAME);
    }
    while (mcp.hasLTIResource(2, QTI_ITEM)) {
      mcp.deleteLTIResource(2, QTI_ITEM);
    }
    while (mcp.hasResource(3, "wikitool")) {
      mcp.deleteResource(3, "wikitool");
    }

    MoodleExternalToolPage etp = new MoodleExternalToolPage(context).load();
    while (etp.hasExternalTool(EXTERNAL_TOOL_NAME)) {
      etp = etp.deleteExternalTool(EXTERNAL_TOOL_NAME);
    }
  }

  @Test
  public void doQTIOverLTITest() throws InterruptedException {
    // add lti item in moodle
    MoodleCoursePage mcp =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    mcp.setEditing(true);
    MoodleAddExternalToolPage aetp = mcp.addExternalTool(2);
    final String QTI_OAUTH_ID = "QTI_" + getMoodleVersion();
    aetp.addKeySecret(QTI_OAUTH_ID, QTU_OAUTH_MAP.get(getMoodleVersion()));
    mcp = aetp.setupExternalTool(QTI_ITEM, null, context.getBaseUrl() + QTI_QUIZ_URL, COURSE_NAME);
    Assert.assertTrue(mcp.hasLTIResource(2, QTI_ITEM));

    // start test - answer some questions
    MoodleResourcePage mrp = mcp.clickLTIResource(2, QTI_ITEM);
    QTIQuizPage quiz = mrp.switchToItem(new QTIQuizPage(context));
    quiz.startQuiz();
    quiz.answerMultiChoiceQuestion(1, 1, 1);
    // String[] inlineAnswers = {"family", "castle", "horse"};
    // quiz.answerInlineChoiceQuestion(1, 5, inlineAnswers);

    // quit test
    mcp = quiz.returnToCourse(mcp);
    mrp = mcp.clickLTIResource(2, QTI_ITEM);
    quiz = mrp.switchToItem(new QTIQuizPage(context));
    // answers remembered?
    assertTrue(quiz.multiChoiceAnswerRemembered(1, 1, 1));
    // assertTrue(quiz.inlineChoiceAnswerRemembered(1, 5, inlineAnswers));
    // TODO: remembering scores/gradebook integration
  }

  @Test
  public void openToolViaEquellaTest() {
    final int WEEK = 3;
    final String LTI_ITEM = "LTI Item";
    final String LTI_ATT = "wikitool";

    MoodleCoursePage mcp =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    mcp.setEditing(true);
    MoodleSelectionPage moodleSelection = mcp.selectEquellaResource(WEEK);
    SelectionSession selectionSession = moodleSelection.equellaSession();
    ItemListPage items = selectionSession.homeExactSearch(LTI_ITEM);
    SummaryPage summary = items.getResultForTitle(LTI_ITEM, 1).viewSummary();
    AttachmentsPage attachments = summary.attachments();
    attachments.selectAttachment("wikitool");
    mcp = summary.finishSelecting(mcp);
    // tool added?
    assertTrue(mcp.hasResource(WEEK, LTI_ATT));
    // open resource
    mcp.clickResource(WEEK, LTI_ATT);
    // TODO: something to check??

  }
}
