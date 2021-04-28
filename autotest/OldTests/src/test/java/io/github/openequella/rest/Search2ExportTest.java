package io.github.openequella.rest;

import static com.tle.webtests.test.AbstractSessionTest.AUTOTEST_LOW_PRIVILEGE_LOGON;
import static org.testng.Assert.assertEquals;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
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
    assertResponseCode(null, 403);
  }

  @Test(description = "Request to export in format other than CSV should fail")
  public void unsupportedFormat() throws IOException {
    // No need to worry about the HEAD request so only test the GET request.
    HttpMethod request = new GetMethod(EXPORT_API_ENDPOINT);
    request.setQueryString(defaultQueryStrings);
    request.setRequestHeader("accept", "text/xml");
    int statusCode = makeClientRequest(request);
    assertEquals(statusCode, 406);
  }

  @Test(description = "Attempts to export with multiple Collections should fail")
  public void onlyOneCollection() throws IOException {
    NameValuePair[] queryStrings = Arrays.copyOf(defaultQueryStrings, 4);
    queryStrings[3] = new NameValuePair("collections", "b2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e");
    assertResponseCode(queryStrings, 400);
  }

  @Test(description = "A standard export")
  public void export() throws IOException, CsvException {
    List<HttpMethod> requests = buildExportRequests(null);
    for (HttpMethod request : requests) {
      int statusCode = makeClientRequest(request);
      assertEquals(statusCode, 200);
      // For the GET request, also check the response data.
      if (request instanceof GetMethod) {
        // This CSV should have 8 rows including one row for headers and 29 columns.
        assertEquals(request.getResponseHeader("Content-Type").getValue(), "text/csv");
        CSVReader reader = new CSVReader(new StringReader(request.getResponseBodyAsString()));
        List<String[]> csv = reader.readAll();
        assertEquals(csv.size(), 8);

        String[] headers = csv.get(0);
        assertEquals(headers.length, 29);
      }
    }
  }

  @Test(description = "Long query string should not break the export")
  public void exportWithLongQueryString() throws IOException {
    NameValuePair[] queryStrings = Arrays.copyOf(defaultQueryStrings, 4);
    queryStrings[3] =
        new NameValuePair(
            "query",
            "LongStringb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024eb2be4e8e-a0d4-4e6a-b9ff-4c65a7c8024e");
    assertResponseCode(queryStrings, 200);
  }

  private void assertResponseCode(NameValuePair[] queryStrings, int expectCode) throws IOException {
    List<HttpMethod> requests = buildExportRequests(queryStrings);
    for (HttpMethod request : requests) {
      int statusCode = makeClientRequest(request);
      assertEquals(statusCode, expectCode);
    }
  }

  // Build two request - one for confirming the export and one for executing the export.
  private List<HttpMethod> buildExportRequests(NameValuePair[] queryStrings) {
    HttpMethod executeExport = new GetMethod(EXPORT_API_ENDPOINT);
    executeExport.addRequestHeader("accept", "text/csv");
    HttpMethod confirmExport = new HeadMethod(EXPORT_API_ENDPOINT);

    NameValuePair[] searchCriteria = queryStrings != null ? queryStrings : defaultQueryStrings;
    executeExport.setQueryString(searchCriteria);
    confirmExport.setQueryString(searchCriteria);

    List<HttpMethod> requests = new ArrayList<>();
    requests.add(executeExport);
    requests.add(confirmExport);
    return requests;
  }
}
