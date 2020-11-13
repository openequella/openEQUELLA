package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.util.List;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.jackson.type.TypeReference;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class AdvancedSearchApiTest extends AbstractRestApiTest {
  private final String ADVANCEDSEARCH_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/settings/advancedsearch";

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(AdvancedSearchApiTest.class);
    }
    return testConfig;
  }

  @Test
  public void testRetrieveAdvancedSearches() throws Exception {
    final List<AdvancedSearchSummary> searches = getAdvancedSearches();
    assertEquals(
        "The number of returned advanced searches should match the institution total.",
        3,
        searches.size());
  }

  private List<AdvancedSearchSummary> getAdvancedSearches() throws IOException {
    final HttpMethod method = new GetMethod(ADVANCEDSEARCH_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsString(), new TypeReference<List<AdvancedSearchSummary>>() {});
  }

  // Mirror of com.tle.web.api.settings.AdvancedSearchSummary
  private static class AdvancedSearchSummary {
    private String uuid;
    private String name;

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
