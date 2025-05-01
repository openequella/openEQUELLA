package com.tle.webtests.test;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.tle.webtests.framework.HasTestConfig;
import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.StandardDriverFactory;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestUtils;
import com.tle.webtests.pageobject.ClassPrefixedName;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractTest implements HasTestConfig {

  private static final String RANDOM_STRING_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final String KEY_DRIVERPOOL = "DriverPool";
  protected static final String KEY_LISTENEREADDED = "ListenerAdded";
  protected static final String KEY_SETUPLISTENEREADDED = "SetupListenerAdded";
  protected PageContext context;
  protected TestConfig testConfig;

  private final ListMultimap<String, PrefixedName> nameMap = ArrayListMultimap.create();

  public AbstractTest() {
    testConfig = new TestConfig(getClass(), !isInstitutional());

    // scan @Name
    Class<?> myClass = getClass();
    Class<?> c = myClass;
    while (c != Object.class) {
      for (Field field : c.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          Name name = field.getAnnotation(Name.class);
          if (name != null) {
            field.setAccessible(true);
            try {
              PrefixedName pfxName = new ClassPrefixedName(myClass, name.value());
              field.set(null, pfxName);
              nameMap.put(name.group(), pfxName);
            } catch (IllegalArgumentException e) {
              throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
      c = c.getSuperclass();
    }
  }

  protected List<PrefixedName> getNames() {
    return getNames("");
  }

  protected List<PrefixedName> getNames(String group) {
    return nameMap.get(group);
  }

  @BeforeClass(alwaysRun = true)
  public void setupContext(ITestContext testContext) throws IOException {
    File rootFolder = testConfig.getTestFolder();
    String contextUrl;

    if (isInstitutional()) {
      contextUrl = testConfig.getInstitutionUrl();
    } else {
      contextUrl = testConfig.getAdminUrl();
    }
    WebDriver driver = new StandardDriverFactory(testConfig).getDriver(getClass());
    context = new PageContext(driver, testConfig, contextUrl);
    if (!testConfig.isNoInstitution()) {
      context.setIntegUrl(testConfig.getIntegrationUrl(rootFolder.getName()));
    }
    context.setNamePrefix(getNamePrefix());
    try {
      customisePageContext();
      prepareBrowserSession();
    } catch (Throwable t) {
      System.err.println("setupContext failed");
      t.printStackTrace();
    }
  }

  public String prefix() {
    return getClass().getSimpleName();
  }

  protected PageContext newContext(String shortName) {
    String contextUrl = testConfig.getInstitutionUrl(shortName);
    return new PageContext(this.context, contextUrl);
  }

  protected boolean isInstitutional() {
    return true;
  }

  protected void customisePageContext() {
    // nothing;
  }

  protected String getNamePrefix() {
    return null;
  }

  private void dismissAlert(WebDriver currentDriver) {
    try {
      Alert alert = currentDriver.switchTo().alert();
      String alertText = alert.getText();
      alert.dismiss();
      System.err.println("An alert was left open on the previous test: " + alertText);
    } catch (NoAlertPresentException e) {

    }
  }

  private void switchToDefaultWindow(WebDriver currentDriver) {
    try {
      currentDriver.switchTo().defaultContent();
      currentDriver.findElement(By.xpath("*[1]")).isDisplayed();
    } catch (UnhandledAlertException e) {
      dismissAlert(currentDriver);
      currentDriver.switchTo().defaultContent();
    } catch (NoSuchElementException nse) {
      // dont care
    }
  }

  private void clearCookies(WebDriver currentDriver) {
    // http://code.google.com/p/selenium/issues/detail?id=267#c11
    // Can only cookies of current domain
    String url = currentDriver.getCurrentUrl();
    String baseUrl = context.getBaseUrl();

    if (!url.startsWith(baseUrl)) {
      if (isInstitutional()) {
        currentDriver.get(baseUrl + "logon.do?logout=true");
      } else {
        currentDriver.get(baseUrl);
      }
    }

    currentDriver.manage().deleteAllCookies();
  }

  @AfterClass(alwaysRun = true)
  public void finishedClass(ITestContext testContext) throws Exception {
    try {
      if (context == null) {
        return;
      }
      String delValue = testConfig.getProperty("test.deleteitems");
      if (alwaysCleanup() || delValue == null || Boolean.parseBoolean(delValue)) {
        cleanupAfterClass();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    try {
      context.getDriver().quit();
    } catch (Throwable t) {
      t.printStackTrace();
    }

    // If we leave the browsers open on grid they will timeout if they are
    // not used in a certain amount of time
    // if( !Check.isEmpty(gridUrl) )
    // {
    // DriverPool driverPool = getDriverPool(testContext);
    // WebDriver driver = driverPool.getCurrentDriver();
    // if( driver != null )
    // {
    // driverPool.removeForGrid(driver);
    // driver.quit();
    // }
    // }
  }

  protected void cleanupAfterClass() throws Exception {
    // do clean up here
  }

  protected boolean alwaysCleanup() {
    return false;
  }

  protected boolean isTextPresent(String text) {
    return context.getDriver().findElement(By.xpath("//body")).getText().contains(text);
  }

  protected boolean isTextPresentInId(String id, String text) {
    return context.getDriver().findElement(By.id(id)).getText().contains(text);
  }

  protected String getValueInId(String id) {
    return context.getDriver().findElement(By.id(id)).getAttribute("value");
  }

  protected void prepareBrowserSession() {
    // classes can override
  }

  public static void assertListEquals(List<?> actual, List<?> expected) {
    assertListEquals(actual, expected, null);
  }

  public static void assertListEquals(List<?> actual, List<?> expected, String message) {
    if (actual == expected) {
      return;
    }

    if (actual == null || expected == null) {
      if (message != null) {
        throw new AssertionError(message);
      } else {
        throw new AssertionError(
            "Collections not equal: expected: " + expected + " and actual: " + actual);
      }
    }

    Iterator<?> actIt = actual.iterator();
    Iterator<?> expIt = expected.iterator();
    int i = -1;
    while (actIt.hasNext() && expIt.hasNext()) {
      i++;
      Object e = expIt.next();
      Object a = actIt.next();
      String explanation = "Lists differ at element [" + i + "]: " + e + " != " + a;
      String errorMessage = message == null ? explanation : message + ": " + explanation;

      assertEquals(a, e, errorMessage);
    }
    assertEquals(actual.size(), expected.size(), message + ": lists don't have the same size");
  }

  public <T extends PageObject> T goBack(WaitingPageObject<T> targetPage) {
    context.getDriver().navigate().back();
    return targetPage.get();
  }

  public PageContext getContext() {
    return context;
  }

  public TestConfig getTestConfig() {
    return testConfig;
  }

  public void browserInvalid(WebDriver currentDriver) {
    switchToDefaultWindow(currentDriver);
    try {
      clearCookies(currentDriver);
    } catch (UnhandledAlertException uae) {
      dismissAlert(currentDriver);
      clearCookies(currentDriver);
    }
    prepareBrowserSession();
  }

  public void invalidateSession() {}

  // It's a sad day when this is the only way
  public void sleepyTime(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static String randomString(int length) {
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      final int charIndex = (int) (Math.random() * RANDOM_STRING_CHARS.length());
      sb.append(RANDOM_STRING_CHARS.charAt(charIndex));
    }
    return sb.toString();
  }

  /**
   * Force click on a button using JavaScript.
   *
   * @param button WebElement to be clicked.
   */
  public void forceButtonClickWithJS(WebElement button) {
    TestUtils.forceButtonClickWithJS((JavascriptExecutor) context.getDriver(), button);
  }
}
