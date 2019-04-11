package com.tle.json.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import org.hamcrest.Matchers;

public class StagingRequests extends AuthorizedRequests {
  public StagingRequests(
      URI baseUri,
      TokenProvider tokenProvider,
      ObjectMapper mapper,
      PageContext pageContext,
      TestConfig testConfig) {
    super(baseUri, tokenProvider, mapper, pageContext, testConfig);
  }

  @Override
  protected String getBasePath() {
    return "api/staging";
  }

  public String create() {
    return createRequest()
        .expect()
        .header("x-eps-stagingid", Matchers.notNullValue())
        .post(getResolvedPath())
        .getHeader("Location");
  }

  public String createId() {
    return createRequest()
        .expect()
        .header("x-eps-stagingid", Matchers.notNullValue())
        .post(getResolvedPath())
        .getHeader("x-eps-stagingid");
  }

  public String toStagingUrl(String stagingId) {
    return getBaseUri().resolve(getBasePath() + "/" + stagingId).toString();
  }

  private String getFileUrl(String stagingUrl, String path) {
    return stagingUrl + "/" + path;
  }

  public Response putFile(String stagingUrl, String path, File file) throws IOException {
    return successfulRequest().content(Files.toByteArray(file)).put(getFileUrl(stagingUrl, path));
  }

  public Response putFileMultipart(String stagingUrl, String path, File file) throws IOException {
    return badRequest().multiPart(file).put(getFileUrl(stagingUrl, path));
  }

  public ObjectNode list(String stagingUrl) {
    return object(successfulRequest().get(stagingUrl));
  }

  public Response copy(String stagingDirUrl, String source, String dest) {
    return successfulRequest().queryParam("copyfrom", source).put(getFileUrl(stagingDirUrl, dest));
  }

  public Response head(String stagingDirUrl, String path) {
    return successfulRequest().head(getFileUrl(stagingDirUrl, path));
  }

  public Response deleteFile(String stagingDirUrl, String path) {
    return successfulDelete().delete(getFileUrl(stagingDirUrl, path));
  }

  public Response headNotFound(String stagingDirUrl, String path) {
    return notFoundRequest().head(getFileUrl(stagingDirUrl, path));
  }

  public Response delete(String stagingDirUrl) {
    return successfulDelete().delete(stagingDirUrl);
  }

  public Response unzip(String stagingUrl, String path, File file, String zipDir)
      throws IOException {
    return successfulRequest()
        .content(Files.toByteArray(file))
        .queryParam("unzipto", zipDir)
        .put(getFileUrl(stagingUrl, path));
  }

  public Response get(RequestSpecification request, String stagingUrl, String path) {
    return request.get(getFileUrl(stagingUrl, path));
  }

  public void downloadToFile(ObjectNode fileNode, File file) throws IOException {
    Files.write(
        successfulRequest()
            .get(URLDecoder.decode(getLink(fileNode, "self"), "UTF-8"))
            .body()
            .asByteArray(),
        file);
  }

  public void downloadToFile(String stagingUrl, String path, File file) throws IOException {
    Files.write(successfulRequest().get(getFileUrl(stagingUrl, path)).body().asByteArray(), file);
  }

  public String startMultipart(String stagingUrl, String path) {
    ObjectNode object =
        object(successfulRequest().queryParam("uploads", "").post(getFileUrl(stagingUrl, path)));
    return object.get("uploadId").asText();
  }

  public String uploadPart(
      String stagingUrl,
      String path,
      String uploadId,
      File file,
      int partNumber,
      long fileOffest,
      long length)
      throws IOException {
    try (FileInputStream finp = new FileInputStream(file)) {
      finp.skip(fileOffest);
      InputStream inp = ByteStreams.limit(finp, length);
      byte[] bytes = ByteStreams.toByteArray(inp);
      return successfulRequest()
          .content(bytes)
          .queryParam("partNumber", partNumber)
          .queryParam("uploadId", uploadId)
          .put(getFileUrl(stagingUrl, path))
          .getHeader("ETag");
    }
  }

  public String completeMultipart(
      String stagingUrl, String path, String uploadId, List<Integer> partNums, List<String> etags) {
    ObjectNode request = newObject();
    ArrayNode parts = newArray();
    request.put("parts", parts);
    int i = 0;
    for (Integer partNum : partNums) {
      String etag = etags.get(i++);
      ObjectNode partObj = newObject();
      partObj.put("partNumber", partNum);
      partObj.put("etag", etag);
      parts.add(partObj);
    }
    return successfulRequest()
        .content(request)
        .contentType(ContentType.JSON)
        .queryParam("uploadId", uploadId)
        .post(getFileUrl(stagingUrl, path))
        .getHeader("E-Tag");
  }
}
