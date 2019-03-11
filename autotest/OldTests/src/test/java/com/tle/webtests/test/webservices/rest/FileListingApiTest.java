package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.common.Pair;
import java.io.IOException;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

public class FileListingApiTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "FileListingAutoTest";
  private static final String ITEM_ID = "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";
  private static final String FILENAME = "avatar.png";
  private static final int ITEM_VERSION = 1;

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {

    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  protected String getListingUrl(String uuid, int version) {
    StringBuilder sbuf = new StringBuilder();
    sbuf.append(context.getBaseUrl());
    sbuf.append("api/item/");
    sbuf.append(uuid);
    sbuf.append('/');
    sbuf.append(version);
    sbuf.append("/file");
    return sbuf.toString();
  }

  private JsonNode getFileListing(String itemId, int version) throws IOException {
    return getEntity(getListingUrl(itemId, version), getToken());
  }

  private Header getHeaders(String uri, String token, String headerName, Object... params)
      throws IOException {
    HttpResponse response =
        execute(new HttpHead(appendQueryString(uri, queryString(params))), false, token);
    return response.getFirstHeader(headerName);
  }

  @Test
  public void testEverything() throws Exception {
    JsonNode fileListing = getFileListing(ITEM_ID, ITEM_VERSION);
    JsonNode files = fileListing.get("files");

    if (files.isArray()) {
      // New file listing shows a deep file structure (and files only, not
      // folders)
      boolean found = false;
      for (int counter = 0; counter < files.size(); counter++) {
        if (files.get(counter).get("name").getTextValue().equals(FILENAME)) {
          found = true;

          Header contentLength =
              getHeaders(
                  files.get(counter).get("links").get("self").getTextValue(),
                  getToken(),
                  "Content-Length");
          assertEquals(contentLength.getValue(), "12627");
        }
      }
      assertTrue(found);
    }
  }
}
