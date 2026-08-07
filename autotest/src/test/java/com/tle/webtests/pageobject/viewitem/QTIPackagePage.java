package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class QTIPackagePage extends AbstractPage<QTIPackagePage> {
  public QTIPackagePage(PageContext context) {
    super(context, By.id("question_1"));
  }

  public String getQuizTitle() {
    return driver.findElement(By.xpath("//div[contains(@class,'qti')]/div/h2")).getText();
  }

  public String getQuestionTitle(int i) {
    return driver.findElement(By.id("question_" + i)).getText();
  }

  public String getQuestion(int i) {
    driver.findElement(By.id("question_" + i)).click();
    return driver.findElement(By.id("answer_" + i)).getText();
  }
}
