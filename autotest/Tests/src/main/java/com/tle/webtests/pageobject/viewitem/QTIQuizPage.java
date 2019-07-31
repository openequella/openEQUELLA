package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class QTIQuizPage extends AbstractPage<QTIQuizPage> {

  @FindBy(id = "qpv_startButton")
  private WebElement startTestButton;

  @FindBy(id = "qpv_nextButton")
  private WebElement nextQuestionButton;

  @FindBy(id = "qpv_previousButton")
  private WebElement prevQuestionButton;

  @FindBy(id = "qpv_submitButton")
  private WebElement submitQuizButton;

  @FindBy(id = "qpv_title")
  private WebElement quizTitleLink;

  @FindBy(id = "qpv_viewResultButton")
  private WebElement viewResultsButton;

  @FindBy(id = "qpv_returnLink")
  private WebElement returnLink;

  @FindBy(id = "question-body-container")
  private WebElement ajaxQuestionBody;

  @FindBy(id = "question-title")
  private WebElement ajaxQuestionTitle;

  @FindBy(id = "test-questions-container")
  private WebElement ajaxQuestionList;

  public QTIQuizPage(PageContext context) {
    super(context, By.xpath("//div[@class = 'qti']"));
  }

  public String getCurrentQuestionTitle() {
    return driver.findElement(By.xpath("//div[@id='question-title']/h2")).getText();
  }

  public boolean coverPagePresent() {
    return isPresent(By.xpath("//div[@class = 'summary-box']/h2[text() = 'Test summary']"));
  }

  public int getNumberOfQuestions() {
    return driver.findElements(By.className("question")).size();
  }

  public int getNumberOfSections() {
    return driver.findElements(By.xpath("//h3[contains(text(),'Section')]")).size();
  }

  // TODO: subclass or something
  public QTIQuizPage startQuiz() {
    ExpectedCondition<Boolean> quizStarted =
        ExpectedConditions2.stalenessOrNonPresenceOf(startTestButton);
    startTestButton.click();
    getWaiter().until(quizStarted);
    return new QTIQuizPage(context).get();
  }

  public int coverPageGetNumberOfQuestions() {
    return Integer.parseInt(
        driver
            .findElement(By.xpath("//label[text() = 'Number of questions:']/../../div[2]/div"))
            .getText());
  }

  public int coverPageGetNumberOfSections() {
    return Integer.parseInt(
        driver
            .findElement(By.xpath("//label[text() = 'Number of sections:']/../../div[2]/div"))
            .getText());
  }

  // *************************

  public String getQuestionTitle(int section, int question) {
    return findQuestion(section, question).getText();
  }

  public void nextQuestion() {
    ExpectedCondition<?> titleWaiter = ExpectedConditions2.ajaxUpdate(ajaxQuestionTitle);
    nextQuestionButton.click();
    getWaiter().until(titleWaiter);
  }

  public void prevQuestion() {
    ExpectedCondition<?> titleWaiter = ExpectedConditions2.ajaxUpdate(ajaxQuestionTitle);
    prevQuestionButton.click();
    getWaiter().until(titleWaiter);
  }

  public void selectQuestion(int section, int question) {
    ExpectedCondition<?> bodyWaiter = ExpectedConditions2.ajaxUpdate(ajaxQuestionBody);
    try {
      findQuestion(section, question).click();
    } catch (StaleElementReferenceException e) {
      // question list can be stale (there's ~3 ajax sections on the page)
      selectQuestion(section, question);
    }
    getWaiter().until(bodyWaiter);
  }

  private WebElement findQuestion(int section, int question) {
    waitForElement(ajaxQuestionList);
    return ajaxQuestionList.findElement(
        By.xpath("//ul[@class='questions'][" + section + "]/li[" + question + "]/a"));
  }

  public void answerMultiChoiceQuestion(int section, int question, int answer) {
    selectQuestion(section, question);

    ExpectedCondition<?> ajaxUpdate = ExpectedConditions2.ajaxUpdate(ajaxQuestionBody);
    getMultiChoiceInput(answer).click();
    getWaiter().until(ajaxUpdate);
  }

  public boolean multiChoiceAnswerRemembered(int section, int question, int expectedAnswer) {
    selectQuestion(section, question);
    return getMultiChoiceInput(expectedAnswer).isSelected();
  }

  private WebElement getMultiChoiceInput(int input) {
    return ajaxQuestionBody.findElement(
        By.xpath("//ul[@class='choiceInteraction']/li[" + input + "]/div/input"));
  }

  public void answerInlineChoiceQuestion(int section, int question, String[] answers) {
    selectQuestion(section, question);
    int index = 0;
    for (String selection : answers) {
      WebElement currentSelect = ajaxQuestionBody.findElement(By.id("i" + index));
      ExpectedCondition<?> ajaxUpdate = ExpectedConditions2.ajaxUpdate(ajaxQuestionBody);
      new EquellaSelect(context, currentSelect).selectByVisibleText(selection);
      getWaiter().until(ajaxUpdate);

      index++;
    }
  }

  public boolean inlineChoiceAnswerRemembered(int section, int question, String[] expectedAnswers) {
    waitForElement(ajaxQuestionList);
    selectQuestion(section, question);
    boolean correctAnswers = true;
    int index = 0;
    for (String selection : expectedAnswers) {
      WebElement currentSelect = ajaxQuestionBody.findElement(By.id("i" + index));
      correctAnswers |=
          new EquellaSelect(context, currentSelect).getSelectedText().equals(selection);
      index++;
    }
    return correctAnswers;
  }

  public void submit() {
    submitQuizButton.click();
    ExpectedCondition<WebElement> resultsFound =
        ExpectedConditions2.presenceOfElement(viewResultsButton);
    getWaiter().until(ExpectedConditions.alertIsPresent()).accept();
    getWaiter().until(resultsFound);
    this.get();
  }

  public boolean resultsPagePresent() {
    return getCurrentQuestionTitle().equals("Results");
  }

  public boolean checkSectionScore(int section, float expectedScore) {
    String scoreText =
        ajaxQuestionBody
            .findElement(By.xpath("//div[contains(text(),'Section " + section + " score')]"))
            .getText();
    scoreText = scoreText.substring(scoreText.indexOf(": ") + 2, scoreText.indexOf(" ("));
    return expectedScore == (Float.parseFloat(scoreText));
  }

  public boolean checkTotalScore(float expectedScore) {
    String scoreText =
        ajaxQuestionBody.findElement(By.xpath("//div[contains(text(),'Total score:')]")).getText();
    scoreText = scoreText.substring(scoreText.indexOf(": ") + 2, scoreText.indexOf(" ("));
    return expectedScore == (Float.parseFloat(scoreText));
  }

  public boolean positiveFeedbackPresent(int section, int question) {
    selectQuestion(section, question);
    return isPresent(By.xpath("//span[@class='feedback'][@identifier='OK']"));
  }

  public boolean negativeFeedbackPresent(int section, int question) {
    selectQuestion(section, question);
    return isPresent(By.xpath("//span[@class='feedback'][@identifier!='OK']"));
  }

  public <T extends PageObject> T exitQuiz(WaitingPageObject<T> targetPage) {
    quizTitleLink.click();
    return targetPage.get();
  }

  public void viewResults() {
    viewResultsButton.click();
  }

  public <T extends PageObject> T returnToCourse(WaitingPageObject<T> returnTo) {
    returnLink.click();
    return returnTo.get();
  }
}
