package io.github.openequella.rest;

import static com.tle.webtests.test.AbstractSessionTest.AUTOTEST_LOW_PRIVILEGE_LOGON;
import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

@TestInstitution("facet")
public class Search2ExportTest extends AbstractRestApiTest {
  private final String EXPORT_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/search2/export";
  private final NameValuePair[] defaultQueryStrings = {
    new NameValuePair("collections", "b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e"),
    new NameValuePair("length", "10"),
    new NameValuePair("start", "0")
  };

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(FacetSearchApiTest.class);
    }
    return testConfig;
  }

  @Test(description = "Export without ACL")
  public void withoutACL() throws IOException {
    // Login as a low privilege user.
    makeClientRequest(buildLoginMethod(AUTOTEST_LOW_PRIVILEGE_LOGON, PASSWORD));
    HttpMethod method = buildExportRequest(null);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 403);
  }

  @Test(description = "Export with unsupported file formats")
  public void unsupportedFormat() throws IOException {
    HttpMethod method = buildExportRequest(null);
    method.setRequestHeader("accept", "text/xml");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 406);
  }

  @Test(description = "Export with multiple Collections")
  public void onlyOneCollection() throws IOException {
    NameValuePair[] queryStrings = Arrays.copyOf(defaultQueryStrings, 4);
    queryStrings[3] = new NameValuePair("collections", "b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e");
    HttpMethod method = buildExportRequest(queryStrings);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 400);
  }

  @Test(description = "A standard export")
  public void export() throws IOException {
    HttpMethod method = buildExportRequest(null);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);
    assertEquals(method.getResponseHeader("Content-Type").getValue(), "text/csv");
  }

  private HttpMethod buildExportRequest(NameValuePair[] queryStrings) {
    final HttpMethod method = new GetMethod(EXPORT_API_ENDPOINT);
    method.addRequestHeader("accept", "text/csv");
    method.setQueryString(queryStrings != null ? queryStrings : defaultQueryStrings);
    return method;
  }
}
