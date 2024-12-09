package com.tle.webtests.test.webservices.rest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.common.URLUtils;
import com.tle.common.Utils;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.test.files.Attachments;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.Test;

public class FileApiTest extends AbstractRestApiTest {
  private static final String TEST_FILE = "Special characters - хцч test2.jpg";
  private static final String FOLDER_1 = "#& 'testfolder1\"%%%.*<script> yada";

  private static final String OAUTH_CLIENT_ID = "FileApiTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testUnzip() throws Exception {
    // create staging
    HttpResponse response = execute(new HttpPost(context.getBaseUrl() + "api/file/"), true);
    assertResponse(response, 201, "201 not returned from staging creation");

    // extract Location and do a get
    final String stagingDirUrl = response.getFirstHeader("Location").getValue();
    final ObjectNode deepRoot = (ObjectNode) getEntity(stagingDirUrl, null, "deep", true);
    final String contentUrl = getLink(deepRoot, "content");

    // put a file to the folder1 content url
    final File file = new File(AbstractPage.getPathFromUrl(Attachments.get("package.zip")));

    response =
        execute(getPut(contentUrl + "/myzip.zip", file, "unzipto", "unzippedхцч/sub1"), true);
    assertResponse(response, 201, "201 not returned from PUT file");

    // check contents of zip
    final ObjectNode deepRoot2 = (ObjectNode) getEntity(stagingDirUrl, null, "deep", true);
    // find the zip
    ObjectNode zipNode = findFile(deepRoot2, "myzip.zip");

    // find the top level unzip folder
    ObjectNode unzipNode = findFolder(deepRoot2, "unzippedхцч");
    ObjectNode unzipNode2 = findFolder(unzipNode, "sub1");

    // find some top level zip content
    ObjectNode conditionsNode = findFile(unzipNode2, "ConditionsOfUse.html");
    ObjectNode graphicsNode = findFolder(unzipNode2, "graphics");

    // find a nested zip content
    ObjectNode cultureNode = findFile(graphicsNode, "culture.jpg");
    // download it
    String cultureContentUrl = getLink(cultureNode, "content");
    final File dlFile = File.createTempFile("culture", "jpg");
    dlFile.deleteOnExit();
    download(cultureContentUrl, dlFile, null);
    assertEquals(
        "Wrong file size for culture.jpg", dlFile.length(), cultureNode.get("size").asInt());
  }

  @Test
  public void testFileCopyWithOAuthLogin() throws Exception {
    final String token = requestToken(OAUTH_CLIENT_ID);
    final String sourceItemUuid = "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";
    final String sourceItemVersion = "1";

    // do copy, ensure 201
    final List<NameValuePair> qparams = Lists.newArrayList();
    qparams.add(new BasicNameValuePair("uuid", sourceItemUuid));
    qparams.add(new BasicNameValuePair("version", sourceItemVersion));
    HttpResponse response =
        execute(
            new HttpPost(
                context.getBaseUrl() + "api/file/copy?" + URLEncodedUtils.format(qparams, "UTF-8")),
            true,
            token);
    assertResponse(response, 201, "201 not returned on copy");
    final String copyLocation = response.getFirstHeader("Location").getValue();

    // get a dir listing, make sure it matches what can be seen in UI
    response = execute(new HttpGet(copyLocation), false, token);
    final JsonNode folderNode = readJson(mapper, response);

    // TODO: iterate rest of files
    // System.out.println(folderNode.toString());

    // ensure avatar.png is there, get it's download link
    final JsonNode avatarNode = findFileNodeByName(folderNode, "avatar.png");
    assertNotNull("avatar.png not found", avatarNode);
    final String avatarContentUrl = avatarNode.get("links").get("content").asText();

    // download avatar.png file
    final File file = File.createTempFile("avatar", "png");
    file.deleteOnExit();
    download(avatarContentUrl, file, token);

    // byte match original file with downloaded
    final File original = new File(AbstractPage.getPathFromUrl(Attachments.get("avatar.png")));
    final String origMd5 = md5(original);
    final String echoedMd5 = md5(file);
    assertEquals("File corrupted!", origMd5, echoedMd5);

    // delete staging
    execute(new HttpDelete(copyLocation), true, token);
  }

  private JsonNode findFileNodeByName(JsonNode folderNode, String filename) {
    final JsonNode filesNode = folderNode.get("files");
    assertNotNull("files node was not present", filesNode);

    final int nodeCount = filesNode.size();
    for (int i = 0; i < nodeCount; i++) {
      final JsonNode fileNode = filesNode.get(i);
      final String name = fileNode.get("filename").asText();
      if (name.equals(filename)) {
        return fileNode;
      }
    }
    return null;
  }

  @Test
  public void testFileCopyNoPrivs() throws Exception {
    final String sourceItemUuid = "2f6e9be8-897d-45f1-98ea-7aa31b449c0e";
    final String sourceItemVersion = "1";

    // do copy, ensure error (403?)
    final List<NameValuePair> qparams = Lists.newArrayList();
    qparams.add(new BasicNameValuePair("uuid", sourceItemUuid));
    qparams.add(new BasicNameValuePair("version", sourceItemVersion));
    HttpResponse response =
        execute(
            new HttpPost(
                context.getBaseUrl() + "api/file/copy?" + URLEncodedUtils.format(qparams, "UTF-8")),
            true);
    assertResponse(response, 403, "403 not returned on copy");
  }

  @Test
  public void testEverything() throws Exception {
    // create staging
    HttpResponse response = execute(new HttpPost(context.getBaseUrl() + "api/file/"), true);
    assertResponse(response, 201, "201 not returned from staging creation");
    // extract Location and do a get
    final String stagingDirUrl = response.getFirstHeader("Location").getValue();

    // create a sub folder (first method : post to root folder)
    response =
        execute(
            getPost(stagingDirUrl, "{\"filename\":\"" + Utils.jsescape(FOLDER_1) + "\"}"), true);
    assertResponse(response, 201, "201 not returned from folder creation");
    // extract Location and do a get
    final String folder1Location = response.getFirstHeader("Location").getValue();
    response = execute(new HttpGet(folder1Location), false);
    final JsonNode folderNode = readJson(mapper, response);
    JsonNode links = folderNode.get("links");
    String folder1DirUrl = links.get("dir").asText();
    assertEquals(
        "folder1 dir url not correct",
        folder1DirUrl,
        stagingDirUrl + "/" + URLUtils.urlEncode(FOLDER_1, false));
    final String folder1ContentUrl = links.get("content").asText();

    // create a folder2 (second method : put to non existent URL)
    response = execute(getPut(stagingDirUrl + "/folder2", "{\"filename\":\"folder2\"}"), true);
    assertResponse(response, 201, "201 not returned from folder2 creation");
    // extract Location and do a get
    final String folder2Location = response.getFirstHeader("Location").getValue();
    response = execute(new HttpGet(folder2Location), false);
    final JsonNode folder2Node = readJson(mapper, response);
    links = folder2Node.get("links");
    final String folder2DirUrl = links.get("dir").asText();
    assertEquals("folder2 dir url not correct", folder2DirUrl, stagingDirUrl + "/folder2");
    // String folder2ContentUrl = links.get(2).get("href").asText();

    // create a folder3 (third method : put to non existent URL, no content sent)
    response = execute(new HttpPut(stagingDirUrl + "/folder3"), true);
    assertResponse(response, 201, "201 not returned from folder3 creation");

    // create a folder3/folder3.1
    response = execute(new HttpPut(stagingDirUrl + "/folder3/folder3.1"), true);
    assertResponse(response, 201, "201 not returned from folder3.1 creation");

    // create a folder4/folder4.1/folder4.1.1 (where folder4.1 does not yet exists)
    response = execute(new HttpPut(stagingDirUrl + "/folder4/folder4.1/folder4.1.1"), true);
    assertResponse(response, 201, "201 not returned from folder4.1.1 creation");

    // ensure it *really* exists
    response = execute(new HttpGet(stagingDirUrl + "/folder4/folder4.1/folder4.1.1"), false);
    assertResponse(response, 200, "200 not returned from getting folder4.1.1");
    final ObjectNode folder411Node = readJson(mapper, response);

    // rename it
    folder411Node.put("filename", "newfilename");
    response =
        execute(
            getPut(stagingDirUrl + "/folder4/folder4.1/folder4.1.1", folder411Node.toString()),
            true);
    assertResponse(response, 201, "201 not returned folder rename");

    // ensure new name is there, old name is not
    response = execute(new HttpGet(stagingDirUrl + "/folder4/folder4.1/newfilename"), true);
    assertResponse(response, 200, "200 not returned from new folder name");
    response = execute(new HttpGet(stagingDirUrl + "/folder4/folder4.1/folder4.1.1"), true);
    assertResponse(response, 404, "404 not returned from olf folder name");

    // get a deep representation of the root
    response = execute(new HttpGet(stagingDirUrl + "?deep=true"), false);
    assertResponse(response, 200, "200 not returned from getting root (deep)");
    final ObjectNode deepRootNode = readJson(mapper, response);

    // try to find newfilename, they *will* be sorted, we can rely on this
    final ObjectNode folder4Node = (ObjectNode) deepRootNode.path("folders").path(3);
    assertTrue("Couldn't find 4th folder at root", !folder4Node.isMissingNode());
    final ObjectNode folder41Node = (ObjectNode) folder4Node.path("folders").path(0);
    assertTrue("Couldn't find 1st folder at folder4", !folder41Node.isMissingNode());
    assertTrue(
        "Couldn't find 1st folder at folder41",
        !folder41Node.path("folders").path(0).isMissingNode());

    // delete folder2, ensure 204
    response = execute(new HttpDelete(folder2DirUrl), true);
    assertResponse(response, 204, "204 not returned from folder2 delete");

    // try to list contents of folder 2
    response = execute(new HttpGet(folder2DirUrl), true);
    assertResponse(response, 404, "404 not returned from non existent folder2");

    // put again to folder1, ensure 200
    response =
        execute(getPut(folder1DirUrl, "{\"filename\":\"" + Utils.jsescape(FOLDER_1) + "\"}"), true);
    assertResponse(response, 200, "200 not returned from folder put (existing)");

    // put a file to the folder1 content url
    final File file = new File(AbstractPage.getPathFromUrl(Attachments.get(TEST_FILE)));
    String myfileUrl = folder1ContentUrl + "/" + URLUtils.urlEncode(TEST_FILE, false);
    response = execute(getPut(myfileUrl, file), true);
    assertResponse(response, 201, "201 not returned from PUT file");

    // do it again, ensure 200 response this time
    response = execute(getPut(myfileUrl, file), true);
    assertResponse(response, 200, "200 not returned from PUT (replace) file");

    // get a file listing of folder1
    response = execute(new HttpGet(folder1DirUrl), false);
    assertResponse(response, 200, "200 not returned from folder1 file listing");
    final JsonNode folder1ListingNode = readJson(mapper, response);
    final JsonNode folder1FilesNode = folder1ListingNode.get("files");
    assertEquals("1 file not returned in file listing of folder1", folder1FilesNode.size(), 1);
    final String folder1SubFilename = folder1FilesNode.get(0).get("filename").asText();
    assertTrue("myfile filename is wrong", folder1SubFilename.equals(TEST_FILE));

    // download it to disk
    final File echoed = File.createTempFile("echoed", "jpg");
    echoed.deleteOnExit();
    response = download(myfileUrl, echoed);

    // byte match original file with echoed
    final String origMd5 = md5(file);
    final String echoedMd5 = md5(echoed);
    assertEquals("File corrupted!", origMd5, echoedMd5);

    // delete file on server, ensure 204 repsonse
    response = execute(new HttpDelete(myfileUrl), true);
    assertResponse(response, 204, "File not deleted");

    // try to get it, ensure 404
    response = execute(new HttpGet(myfileUrl), true);
    assertResponse(response, 404, "File still gettable");

    // delete staging area
    response = execute(new HttpDelete(stagingDirUrl), true);
    assertResponse(response, 204, "Staging area not deleted");

    // try to get it, ensure 404
    response = execute(new HttpGet(stagingDirUrl), true);
    assertResponse(response, 404, "Staging still gettable");
  }

  @Test
  public void testNestedFolders() throws Exception {
    // As per http://dev.equella.com/issues/6245

    // create staging
    HttpResponse response = execute(new HttpPost(context.getBaseUrl() + "api/file/"), true);
    assertResponse(response, 201, "201 not returned from staging creation");
    // extract Location and do a get
    final String stagingDirUrl = response.getFirstHeader("Location").getValue();

    // create a triple nested folder
    response = execute(new HttpPut(stagingDirUrl + "/f1/f2/f3"), true);
    assertResponse(response, 201, "201 not returned from f3 creation");
    // extract Location and do a get
    final String f3Location = response.getFirstHeader("Location").getValue();

    // Get it, check the links
    response = execute(new HttpGet(f3Location), false);
    assertResponse(response, 200, "200 not returned from f3 GET");

    final ObjectNode f3Node = readJson(mapper, response);
    String f3DirLink = getLink(f3Node, "dir");
    assertEquals("f3 Location and dir link mismatch", f3Location, f3DirLink);

    // Get the parent, check f3 is in folders
    final String f3ParentLoc = getLink((ObjectNode) f3Node.get("parent"), "self");
    final String f3ParentContentLoc = getLink((ObjectNode) f3Node.get("parent"), "content");
    // ensure parent loc is /f1/f2
    assertEquals("f3 parent dir location is wrong", stagingDirUrl + "/f1/f2", f3ParentLoc);

    // get the parent parent (ie f1)
    response = execute(new HttpGet(f3ParentLoc), false);
    assertResponse(response, 200, "200 not returned from f3ParentLoc GET");
    final ObjectNode f2Node = readJson(mapper, response);
    final String f2ParentLoc = getLink((ObjectNode) f2Node.get("parent"), "self");
    final String f2ParentContentLoc = getLink((ObjectNode) f2Node.get("parent"), "content");

    // Upload a file to f1
    final File file = new File(AbstractPage.getPathFromUrl(Attachments.get(TEST_FILE)));
    response =
        execute(
            getPut(f2ParentContentLoc + "/" + URLUtils.urlEncode(TEST_FILE, false), file), true);
    assertResponse(response, 201, "201 not returned from PUT file");

    // GET f1, check the file is in f1.files
    response = execute(new HttpGet(f2ParentLoc), false);
    assertResponse(response, 200, "200 not returned from f2ParentLoc GET");
    final ObjectNode f1Node = readJson(mapper, response);
    ObjectNode myfileNode = findFile(f1Node, TEST_FILE);
    // assertEquals("filename", TEST_FILE, myfileNode.get("filename").asText()); <-- redundant
    // TODO: check links

    // Upload a file to f2
    response =
        execute(
            getPut(f3ParentContentLoc + "/" + URLUtils.urlEncode(TEST_FILE, false), file), true);
    assertResponse(response, 201, "201 not returned from PUT file");

    // Do a deep GET, check all files and folders
    ObjectNode deepRoot = (ObjectNode) getEntity(stagingDirUrl, null, "deep", true);

    // get folder f1
    ObjectNode f1DeepNode = findFolder(deepRoot, "f1");

    // ensure file TEST_FILE
    ObjectNode myfile1DeepNode = findFile(f1DeepNode, TEST_FILE);
    // check links
    String myfile1LinkSelf = getLink(myfile1DeepNode, "self");
    assertEquals(
        "file1 self link wrong",
        stagingDirUrl + "/f1/" + URLUtils.urlEncode(TEST_FILE, false),
        myfile1LinkSelf);
    String myfile1LinkDir = getLink(myfile1DeepNode, "dir");
    assertEquals(
        "file1 dir link wrong",
        stagingDirUrl + "/f1/" + URLUtils.urlEncode(TEST_FILE, false),
        myfile1LinkDir);
    String myfile1LinkContent = getLink(myfile1DeepNode, "content");
    assertTrue(
        "file1 content link wrong",
        myfile1LinkContent.endsWith("content/f1/" + URLUtils.urlEncode(TEST_FILE, false)));

    // get folder f2
    ObjectNode f2DeepNode = findFolder(f1DeepNode, "f2");
    // TODO: ensure file TEST_FILE
    // TODO: check links

    // get folder f3
    ObjectNode f3DeepNode = findFolder(f2DeepNode, "f3");
    // ensure no files and folders
    assertTrue("f3 files not empty", getArray(f3DeepNode, "files").size() == 0);
    assertTrue("f3 folders not empty", getArray(f3DeepNode, "folders").size() == 0);
    // TODO: check links
  }

  private ObjectNode findFile(ObjectNode folderNode, String filename) {
    ArrayNode filesArray = getArray(folderNode, "files");
    for (int i = 0; i < filesArray.size(); i++) {
      ObjectNode file = (ObjectNode) filesArray.get(i);
      if (file.get("filename").asText().equals(filename)) {
        return file;
      }
    }
    throw new AssertionError("File " + filename + " not found");
  }

  private ObjectNode findFolder(ObjectNode folderNode, String filename) {
    ArrayNode filesArray = getArray(folderNode, "folders");
    for (int i = 0; i < filesArray.size(); i++) {
      ObjectNode file = (ObjectNode) filesArray.get(i);
      if (file.get("filename").asText().equals(filename)) {
        return file;
      }
    }
    throw new AssertionError("Folder " + filename + " not found");
  }

  private String md5(File file) throws IOException {
    InputStream inp = new FileInputStream(file);
    try {
      return DigestUtils.md5Hex(inp);
    } finally {
      Closeables.closeQuietly(inp);
    }
  }
}
