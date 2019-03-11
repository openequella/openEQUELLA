package com.tle.resttests.test.file;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.requests.StagingRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.files.Attachments;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.testng.annotations.Test;

/** @author Aaron */
public class StagingApiTest extends AbstractRestAssuredTest {
  private static final int LARGE_FILE_SIZE = 6 * 1024 * 1024;
  private static final int MIN_PART_SIZE = 5 * 1024 * 1024;
  private static final String LARGE_PATH = "large path";
  private static final String AVATAR_PATH = "folder/avatar.png";
  private static final String COPY_PATH = "another/copy.png";
  private static final String AVATAR_PNG_ETAG = "\"5a4e69eeae86aa557c4a27d52257b757\"";
  private StagingRequests staging;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    staging = builder().staging();
  }

  @Test
  public void testFiles() throws Exception {
    // create staging
    String stagingUrl = staging.create();

    // put a file to the folder1 content url
    final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));

    Response response = staging.putFile(stagingUrl, AVATAR_PATH, file);
    assertEquals(response.getHeader("ETag"), AVATAR_PNG_ETAG);

    // check details

    ObjectNode stagingRoot = staging.list(stagingUrl);
    ObjectNode avdetails = findFile(stagingRoot, AVATAR_PATH);
    assertDetails(avdetails);

    response = staging.copy(stagingUrl, AVATAR_PATH, COPY_PATH);

    assertEquals(response.getHeader("ETag"), AVATAR_PNG_ETAG);

    response = staging.head(stagingUrl, COPY_PATH);
    assertEquals(Long.parseLong(response.getHeader("Content-Length")), 12627);
    assertEquals(response.getHeader("Content-Type"), "image/png");
    assertEquals(response.getHeader("ETag"), AVATAR_PNG_ETAG);
    assertTrue(response.getHeader("Last-Modified") != null);

    // check file listing for alphabetical
    stagingRoot = staging.list(stagingUrl);
    avdetails = findFile(stagingRoot, AVATAR_PATH);
    assertDetails(avdetails);
    avdetails = findFile(stagingRoot, COPY_PATH);
    assertDetails(avdetails);
    assertEquals(findFileIndex(stagingRoot, COPY_PATH), 0);
    assertEquals(findFileIndex(stagingRoot, AVATAR_PATH), 1);

    // delete a file
    staging.deleteFile(stagingUrl, COPY_PATH);
    staging.headNotFound(stagingUrl, COPY_PATH);

    stagingRoot = staging.list(stagingUrl);
    assertEquals(findFileIndex(stagingRoot, COPY_PATH), -1);
    assertEquals(findFileIndex(stagingRoot, AVATAR_PATH), 0);

    staging.delete(stagingUrl);
    staging.headNotFound(stagingUrl, AVATAR_PATH);
  }

  private void assertDetails(ObjectNode avdetails) {
    assertEquals(avdetails.get("size").asLong(), 12627);
    assertEquals(avdetails.get("contentType").asText(), "image/png");
    assertEquals(avdetails.get("etag").asText(), AVATAR_PNG_ETAG);
  }

  @Test
  public void testUnzip() throws Exception {
    // create staging
    String stagingUrl = staging.create();

    // put a file to the folder1 content url
    final File file = new File(getPathFromUrl(Attachments.get("package.zip")));

    staging.unzip(stagingUrl, "myzip.zip", file, "unzippedхцч/sub1");

    // check contents of zip
    final ObjectNode deepRoot2 = staging.list(stagingUrl);
    // find the zip
    findFile(deepRoot2, "myzip.zip");

    // find some top level zip content
    findFile(deepRoot2, "unzippedхцч/sub1/ConditionsOfUse.html");

    // find a nested zip content
    ObjectNode cultureNode = findFile(deepRoot2, "unzippedхцч/sub1/graphics/culture.jpg");

    // download it
    final File dlFile = File.createTempFile("culture", "jpg");
    dlFile.deleteOnExit();
    staging.downloadToFile(cultureNode, dlFile);
    assertEquals(
        cultureNode.get("size").asInt(), dlFile.length(), "Wrong file size for culture.jpg");
  }

  private int findFileIndex(ObjectNode staging, String filename) {
    ArrayNode filesArray = (ArrayNode) staging.get("files");
    for (int i = 0; i < filesArray.size(); i++) {
      ObjectNode file = (ObjectNode) filesArray.get(i);
      if (file.get("name").asText().equals(filename)) {
        return i;
      }
    }
    return -1;
  }

  private ObjectNode findFile(ObjectNode staging, String filename) {
    ArrayNode filesArray = (ArrayNode) staging.get("files");
    for (int i = 0; i < filesArray.size(); i++) {
      ObjectNode file = (ObjectNode) filesArray.get(i);
      if (file.get("name").asText().equals(filename)) {
        return file;
      }
    }
    throw new AssertionError("File " + filename + " not found");
  }

  @Test
  public void restrictions() throws IOException, DateParseException {
    String stagingUrl = staging.create();

    // put a file to the folder1 content url
    final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));

    Response response = staging.putFile(stagingUrl, AVATAR_PATH, file);
    assertEquals(response.getHeader("ETag"), AVATAR_PNG_ETAG);

    RequestSpecification notModified = staging.notModified();
    notModified.header("If-None-Match", AVATAR_PNG_ETAG);
    staging.get(notModified, stagingUrl, AVATAR_PATH);
    response = staging.head(stagingUrl, AVATAR_PATH);
    Date date = DateUtil.parseDate(response.header("Last-Modified"));

    notModified = staging.notModified();
    notModified.header("If-Modified-Since", DateUtil.formatDate(date));
    staging.get(notModified, stagingUrl, AVATAR_PATH);
  }

  @Test(groups = "eps")
  public void partial() throws IOException {
    String stagingUrl = staging.create();

    // put a file to the folder1 content url
    final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));

    RequestSpecification partial = staging.partialContent();
    partial.header("Range", "bytes=100-199");
    partial.expect().header("Content-Range", "bytes 100-199/12627").header("Content-Length", "100");
    byte[] second100 = staging.get(partial, stagingUrl, AVATAR_PATH).asByteArray();
    byte[] original = new byte[100];
    FileInputStream finp = new FileInputStream(file);
    try {
      finp.skip(100);
      ByteStreams.read(finp, original, 0, 100);
      assertEquals(second100, original, "Bytes returned are wrong");
    } finally {
      finp.close();
    }
  }

  @Test(groups = "eps")
  public void uploadLarge() throws IOException, DateParseException {
    String stagingUrl = staging.create();

    File largeFile = File.createTempFile("large", "file");
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(largeFile));
    Random random = new Random();
    for (int i = 0; i < LARGE_FILE_SIZE; i++) {
      out.write(random.nextInt());
    }
    out.close();
    largeFile.deleteOnExit();

    String uploadId = staging.startMultipart(stagingUrl, LARGE_PATH);
    long offset = 0;
    long length = largeFile.length();
    int partNum = 1;
    List<Integer> partNums = Lists.newArrayList();
    List<String> etags = Lists.newArrayList();
    while (offset < length) {
      long left = length - offset;
      long size = Math.min(left, MIN_PART_SIZE);
      partNums.add(partNum);
      etags.add(
          staging.uploadPart(stagingUrl, LARGE_PATH, uploadId, largeFile, partNum++, offset, size));
      offset += size;
    }
    String etag = staging.completeMultipart(stagingUrl, LARGE_PATH, uploadId, partNums, etags);
    Response response = staging.head(stagingUrl, LARGE_PATH);
    String lenHeader = response.getHeader("Content-Length");
    assertEquals(Long.parseLong(lenHeader), largeFile.length());
    assertEquals(response.getHeader("E-Tag"), etag);
    File downloadFile = File.createTempFile("large", "file");
    staging.downloadToFile(stagingUrl, LARGE_PATH, downloadFile);
    assertEquals(Files.hash(downloadFile, Hashing.md5()), Files.hash(largeFile, Hashing.md5()));
  }

  @Test
  public void testMultipartFail() throws IOException {
    String stagingUrl = staging.create();
    final File file = new File(getPathFromUrl(Attachments.get("avatar.png")));
    staging.putFileMultipart(stagingUrl, "wontwork", file);
  }
}
