package com.tle.webtests.pageobject;

import com.tle.common.NameValue;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.factory.RefreshingFieldDecorator;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class AbstractPage<T extends PageObject>
    implements PageObject, WaitingPageObject<T> {

  protected PageContext context;
  protected WebDriver driver;
  protected SearchContext relativeTo;
  protected By loadedBy;
  protected WebDriverWait waiter;
  protected WebElement loadedElement;

  protected boolean mustBeVisible = true;
  private long refreshTime = -1;

  public AbstractPage(AbstractPage<?> parent) {
    this(parent.getContext());
  }

  public AbstractPage(
      PageContext context, SearchContext relativeTo, By loadedBy, WebDriverWait waiter) {
    this.context = context;
    this.driver = context.getDriver();
    this.loadedBy = loadedBy;
    this.relativeTo = relativeTo;
    this.waiter = waiter;
    initElements();
    loadedElement = null;
  }

  /** @param timeoutSeconds -1 for default timeout */
  public AbstractPage(PageContext context, By loadedBy, int timeoutSeconds) {
    this(context, context.getDriver(), loadedBy, timeoutSeconds);
  }

  public AbstractPage(PageContext context, SearchContext searchContext, By loadedBy) {
    this(context, searchContext, loadedBy, -1);
  }

  /** @param timeoutSeconds -1 for default timeout */
  public AbstractPage(
      PageContext context, SearchContext searchContext, By loadedBy, int timeoutSeconds) {
    this(
        context,
        searchContext,
        loadedBy,
        new WebDriverWait(
            context.getDriver(),
            timeoutSeconds == -1
                ? context.getTestConfig().getStandardTimeout()
                : Duration.ofSeconds(timeoutSeconds),
            Duration.ofMillis(50)));
  }

  public AbstractPage(PageContext context, By loadedBy) {
    this(context, loadedBy, -1);
  }

  public AbstractPage(PageContext context) {
    this(context, null, -1);
  }

  public AbstractPage(PageContext context, WebDriverWait waiter) {
    this(context, context.getDriver(), null, waiter);
  }

  protected void initElements() {
    PageFactory.initElements(new RefreshingFieldDecorator(this), this);
    afterInitElements();
  }

  protected void afterInitElements() {
    // we have proxies now
  }

  @Override
  public SearchContext getSearchContext() {
    return relativeTo;
  }

  @Override
  public PageContext getContext() {
    return context;
  }

  @Override
  public WebDriverWait getWaiter() {
    return waiter;
  }

  public static String quoteXPath(int input) {
    return quoteXPath(Integer.toString(input));
  }

  public static String quoteXPath(String input) {
    final String txt = input;
    if (txt.indexOf("'") > -1 && txt.indexOf("\"") > -1) {
      return "concat('" + txt.replace("'", "', \"'\", '") + "')";
    } else if (txt.indexOf("\"") > -1) {
      return "'" + txt + "'";
    } else {
      return "\"" + txt + "\"";
    }
  }

  protected static String normaliseSpace(String text) {
    return text.replaceAll("\\s+", " ").trim();
  }

  public boolean isLoaded() {
    try {
      checkLoaded();
      return true;
    } catch (NotFoundException error) {
      return false;
    }
  }

  @Override
  public void checkLoaded() throws NotFoundException {
    checkLoadedElement();
  }

  public boolean usingNewUI() {
    return isNewUI();
  }

  protected void checkLoadedElement() {
    loadedElement = findLoadedElement();
    boolean displayed = loadedElement.isDisplayed();
    if (mustBeVisible && !displayed) {
      throw new NotFoundException("Found " + loadedElement + " but not visible");
    }
  }

  protected WebElement findLoadedElement() {
    if (loadedBy != null) {
      return relativeTo.findElement(loadedBy);
    } else {
      throw new RuntimeException("You must supply an overridden checkLoadedElement() method");
    }
  }

  protected void ensureVisible(WebElement... elements) {
    for (WebElement webElement : elements) {
      try {
        if (!webElement.isDisplayed()) {
          throw new NotFoundException("Found " + webElement + " but not visible");
        }
      } catch (StaleElementReferenceException se) {
        throw new NotFoundException("Stale element found:" + webElement, se);
      }
    }
  }

  public T load() {
    loadUrl();
    return get();
  }

  protected void loadUrl() {
    throw new Error("This page object does not support loading");
  }

  public WaitingPageObject<T> acceptAlert() {
    if (context.getTestConfig().isAlertSupported()) {
      return ExpectWaiter.waiter(ExpectedConditions2.acceptAlert(), this);
    } else return this;
  }

  protected void acceptConfirmation(String alertText) {
    if (context.getTestConfig().isAlertSupported()) {
      Alert alert = getWaiter().until(ExpectedConditions.alertIsPresent());
      String text = alert.getText();
      alert.accept();
      if (!alertText.equals(text)) {
        throw new Error(
            "Alert text is wrong, expecting '" + alertText + "' but was '" + text + "'.");
      }
    }
  }

  protected void acceptConfirmation() {
    if (context.getTestConfig().isAlertSupported()) {
      getWaiter().until(ExpectedConditions.alertIsPresent()).accept();
    }
  }

  protected void cancelConfirmation() {
    if (context.getTestConfig().isAlertSupported()) {
      getWaiter().until(ExpectedConditions.alertIsPresent()).dismiss();
    }
  }

  public static String getPathFromUrl(URL file) {
    try {
      return Paths.get(file.toURI()).toAbsolutePath().toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected static WebElement elementIfPresent(SearchContext searchContext, By by) {
    try {
      return searchContext.findElement(by);
    } catch (NotFoundException nfe) {
      return null;
    }
  }

  protected boolean isPresent(By by) {
    return isPresent(driver, by);
  }

  protected static boolean isPresent(SearchContext context, By by) {
    return elementIfPresent(context, by) != null;
  }

  protected static WebElement find(SearchContext context, By... by) {
    for (By b : by) {
      context = context.findElement(b);
    }
    return (WebElement) context;
  }

  protected boolean isPresent(WebElement ele) {
    try {
      // Call this to trigger freshness check on ele
      ele.isDisplayed();
      return true;
    } catch (NoSuchElementException e) {
      return false;
    } catch (StaleElementReferenceException e) {
      return ele.isDisplayed();
    }
  }

  public boolean isVisible() {
    return findLoadedElement().isDisplayed();
  }

  public boolean isVisible(By by) {
    return isVisible(driver, by);
  }

  protected static boolean isVisible(SearchContext context, By by) {
    try {
      WebElement findElement = context.findElement(by);
      return findElement.isDisplayed();
    } catch (NotFoundException nfe) {
      return false;
    }
  }

  protected boolean isVisible(WebElement ele) {
    return isVisible(driver, ele);
  }

  protected static boolean isVisible(SearchContext context, WebElement ele) {
    try {
      return ele.isDisplayed();
    } catch (NotFoundException nfe) {
      return false;
    }
  }

  protected void get(String url, Object... params) {
    List<NameValue> nvs = new ArrayList<NameValue>();
    for (int i = 0; i < params.length; i++) {
      String strName = params[i++].toString();
      Collection<?> values = Collections.emptyList();

      Object obj = params[i];
      if (obj != null) {
        if (obj instanceof Object[]) {
          values = Arrays.asList((Object[]) obj);
        } else if (obj instanceof Collection<?>) {
          values = (Collection<?>) obj;
        } else {
          values = Collections.singleton(obj);
        }
      }

      for (Object val : values) {
        if (val != null) {
          String element = val.toString();
          nvs.add(new NameValue(strName, element));
        }
      }
    }
    driver.get(context.getBaseUrl() + url + '?' + getParameterString(nvs));
  }

  public static String getParameterString(List<NameValue> nvs) {
    StringBuilder parameters = new StringBuilder();

    boolean first = true;
    for (NameValue nv : nvs) {
      if (!first) {
        parameters.append("&"); // $NON-NLS-1$
      } else {
        first = false;
      }

      try {
        String encName = URLEncoder.encode(nv.getName(), "UTF-8");
        String encValue = URLEncoder.encode(nv.getValue(), "UTF-8");
        parameters.append(encName);
        parameters.append('=');
        parameters.append(encValue);
      } catch (UnsupportedEncodingException ex) {
        // This should never happen.... ever.
        // We can ensure that CHARSET_ENCODING will be supported.
        throw new RuntimeException("Problem encoding URLs as " + "UTF-8"); // $NON-NLS-1$
      }
    }
    return parameters.toString();
  }

  protected void setMustBeVisible(boolean mustBeVisible) {
    this.mustBeVisible = mustBeVisible;
  }

  public WaitingPageObject<T> removalWaiter(WebElement element) {
    return ExpectWaiter.waiter(removalCondition(element), this);
  }

  protected static ExpectedCondition<Boolean> removalCondition(WebElement element) {
    return ExpectedConditions2.stalenessOrNonPresenceOf(element);
  }

  public WaitingPageObject<T> updateWaiter(WebElement element) {
    return ExpectWaiter.waiter(ExpectedConditions2.updateOfElement(element), this);
  }

  public WaitingPageObject<T> updateWaiter() {
    return ExpectWaiter.waiter(ExpectedConditions2.updateOfElement(loadedElement), this);
  }

  public WaitingPageObject<T> showWaiter(WebElement element, boolean show) {
    if (show) {
      return visibilityWaiter(element);
    }
    return removalWaiter(element);
  }

  public WaitingPageObject<T> visibilityWaiter(WebElement element) {
    return ExpectWaiter.waiter(ExpectedConditions.visibilityOf(element), this);
  }

  public WaitingPageObject<T> visibilityWaiter(SearchContext searchContext, By by) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(searchContext, by), this);
  }

  public ExpectedCondition<?> ajaxUpdateCondition(By ajaxElem) {
    return ExpectedConditions2.ajaxUpdateExpectBy(
        driver.findElement(ajaxElem), new ByChained(ajaxElem, ExpectedConditions2.XPATH_FIRSTELEM));
  }

  public WaitingPageObject<T> ajaxUpdate(By ajaxElem) {
    return ExpectWaiter.waiter(ajaxUpdateCondition(ajaxElem), this);
  }

  public WaitingPageObject<T> ajaxUpdate(WebElement ajaxElem) {
    return ExpectWaiter.waiter(ExpectedConditions2.ajaxUpdate(ajaxElem), this);
  }

  public WaitingPageObject<T> ajaxUpdateExpect(WebElement ajaxElem, WebElement expectedElement) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.ajaxUpdateExpect(ajaxElem, expectedElement), this);
  }

  public WaitingPageObject<T> ajaxUpdateEmpty(WebElement ajaxElem) {
    return ExpectWaiter.waiter(ExpectedConditions2.ajaxUpdateEmpty(ajaxElem), this);
  }

  protected WebElement waitForElement(By by) {
    return getWaiter().until(ExpectedConditions.visibilityOfElementLocated(by));
  }

  protected WebElement waitForElement(final SearchContext context, final By by) {
    return getWaiter().until(ExpectedConditions2.visibilityOfElementLocated(context, by));
  }

  protected WebElement waitForElement(final WebElement element) {
    return getWaiter().until(ExpectedConditions.visibilityOf(element));
  }

  protected void waitForElementInvisibility(final WebElement element) {
    getWaiter().until(ExpectedConditions2.invisibilityOf(element));
  }

  protected WebElement waitForHiddenElement(final WebElement element) {
    return getWaiter().until(ExpectedConditions2.presenceOfElement(element));
  }

  @Override
  public T get() {
    refreshTime = System.currentTimeMillis();
    // Due to some of the pages using various Sections AJAXy stuff, we need
    // a bit more thorough wait checking then the plain old check by Selenium.
    getWaiter()
        .until(
            driver ->
                ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState")
                    .equals("complete"));
    return getWaiter()
        .until(
            new ExpectedCondition<T>() {
              @Override
              public T apply(WebDriver driver) {
                try {
                  checkLoaded();
                  return actualPage();
                } catch (NotFoundException nfe) {
                  isError();
                  throw nfe;
                } catch (StaleElementReferenceException stale) {
                  return null;
                }
              }

              @Override
              public String toString() {
                return AbstractPage.this.toString();
              }
            });
  }

  @Override
  public String toString() {
    String contextStr = "";
    String loadedStr = "";
    if (!(relativeTo instanceof WebDriver)) {
      contextStr = " searchContext:" + relativeTo;
    }
    if (loadedElement != null) {
      loadedStr = " loadedElement:" + loadedElement;
    }
    return "PageObject " + getClass() + contextStr + " loadedBy:" + loadedBy + loadedStr;
  }

  @Override
  public long getRefreshTime() {
    return refreshTime;
  }

  @SuppressWarnings("unchecked")
  protected T actualPage() {
    return (T) this;
  }

  protected void isError() {
    if (isPresent(By.xpath("//div[@class='area error']/h2"))) {
      throw new RuntimeException("Error page present");
    }
  }

  protected By byForPageTitle(String title) {
    if (context.getTestConfig().isNewUI())
      return By.xpath("//header/div/div/h5[text()=" + quoteXPath(title) + "]");
    else return By.xpath("id('header-inner')/div[text()=" + quoteXPath(title) + "]");
  }

  public ErrorPage errorPage() {
    return new ErrorPage(context).get();
  }

  protected void setRelativeTo(SearchContext relativeTo) {
    this.relativeTo = relativeTo;
  }

  public WebElement getLoadedElement() {
    return loadedElement;
  }

  protected T clickAndUpdate(WebElement element) {
    WaitingPageObject<T> updateWaiter = updateWaiter(element);
    element.click();
    return updateWaiter.get();
  }

  protected T clickAndRemove(WebElement element) {
    WaitingPageObject<T> removalWaiter = removalWaiter(element);
    element.click();
    return removalWaiter.get();
  }

  // It's a sad day when this is the only way
  public void sleepyTime(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void scrollToElement(WebElement el) {
    if (driver instanceof JavascriptExecutor) {
      ((JavascriptExecutor) driver)
          .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }
  }

  protected By byPrefixId(String prefix, String postfix) {
    return By.id(prefix + postfix);
  }

  protected WebElement findWithId(String prefix, String postfix) {
    return driver.findElement(byPrefixId(prefix, postfix));
  }

  protected boolean isNewUI() {
    return context.getTestConfig().isNewUI();
  }

  protected Object executeSubmit(String args) {
    String submitFunc = isNewUI() ? "EQ.event" : "_subev";
    return ((JavascriptExecutor) driver).executeScript(submitFunc + args);
  }

  protected void switchTab(int tabIndex) {
    String tab = new ArrayList<>(driver.getWindowHandles()).get(tabIndex);
    driver.switchTo().window(tab);
  }
}
