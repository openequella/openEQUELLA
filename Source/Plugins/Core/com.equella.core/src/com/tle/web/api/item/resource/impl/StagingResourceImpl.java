/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.item.resource.impl;

import com.dytech.edge.common.FileInfo;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.BlobBean;
import com.tle.web.api.staging.interfaces.StagingResource;
import com.tle.web.api.staging.interfaces.beans.MultipartBean;
import com.tle.web.api.staging.interfaces.beans.MultipartCompleteBean;
import com.tle.web.api.staging.interfaces.beans.PartBean;
import com.tle.web.api.staging.interfaces.beans.StagingBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.jboss.resteasy.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
@Bind(StagingResource.class)
@Singleton
public class StagingResourceImpl implements StagingResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(StagingResourceImpl.class);

  @Inject private MimeTypeService mimeService;
  @Inject private StagingService stagingService;
  @Inject private FileSystemService fileSystemService;
  @Inject private UrlLinkService urlLinkService;

  @Override
  public Response createStaging() {
    checkPermissions();
    final StagingFile stagingFile = stagingService.createStagingArea();
    // Need compatibility with EPS endpoint :(
    return Response.created(stagingUri(stagingFile.getUuid()))
        .header("x-eps-stagingid", stagingFile.getUuid())
        .build();
  }

  @Override
  public StagingBean getStaging(UriInfo uriInfo, String stagingUuid) {
    checkPermissions();
    StagingFile stagingFile = stagingService.getStagingFile(stagingUuid);

    try {
      FileEntry base = fileSystemService.enumerateTree(stagingFile, null, null);
      List<BlobBean> blobs = Lists.newArrayList();
      for (FileEntry fileEntry : base.getFiles()) {
        buildBlobBeans(stagingFile, stagingUuid, blobs, fileEntry, "");
      }
      Collections.sort(
          blobs,
          new Comparator<BlobBean>() {
            @Override
            public int compare(BlobBean o1, BlobBean o2) {
              return o1.getName().compareToIgnoreCase(o2.getName());
            }
          });
      URI directUrl = stagingUri(stagingUuid);

      StagingBean stagingBean = new StagingBean();
      stagingBean.setFiles(blobs);
      stagingBean.setUuid(stagingUuid);
      stagingBean.setDirectUrl(directUrl.toString());
      Map<String, URI> links = Maps.newLinkedHashMap();
      links.put("self", directUrl);
      stagingBean.set("links", links);
      return stagingBean;
    } catch (IOException e) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
  }

  private void buildBlobBeans(
      FileHandle fileHandle,
      String stagingUuid,
      List<BlobBean> blobs,
      FileEntry entry,
      String currentPath) {
    // Folders are not listed
    if (entry.isFolder()) {
      for (FileEntry subEntry : entry.getFiles()) {
        buildBlobBeans(
            fileHandle,
            stagingUuid,
            blobs,
            subEntry,
            PathUtils.filePath(currentPath, entry.getName()));
      }
    } else {
      final BlobBean blobBean = new BlobBean();
      final String filename = entry.getName();
      final String filePath = PathUtils.filePath(currentPath, filename);
      try {
        String md5CheckSum = fileSystemService.getMD5Checksum(fileHandle, filePath);
        blobBean.setEtag("\"" + md5CheckSum + "\"");
      } catch (IOException e) {
        // Whatever
      }
      blobBean.setName(filePath);
      blobBean.setSize(entry.getLength());
      blobBean.setContentType(mimeService.getMimeTypeForFilename(filename));
      final Map<String, URI> links = new HashMap<>();
      links.put(
          "self",
          urlLinkService
              .getMethodUriBuilder(StagingResource.class, "getFile")
              .build(stagingUuid, filePath));
      blobBean.set("links", links);
      blobs.add(blobBean);
    }
  }

  @Override
  public Response headFile(String uuid, String filepath) {
    checkPermissions();
    try {
      stagingService.ensureFileExists(uuid, filepath);
      FileInfo fileInfo = fileSystemService.getFileInfo(new StagingFile(uuid), filepath);
      if (fileInfo == null) {
        return Response.status(Status.NOT_FOUND).build();
      }
      return makeResponseHeaders(uuid, filepath).build();
    } catch (IOException io) {
      LOGGER.error("Error getting HEAD for file", io);
      return Response.serverError().build();
    }
  }

  @Override
  public Response getFile(HttpHeaders headers, String uuid, String filepath) {
    checkPermissions();
    final StagingFile stagingFile = stagingService.getStagingFile(uuid);
    stagingService.ensureFileExists(uuid, filepath);

    try {
      final String etag = headers.getHeaderString(HttpHeaders.IF_NONE_MATCH);
      if (etag != null) {
        String md5Checksum = fileSystemService.getMD5Checksum(stagingFile, filepath);
        String quotedChecksum = "\"" + md5Checksum + "\"";
        if (Objects.equals(etag, quotedChecksum)) {
          return Response.notModified(quotedChecksum).build();
        }
      }
      final String modifiedSince = headers.getHeaderString(HttpHeaders.IF_MODIFIED_SINCE);
      if (modifiedSince != null) {
        final Date lastModified = new Date(fileSystemService.lastModified(stagingFile, filepath));
        if (Objects.equals(modifiedSince, DateUtil.formatDate(lastModified))) {
          return Response.notModified().build();
        }
      }

      final InputStream input = fileSystemService.read(stagingFile, filepath);
      final ResponseBuilder responseBuilder = makeResponseHeaders(uuid, filepath);
      return responseBuilder
          .entity(
              new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                  try {
                    ByteStreams.copy(input, output);
                  } finally {
                    Closeables.close(input, false);
                  }
                }
              })
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Response deleteFile(String stagingUuid, String filepath) {
    checkPermissions();
    if (!stagingService.deleteFile(stagingUuid, filepath)) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response deleteStaging(String uuid) throws IOException {
    checkPermissions();
    StagingFile stagingFile = stagingService.getStagingFile(uuid);
    stagingService.removeStagingArea(stagingFile, true);
    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response completeMultipart(
      String uuid, String filepath, String uploadId, MultipartCompleteBean completion)
      throws IOException {
    checkPermissions();

    StagingFile stagingFile = stagingService.getStagingFile(uuid);
    String folderPath = multipartFolderPath(uploadId);

    stagingService.ensureFileExists(stagingFile, folderPath);

    List<PartBean> parts = Lists.newArrayList(completion.getParts());
    parts.sort(Comparator.comparingInt(PartBean::getPartNumber));

    for (PartBean partBean : parts) {
      MultipartChunk chunk =
          new MultipartChunk(
              partBean.getPartNumber(),
              partBean.getEtag(),
              PathUtils.filePath(folderPath, Integer.toString(partBean.getPartNumber())));

      stagingService.ensureFileExists(stagingFile, chunk.chunkPath());
      validatePartEtag(stagingFile, chunk);
      appendPartToFile(stagingFile, chunk, filepath);
    }

    fileSystemService.removeFile(stagingFile, folderPath);
    return Response.ok().location(stagingUri(uuid, filepath)).build();
  }

  @Override
  public Response startMultipart(String uuid) {
    checkPermissions();
    StagingFile stagingFile = stagingService.getStagingFile(uuid);
    String uploadId = UUID.randomUUID().toString();
    String folderPath = multipartFolderPath(uploadId);
    ensureMultipartDir(stagingFile);
    try {
      fileSystemService.mkdir(stagingFile, folderPath);
      return Response.status(Status.CREATED)
          .location(stagingUri(uuid))
          .entity(new MultipartBean(uploadId))
          .build();
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Response uploadChunk(
      String uuid, String uploadId, int partNumber, InputStream data, String contentType)
      throws IOException {
    checkPermissions();
    checkValidContentType(contentType);

    if (partNumber <= 0) {
      throw new BadRequestException("partNumber must be greater than 0.");
    }

    final StagingFile stagingFile = stagingService.getStagingFile(uuid);
    final String chunkPath =
        PathUtils.filePath(multipartFolderPath(uploadId), Integer.toString(partNumber));

    if (fileSystemService.fileExists(stagingFile, chunkPath)) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    try (InputStream stream = data) {
      FileInfo info = fileSystemService.write(stagingFile, chunkPath, stream, false, true);
      return buildChunkResponse(info.getMd5CheckSum());
    }
  }

  private void ensureMultipartDir(StagingFile handle) {
    try {
      if (!fileSystemService.fileExists(handle, "multipart")) {
        fileSystemService.mkdir(handle, "multipart");
      }
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public Response putFile(
      String uuid,
      String filepath,
      InputStream data,
      String unzipTo,
      String copySource,
      String contentType)
      throws IOException {
    checkPermissions();
    checkValidContentType(contentType);

    final boolean isCopy = !Strings.isNullOrEmpty(copySource);
    final boolean isUnzip = !Check.isEmpty(unzipTo);

    if (isCopy && isUnzip) {
      throw new BadRequestException(
          "copyfrom and unzipto cannot be used together. Copy the file first, then unzip it"
              + " separately.");
    }

    final StagingFile stagingFile = stagingService.getStagingFile(uuid);

    return isCopy
        ? handleCopy(stagingFile, copySource, filepath, uuid)
        : handleWrite(stagingFile, data, unzipTo, isUnzip, filepath, uuid);
  }

  private void checkValidContentType(String contentType) {
    if (contentType != null && contentType.startsWith("multipart/form-data")) {
      throw new BadRequestException(
          "Don't use multipart encoding to upload files, upload the file directly");
    }
  }

  private ResponseBuilder makeResponseHeaders(String uuid, String filepath) throws IOException {
    ResponseBuilder builder = Response.ok();
    StagingFile handle = new StagingFile(uuid);
    FileInfo fileInfo = fileSystemService.getFileInfo(handle, filepath);

    builder.lastModified(new Date(fileSystemService.lastModified(handle, filepath)));
    builder.header(HttpHeaders.CONTENT_LENGTH, fileInfo.getLength());
    builder.header(
        HttpHeaders.CONTENT_TYPE, mimeService.getMimeTypeForFilename(fileInfo.getFilename()));
    builder.header(
        HttpHeaders.ETAG, "\"" + fileSystemService.getMD5Checksum(handle, filepath) + "\"");
    return builder;
  }

  private URI stagingUri(String stagingUuid) {
    return urlLinkService
        .getMethodUriBuilder(StagingResource.class, "getStaging")
        .build(stagingUuid);
  }

  private URI stagingUri(String stagingUuid, String filepath) {
    return urlLinkService
        .getMethodUriBuilder(StagingResource.class, "getFile")
        .build(stagingUuid, filepath);
  }

  private void checkPermissions() {
    if (CurrentUser.isGuest()) {
      throw new AccessDeniedException("You need to be logged in to use a staging area.");
    }
  }

  private String multipartFolderPath(String uploadId) {
    return PathUtils.filePath("multipart", uploadId);
  }

  private Response handleCopy(
      StagingFile stagingFile, String copySource, String targetPath, String uuid)
      throws IOException {
    fileSystemService.copy(stagingFile, copySource, stagingFile, targetPath);
    String md5 = fileSystemService.getMD5Checksum(stagingFile, targetPath);
    return buildFileResponse(md5, uuid, targetPath);
  }

  private Response handleWrite(
      StagingFile stagingFile,
      InputStream data,
      String unzipTo,
      boolean isUnzip,
      String targetPath,
      String uuid)
      throws IOException {
    try (InputStream stream = data) {
      FileInfo info = fileSystemService.write(stagingFile, targetPath, stream, false, true);
      if (isUnzip) {
        fileSystemService.mkdir(stagingFile, unzipTo);
        info = fileSystemService.unzipFile(stagingFile, targetPath, unzipTo);
      }
      return buildFileResponse(info.getMd5CheckSum(), uuid, targetPath);
    }
  }

  private Response buildFileResponse(String md5, String uuid, String targetPath) {
    return Response.ok()
        .header(HttpHeaders.ETAG, "\"" + md5 + "\"")
        .location(stagingUri(uuid, targetPath))
        .build();
  }

  private Response buildChunkResponse(String md5) {
    return Response.ok().header(HttpHeaders.ETAG, "\"" + md5 + "\"").build();
  }

  private record MultipartChunk(int partNumber, String expectedEtag, String chunkPath) {}

  private void validatePartEtag(StagingFile stagingFile, MultipartChunk chunk) throws IOException {
    if (!Strings.isNullOrEmpty(chunk.expectedEtag())) {
      String actualMd5 = fileSystemService.getMD5Checksum(stagingFile, chunk.chunkPath());
      String unquotedEtag = chunk.expectedEtag().replace("\"", "");
      if (!unquotedEtag.equals(actualMd5)) {
        throw new BadRequestException(
            "ETag mismatch for part "
                + chunk.partNumber()
                + ". Expected: "
                + unquotedEtag
                + ", Actual: "
                + actualMd5);
      }
    }
  }

  private void appendPartToFile(StagingFile stagingFile, MultipartChunk chunk, String filepath) {
    try (InputStream chunkStream = fileSystemService.read(stagingFile, chunk.chunkPath())) {
      fileSystemService.write(stagingFile, filepath, chunkStream, true);
    } catch (IOException e) {
      LOGGER.error("Failed to append part {} to file {}", chunk.partNumber(), filepath, e);
      throw new WebApplicationException(
          "Failed to append multipart chunk: " + e.getMessage(), Status.INTERNAL_SERVER_ERROR);
    }
  }
}
