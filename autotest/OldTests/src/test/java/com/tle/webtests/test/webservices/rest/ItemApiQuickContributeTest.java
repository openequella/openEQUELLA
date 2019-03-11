package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.tle.common.Pair;
import com.tle.webtests.pageobject.settings.QuickContributeAndVersionPage;
import com.tle.webtests.test.files.Attachments;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ItemApiQuickContributeTest extends AbstractItemApiTest {
  private static final String OAUTH_CLIENT_ID = "ItemApiQuickContributeTestClient";
  private static final String FILENAME = "avatar.png";
  private static final String QUICKCONTRIB_COLLECTION = "Basic Items";
  private static final String NO_COLLECTION = "<NONE>";
  private static final String QUICKCONTRIB_COLLECTION_UUID = "b28f1ffe-2008-4f5e-d559-83c8acd79316";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @BeforeClass
  public void setUp() throws Exception {
    logon("AutoTest", "automated");
    QuickContributeAndVersionPage page =
        new QuickContributeAndVersionPage(context).load().selectCollection(QUICKCONTRIB_COLLECTION);
    page.save();
    logout();
  }

  @Test
  public void quickUploadTest() throws Exception {
    String token = getToken();
    HttpResponse putfileResponse = putFile(FILENAME, Attachments.get(FILENAME), token, true);

    assertResponse(putfileResponse, 201, "HTTP Status code error");
    Header headerLocation = putfileResponse.getFirstHeader("Location");
    assertNotNull(headerLocation);
    String itemUri = headerLocation.getValue();

    JsonNode itemNode = getItemJson(itemUri, "basic,detail", token);
    String attachmentUuid = itemNode.get("uuid").getTextValue();
    assertEquals(itemNode.get("name").getTextValue(), FILENAME);
    assertEquals(
        itemNode.get("collection").get("uuid").getTextValue(), QUICKCONTRIB_COLLECTION_UUID);
    // upload again to verify uuid
    putfileResponse = putFile(FILENAME, Attachments.get(FILENAME), token, true);
    assertResponse(putfileResponse, 201, "HTTP Status code error");
    headerLocation = putfileResponse.getFirstHeader("Location");
    assertNotNull(headerLocation);
    itemUri = headerLocation.getValue();

    itemNode = getItemJson(itemUri, "basic,detail", token);
    assertEquals(itemNode.get("uuid").getTextValue(), attachmentUuid);
  }

  @Test
  public void errorsTest() throws Exception {
    String token = getToken();
    HttpResponse putfileResponse = putFile("", Attachments.get(FILENAME), token, false);
    int statusCode = putfileResponse.getStatusLine().getStatusCode();
    // with no filename identifier, the @Path("quick/{filename}") won't
    // match
    assertTrue(
        statusCode == 405 || statusCode == 500,
        "HTTP Status code, expected 405 or 500, but got " + statusCode);
    JsonNode itemNode = mapper.readTree(putfileResponse.getEntity().getContent());
    String errMsg = itemNode.get("error").getTextValue();
    assertTrue(errMsg.equals("Internal Server Error") || errMsg.equals("Method Not Allowed"));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
    QuickContributeAndVersionPage page =
        new QuickContributeAndVersionPage(context).load().selectCollection(NO_COLLECTION);
    page.save();
    logout();
    super.cleanupAfterClass();
  }

  private HttpResponse putFile(String filename, URL resource, String token, boolean consume)
      throws IOException {
    String avatarUrl =
        context.getBaseUrl() + "api/item/quick/" + com.tle.common.URLUtils.urlEncode(filename);
    HttpPut putfile = new HttpPut(avatarUrl);
    URLConnection file = resource.openConnection();
    InputStreamEntity inputStreamEntity =
        new InputStreamEntity(file.getInputStream(), file.getContentLength());
    inputStreamEntity.setContentType("application/octet-stream");
    putfile.setEntity(inputStreamEntity);
    HttpResponse putfileResponse = execute(putfile, consume, token);
    return putfileResponse;
  }
}
