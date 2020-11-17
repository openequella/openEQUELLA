package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import io.github.openequella.rest.models.BaseEntitySummary;
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
    final List<BaseEntitySummary> searches = getAdvancedSearches();
    assertEquals(
        "The number of returned advanced searches should match the institution total.",
        3,
        searches.size());
  }

  private List<BaseEntitySummary> getAdvancedSearches() throws IOException {
    final HttpMethod method = new GetMethod(ADVANCEDSEARCH_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsString(), new TypeReference<List<BaseEntitySummary>>() {});
  }
}
