package com.tle.webtests.framework;

import static org.testng.internal.Utils.longStackTrace;
import static org.testng.internal.Utils.shortStackTrace;

import com.tle.common.Check;
import java.io.File;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.*;
import org.testng.*;

public class ScreenshotListener implements ITestListener {

  @Override
  public void onStart(ITestContext context) {}

  @Override
  public void onTestStart(ITestResult result) {}

  @Override
  public void onTestSuccess(ITestResult result) {}

  @Override
  public void onTestFailure(ITestResult testResult) {
    Reporter.setCurrentTestResult(testResult);
    try {
      Throwable throwable = testResult.getThrowable();
      throwable.printStackTrace();
      Reporter.log(
          "<h3>" + testResult.getTestClass().getName() + "." + testResult.getName() + "</h3>");
      String testName = testResult.getTestName();
      if (!Check.isEmpty(testName)) {
        Reporter.log("<h2>" + testName + "</h2>");
      }

      Object[] parameters = testResult.getParameters();
      if (parameters != null && parameters.length > 0) {
        StringBuilder sb = new StringBuilder();
        sb.append("<br>Parameters: ");
        for (int j = 0; j < parameters.length; j++) {
          if (j > 0) {
            sb.append(", ");
          }
          sb.append(parameters[j] == null ? "null" : parameters[j].toString());
        }
        Reporter.log(sb.toString());
      }

      Object testInstance = testResult.getMethod().getInstance();
      if (testInstance instanceof HasTestConfig) {
        HasTestConfig test = (HasTestConfig) testInstance;
        TestInstitution institution = test.getClass().getAnnotation(TestInstitution.class);
        if (institution != null) {
          Reporter.log("<strong>Institution:</strong> " + institution.value());
        }
        String alertText = null;
        TestConfig config = test.getTestConfig();
        WebDriver driver = test.getContext().getCurrentDriver();
        if (driver != null) {
          if (throwable instanceof UnhandledAlertException) {
            alertText = ((UnhandledAlertException) throwable).getAlertText();

            // Cant take a screenshot if there is still an alert
            try {
              // Have to get the real alert to dismiss it
              Alert realAlert = driver.switchTo().alert();
              alertText = realAlert.getText();
              realAlert.dismiss();
            } catch (NoAlertPresentException nape) {
              // nothing
            }
          }

          String screenshot =
              takeScreenshot(
                  driver, config.getScreenshotFolder(), testResult, config.isChromeDriverSet());
          String error = captureErrorPage(driver, testResult);
          String jsError = captureJavascriptError(driver, testResult);
          test.invalidateSession();

          Reporter.log("<strong>Failed at url:</strong> " + driver.getCurrentUrl());
          if (!Check.isEmpty(error)) {
            Reporter.log("<strong>Captured error: </strong><pre>" + error + "</pre>");
          }
          if (!Check.isEmpty(screenshot)) {
            Reporter.log(
                String.format(
                    "<strong>Screenshot:</strong> <a href='%s/%s'>%s</a>",
                    "screenshots", screenshot, screenshot));
          }

          if (!Check.isEmpty(jsError)) {
            Reporter.log("<strong>JS error: </strong><pre>" + jsError + "</pre>");
          }

          Reporter.log(
              "<strong>Message: </strong>" + StringEscapeUtils.escapeHtml4(throwable.getMessage()));

          if (alertText != null) {
            System.err.println("Alert text:" + alertText);
            Reporter.log(
                "<strong>Alert Text: </strong>" + StringEscapeUtils.escapeHtml4(alertText));
          }

          Throwable tw = testResult.getThrowable();
          String stackTrace = "";
          String fullStackTrace = "";

          String id = "stack-trace" + testResult.hashCode();
          StringBuffer sb = new StringBuffer();

          if (null != tw) {
            String[] stackTraces =
                new String[] {shortStackTrace(tw, true), longStackTrace(tw, true)};
            fullStackTrace = stackTraces[1];
            stackTrace = "<div><pre>" + stackTraces[0] + "</pre></div>";

            sb.append(stackTrace);
            // JavaScript link
            sb.append(
                    "<a href='#' onClick=\"document.getElementById('"
                        + id
                        + "').style.display='block';\">Show full stacktrace</a>")
                .append("<div class='stack-trace' id='" + id + "' style='display: none'>")
                .append("<pre>" + fullStackTrace + "</pre>")
                .append("</div>");
            Reporter.log(sb.toString());
          }
        }
      }
    } catch (Throwable t) {
      System.err.println("Error capturing screenshot");
      t.printStackTrace();
    }
  }

  @Override
  public void onTestSkipped(ITestResult result) {}

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

  @Override
  public void onFinish(ITestContext context) {}

  private String takeScreenshot(
      WebDriver driver, File screenshotFolder, ITestResult testResult, boolean chrome) {
    ITestNGMethod method = testResult.getMethod();
    String filename = testResult.getTestClass().getName() + "_" + method.getMethodName();
    return ScreenshotTaker.takeScreenshot(driver, screenshotFolder, filename, chrome);
  }

  private String captureErrorPage(WebDriver driver, ITestResult testResult) {
    WebElement errorDiv = null;
    try {
      errorDiv =
          driver.findElement(
              By.xpath("//div[contains(@class,'area') and contains(@class,'error')]"));

    } catch (NotFoundException e1) {
      try {
        errorDiv = driver.findElement(By.xpath("//p[contains(@class,'warning')][2]"));

      } catch (NotFoundException e2) {

      }
    }

    if (errorDiv != null) {
      EquellaErrorPageException exception =
          new EquellaErrorPageException(errorDiv.getText(), testResult.getThrowable());
      testResult.setThrowable(exception);
      return errorDiv.getText();
    }
    return null;
  }

  private String captureJavascriptError(WebDriver driver, ITestResult testResult) {
    try {
      driver.findElement(By.xpath("//body[@jserror-msg]"));

    } catch (NotFoundException e1) {
      return null;
    }

    WebElement body = driver.findElement(By.tagName("body"));
    String error = body.getAttribute("jserror-msg");
    error += " - " + body.getAttribute("jserror-url");
    error += ":" + body.getAttribute("jserror-line");
    return error;
  }
}
