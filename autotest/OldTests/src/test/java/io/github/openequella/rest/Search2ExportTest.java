package io.github.openequella.rest;

import static com.tle.webtests.test.AbstractSessionTest.AUTOTEST_LOW_PRIVILEGE_LOGON;
import static org.testng.Assert.assertEquals;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
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

  @Test(description = "Export should fail when ACL is not granted")
  public void withoutACL() throws IOException {
    // Login as a low privilege user.
    makeClientRequest(buildLoginMethod(AUTOTEST_LOW_PRIVILEGE_LOGON, PASSWORD));
    HttpMethod method = buildExportRequest(null);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 403);
  }

  @Test(description = "Request to export in format other than CSV should fail")
  public void unsupportedFormat() throws IOException {
    HttpMethod method = buildExportRequest(null);
    method.setRequestHeader("accept", "text/xml");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 406);
  }

  @Test(description = "Attempts to export with multiple Collections should fail")
  public void onlyOneCollection() throws IOException {
    NameValuePair[] queryStrings = Arrays.copyOf(defaultQueryStrings, 4);
    queryStrings[3] = new NameValuePair("collections", "b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e");
    HttpMethod method = buildExportRequest(queryStrings);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 400);
  }

  @Test(description = "A standard export")
  public void export() throws IOException, CsvException {
    HttpMethod method = buildExportRequest(null);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);
    assertEquals(method.getResponseHeader("Content-Type").getValue(), "text/csv");

    // This CSV should have 8 rows including one row for headers and 29 columns.
    CSVReader reader = new CSVReader(new StringReader(method.getResponseBodyAsString()));
    List<String[]> csv = reader.readAll();
    assertEquals(csv.size(), 8);

    String[] headers = csv.get(0);
    assertEquals(headers.length, 29);
  }

  @Test(description = "Long query string should not break the export")
  public void exportWithLongQueryString() throws IOException {
    NameValuePair[] queryStrings = Arrays.copyOf(defaultQueryStrings, 4);
    queryStrings[3] =
        new NameValuePair(
            "query",
            "LongStringb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e");
    HttpMethod method = buildExportRequest(queryStrings);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);
  }

  private HttpMethod buildExportRequest(NameValuePair[] queryStrings) {
    final HttpMethod method = new GetMethod(EXPORT_API_ENDPOINT);
    method.addRequestHeader("accept", "text/csv");
    method.setQueryString(queryStrings != null ? queryStrings : defaultQueryStrings);
    return method;
  }
}
