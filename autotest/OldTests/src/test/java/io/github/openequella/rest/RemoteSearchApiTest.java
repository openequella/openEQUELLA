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

@TestInstitution("vanilla")
public class RemoteSearchApiTest extends AbstractRestApiTest {
  private final String REMOTESEARCH_API_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/settings/remotesearch";

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(RemoteSearchApiTest.class);
    }
    return testConfig;
  }

  @Test
  public void testRetrieveRemoteSearches() throws Exception {
    final List<BaseEntitySummary> searches = getRemoteSearches();
    assertEquals(
        "The number of returned remote searches should match the institution total.",
        2,
        searches.size());
  }

  private List<BaseEntitySummary> getRemoteSearches() throws IOException {
    final HttpMethod method = new GetMethod(REMOTESEARCH_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsString(), new TypeReference<List<BaseEntitySummary>>() {});
  }
}
