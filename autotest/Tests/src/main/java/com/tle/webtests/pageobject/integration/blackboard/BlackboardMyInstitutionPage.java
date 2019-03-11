package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class BlackboardMyInstitutionPage extends AbstractPage<BlackboardMyInstitutionPage> {

  public BlackboardMyInstitutionPage(PageContext context) {
    super(
        context,
        By.xpath(
            "id('paneTabs')/li[contains(@class, 'active')]/a[contains(text(), 'My Institution')]"));
  }

  public BlackboardCoursePage clickCourse(String courseName) {
    waitForElement(By.xpath("//ul[contains(@class,'courseListing')]"));
    return new MyCoursesPage(context).get().clickCourse(courseName);
  }

  public List<String> listCourses() {
    return new MyCoursesPage(context).get().getCourseNames();
  }

  public static class MyCoursesPage extends AbstractPage<MyCoursesPage> {
    public MyCoursesPage(PageContext context) {
      super(context, By.xpath("//ul[contains(@class,'courseListing')]"));
    }

    public List<String> getCourseNames() {
      ArrayList<String> courses = new ArrayList<String>();
      List<WebElement> eles =
          driver.findElements(By.xpath("//ul[contains(@class,'courseListing')]/li/a"));
      for (WebElement ele : eles) {
        courses.add(ele.getText().trim());
      }
      return courses;
    }

    public BlackboardCoursePage clickCourse(String courseName) {
      driver.findElement(By.xpath("//a[text()=" + quoteXPath(courseName) + "]")).click();
      return new BlackboardCoursePage(context, courseName).get();
    }
  }
}
