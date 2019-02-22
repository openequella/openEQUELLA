package com.tle.resttests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.json.assertions.ApiAssertions;
import com.tle.json.framework.CleanupAfter;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.TestInstitution;
import com.tle.json.framework.TokenProvider;
import com.tle.resttests.util.OAuthTokenCache;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;
import java.net.URI;
import java.util.List;
import java.util.ListIterator;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.collections.Lists;

@TestInstitution("autotest")
public class AbstractRestAssuredTest extends AbstractSessionTest
    implements TokenProvider, CleanupController {
  private OAuthTokenCache tokens;
  public static final ObjectMapper MAPPER = new ObjectMapper();
  private String namePrefix;
  protected ApiAssertions asserter;

  public AbstractRestAssuredTest() {
    this.namePrefix = getClass().getSimpleName();
  }

  @Override
  protected String getNamePrefix() {
    return namePrefix;
  }

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    asserter = new ApiAssertions(context);
  }

  @BeforeSuite
  public void setupProxy(ITestContext testContext) {
    testContext.getSuite().setAttribute("cleanups", Lists.newArrayList());
    String proxyHost = testConfig.getProperty("proxy.host");
    String proxyPortString = testConfig.getProperty("proxy.port");
    if (proxyPortString != null) {
      System.setProperty("http.proxyHost", proxyHost);
      System.setProperty("http.proxyPort", proxyPortString);
    }
  }

  @Override
  public void addCleanup(CleanupAfter cleanup) {
    if (cleanup == null) {
      throw new NullPointerException("Added null cleanup");
    }
    @SuppressWarnings("unchecked")
    List<CleanupAfter> cleanups = (List<CleanupAfter>) suite.getAttribute("cleanups");
    cleanups.add(cleanup);
  }

  @AfterSuite
  public void cleanupAfterSuite(ITestContext context) {
    if (suite != null) {
      @SuppressWarnings("unchecked")
      List<CleanupAfter> cleanups = (List<CleanupAfter>) suite.getAttribute("cleanups");
      if (cleanups != null) {
        ListIterator<CleanupAfter> listIterator = cleanups.listIterator(cleanups.size());
        while (listIterator.hasPrevious()) {
          CleanupAfter cleanup = listIterator.previous();
          if (cleanup != null) {
            cleanup.cleanUp();
          }
        }
      }
    }
  }

  public OAuthTokenCache getTokens() {
    if (tokens == null) {
      tokens =
          new OAuthTokenCache(URI.create(context.getBaseUrl()), MAPPER, context, this, testConfig);
    }
    return tokens;
  }

  public RequestsBuilder builder() {
    return new RequestsBuilder(this, this, context.getBaseURI());
  }

  @Override
  public String getToken() {
    if (getDefaultUser() == null) {
      return null;
    }
    return getTokens().getToken(getDefaultUser());
  }

  protected String getDefaultUser() {
    return RestTestConstants.USERID_AUTOTEST;
  }
}
