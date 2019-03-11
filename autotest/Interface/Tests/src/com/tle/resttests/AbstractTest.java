package com.tle.resttests;

import static org.testng.Assert.assertEquals;

import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractTest {
  // protected static final String KEY_LISTENEREADDED = "ListenerAdded";
  // protected static final String KEY_SETUPLISTENEREADDED =
  // "SetupListenerAdded";
  protected PageContext context;
  protected TestConfig testConfig;

  protected ISuite suite;

  public AbstractTest() {
    testConfig = new TestConfig(getClass(), !isInstitutional());
  }

  @BeforeClass(alwaysRun = true)
  public void setupContext(ITestContext testContext) {
    this.suite = testContext.getSuite();
    File rootFolder = testConfig.getTestFolder();
    String contextUrl;

    if (isInstitutional()) {
      contextUrl = testConfig.getInstitutionUrlFromShortName(rootFolder.getName());
    } else {
      contextUrl = testConfig.getAdminUrl();
    }

    context = new PageContext(testConfig, contextUrl);
    context.setIntegUrl(testConfig.getIntegrationUrl(rootFolder.getName()));
    context.setNamePrefix(getNamePrefix());
    customisePageContext();
  }

  public String prefix() {
    return getClass().getSimpleName();
  }

  protected PageContext newContext(String shortName) {
    String contextUrl = testConfig.getInstitutionUrlFromShortName(shortName);
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

  @AfterClass(alwaysRun = true)
  public void finishedClass(ITestContext testContext) throws Exception {
    if (context == null) {
      return;
    }
    String delValue = testConfig.getProperty("test.deleteitems");
    if (delValue == null || Boolean.parseBoolean(delValue)) {
      cleanupAfterClass();
    }
  }

  protected void cleanupAfterClass() throws Exception {
    // do clean up here
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

  public PageContext getContext() {
    return context;
  }

  public TestConfig getTestConfig() {
    return testConfig;
  }

  public boolean isEquella() {
    return testConfig.isEquella();
  }

  public static String getPathFromUrl(URL file) {
    if (!file.getProtocol().equals("file")) {
      throw new Error("Must be a file: url! - " + file);
    }
    try {
      String part = file.getFile();
      String os = System.getProperty("os.name").toLowerCase();
      if (os.indexOf("windows") >= 0 && part.startsWith("/")) {
        part = part.substring(1);
        part = part.replaceAll("/", Matcher.quoteReplacement("\\"));
      }

      return URLDecoder.decode(part.replace("+", "%2b"), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
