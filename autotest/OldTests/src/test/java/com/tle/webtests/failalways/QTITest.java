package com.tle.webtests.failalways;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.QTIPackagePage;
import com.tle.webtests.pageobject.viewitem.QTIQuizPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.PackageAttachmentEditPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class QTITest extends AbstractCleanupAutoTest {
  static final String OAUTH_ID = "LTI_QUIZ";

  @Test
  public void QTIPackageTest() {
    String q1 =
        "Question 1 - Welcome\n"
            + "This is a brief introduction to Respondus. As you can see, this is not a wizard or a"
            + " typical user manual. Instead, this is an \"assessment\" in which each \"question\""
            + " introduces a feature that is available in Respondus. Let's go to the next"
            + " question.\n"
            + "Single Choice\n"
            + "The user must select one of the following options\n"
            + "Continue.\n"
            + "Go to www.respondus.com\n"
            + "Answers\n"
            + "Continue. : 1.0\n"
            + "Go to www.respondus.com : 0.0";

    String q15 =
        "Question 15 - Matching\n"
            + "The Matching question type requires students to match items from two columns or"
            + " lists.\n"
            + "\n"
            + "Question: Select the country in which the following cities are located.\n"
            + "Matching\n"
            + "The user must match the list on the left with an answer from the right\n"
            + "London\n"
            + "Paris\n"
            + "New York\n"
            + "Toronto\n"
            + "Sydney\n"
            + "Great Britain\n"
            + "France\n"
            + "United States\n"
            + "Canada\n"
            + "Australia\n"
            + "Answers\n"
            + "Great Britain : 1.0\n"
            + "France : 1.0\n"
            + "United States : 1.0\n"
            + "Canada : 1.0\n"
            + "Australia : 1.0";

    String q16 =
        "Question 16 - Many Answers\n"
            + "The \"Multiple Response\" question format is similar to \"Multiple Choice\" except"
            + " that more than one answer can be (and should be) selected by the student.\n"
            + "\n"
            + "Question: Which of the following are fruits? (select all that apply)\n"
            + "Multiple Choice\n"
            + "The user can select multiple answers\n"
            + "Apples\n"
            + "Pears\n"
            + "Spinach\n"
            + "Bananas\n"
            + "Answers\n"
            + "Apples : 33.33\n"
            + "Pears : 33.33\n"
            + "Spinach : -100.0\n"
            + "Bananas : 33.33";

    String itemName = context.getFullName("a qti package");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, itemName);

    UniversalControl control = wizard.universalControl(2);
    PackageAttachmentEditPage pkgEditor =
        control
            .addResource(new FileUniversalControlType(control))
            .uploadPackageOption(Attachments.get("qti.zip"));
    assertEquals(pkgEditor.getName(), "RespondusIntroduction (Respondus IMS QTI export)");
    pkgEditor.showStructure().save();

    SummaryPage item = wizard.save().publish();

    AttachmentsPage attachments = item.attachments();
    assertTrue(attachments.folderExists("default"));

    QTIPackagePage qti = attachments.viewAttachment("Exam 1", new QTIPackagePage(context));

    assertEquals(qti.getQuizTitle(), "RespondusIntroduction");
    assertEquals(qti.getQuestionTitle(1), "Question 1 - Welcome");
    assertEquals(qti.getQuestion(1).replace(" ", ""), q1.replace(" ", ""));

    assertEquals(qti.getQuestionTitle(15), "Question 15 - Matching");
    assertEquals(qti.getQuestion(15).replace(" ", ""), q15.replace(" ", ""));

    assertEquals(qti.getQuestionTitle(16), "Question 16 - Many Answers");
    assertEquals(qti.getQuestion(16).replace(" ", ""), q16.replace(" ", ""));

    assertEquals(qti.getQuestionTitle(18), "Question 18 - The End");
  }

  /** http://dtec.equella.com/DTEC/test/editTest.aspx?testId=21781 */
  @Test
  public void QTIQuizTest() {
    // contribute test
    String itemName = context.getFullName("qti player item");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, itemName);

    UniversalControl control = wizard.universalControl(2);
    control
        .addResource(new FileUniversalControlType(control))
        .uploadQTITestOption(Attachments.get("repaired bbq.zip"))
        .save();

    // open attachment
    SummaryPage item = wizard.save().publish();
    QTIQuizPage quiz =
        item.attachments().viewAttachment("BBQs test package", new QTIQuizPage(context));
    int numQuestions = quiz.coverPageGetNumberOfQuestions();
    int numSections = quiz.coverPageGetNumberOfSections();
    assertTrue(quiz.coverPagePresent());
    // start quiz & check number of questions/sections adds up
    quiz = quiz.startQuiz();
    assertEquals(numQuestions, quiz.getNumberOfQuestions());
    assertEquals(numSections, quiz.getNumberOfSections());

    // prev/next buttons
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(1, 1));
    quiz.nextQuestion();
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(1, 2));
    quiz.prevQuestion();
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(1, 1));
    quiz.prevQuestion();
    // last question - test looping
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(2, 11));
    quiz.nextQuestion();
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(1, 1));
    quiz.prevQuestion();
    assertEquals(quiz.getCurrentQuestionTitle(), quiz.getQuestionTitle(2, 11));
    // go through every question for coverage
    for (int x = 0; x < quiz.getNumberOfQuestions(); x++) {
      quiz.nextQuestion();
    }

    // answer some questions
    quiz.answerMultiChoiceQuestion(1, 1, 1);

    String[] inlineAnswers = {"family", "castle", "horse"};
    quiz.answerInlineChoiceQuestion(1, 5, inlineAnswers);

    // answers remembered?
    assertTrue(quiz.multiChoiceAnswerRemembered(1, 1, 1));
    assertTrue(quiz.inlineChoiceAnswerRemembered(1, 5, inlineAnswers));

    // submit test
    quiz.submit();
    assertTrue(quiz.resultsPagePresent());

    // scores ok?
    assertTrue(quiz.checkSectionScore(1, 3));
    assertTrue(quiz.checkSectionScore(2, 0));
    assertTrue(quiz.checkTotalScore(3));

    // feedback accurate?
    assertTrue(quiz.positiveFeedbackPresent(1, 5));
    assertFalse(quiz.positiveFeedbackPresent(2, 5));
    assertTrue(quiz.negativeFeedbackPresent(1, 1));
    assertFalse(quiz.negativeFeedbackPresent(2, 1));

    // exit test -> return
    item = quiz.exitQuiz(item);
    quiz = item.attachments().viewAttachment("BBQs test package", quiz);

    // answers & score remembered?
    quiz.viewResults();
    assertTrue(quiz.resultsPagePresent());
    assertTrue(quiz.checkTotalScore(3));
    assertTrue(quiz.positiveFeedbackPresent(1, 5));
  }
}
