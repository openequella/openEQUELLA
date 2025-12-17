package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

public class MimeTypeApiTest extends AbstractRestApiTest {

  private final String MIMETYPE_API_ENDPOINT = getTestConfig().getInstitutionUrl() + "api/mimetype";

  @Test
  public void testRetrieveMimeTypes() throws Exception {
    final List<MimeTypeDetail> initialFilters = getMimeTypes();
    assertEquals(
        "The number of returned filters should match the institution total.",
        153,
        initialFilters.size());
  }

  @Test
  public void testRetrieveMimeTypeConfiguration() throws Exception {
    // Let's process all the known mimetypes to ensure we can
    for (MimeTypeDetail detail : getMimeTypes()) {
      final HttpMethod method = new GetMethod(buildViewerConfigPath(detail.getMimeType()));
      assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
      JsonNode viewConfig = mapper.readTree(method.getResponseBodyAsStream());
      assertNotNull(viewConfig.findValue("defaultViewer"));
    }
  }

  @Test
  public void testMimeTypeConfigurationNotFoundHandling() throws IOException {
    final String noSuchMimeType = "blah/blah";
    final HttpMethod method = new GetMethod(buildViewerConfigPath(noSuchMimeType));
    assertEquals(HttpStatus.SC_NOT_FOUND, makeClientRequest(method));
  }

  private List<MimeTypeDetail> getMimeTypes() throws IOException {
    final HttpMethod method = new GetMethod(MIMETYPE_API_ENDPOINT);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    return mapper.readValue(
        method.getResponseBodyAsStream(), new TypeReference<List<MimeTypeDetail>>() {});
  }

  private String buildViewerConfigPath(String mimeType) {
    return MIMETYPE_API_ENDPOINT + "/viewerconfig/" + mimeType;
  }

  // Mirror of com.tle.web.api.settings.MimeTypeDetail
  private static final class MimeTypeDetail {

    private String mimeType = null;
    private String desc = null;

    public String getMimeType() {
      return mimeType;
    }

    public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
    }

    public String getDesc() {
      return desc;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }
  }
}
