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
    final List<BaseEntityLabel> searches = getAdvancedSearches();
    assertEquals(
        "The number of returned advanced searches should match the institution total.",
        3,
        searches.size());
  }

  private List<BaseEntityLabel> getAdvancedSearches() throws IOException {
    final HttpMethod method = new GetMethod(ADVANCEDSEARCH_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsString(), new TypeReference<List<BaseEntityLabel>>() {});
  }

  private static class BaseEntityLabel {
    private String owner;
    private String privType;
    private String uuid;
    private String value;
    private boolean systemType;
    private long bundleId;
    private long id;
    private long idValue;

    public String getOwner() {
      return owner;
    }

    public void setOwner(String owner) {
      this.owner = owner;
    }

    public String getPrivType() {
      return privType;
    }

    public void setPrivType(String privType) {
      this.privType = privType;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public boolean isSystemType() {
      return systemType;
    }

    public void setSystemType(boolean systemType) {
      this.systemType = systemType;
    }

    public long getBundleId() {
      return bundleId;
    }

    public void setBundleId(long bundleId) {
      this.bundleId = bundleId;
    }

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public long getIdValue() {
      return idValue;
    }

    public void setIdValue(long idValue) {
      this.idValue = idValue;
    }
  }
}
