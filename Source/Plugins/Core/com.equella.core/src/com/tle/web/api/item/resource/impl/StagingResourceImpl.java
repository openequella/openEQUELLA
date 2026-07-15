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
import com.google.common.collect.Maps;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
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
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
@Bind(StagingResource.class)
@Singleton
public class StagingResourceImpl implements StagingResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(StagingResourceImpl.class);

  private static final String HEADER_EPS_STAGING_ID = "x-eps-stagingid";

  @Inject private MimeTypeService mimeService;
  @Inject private StagingService stagingService;
  @Inject private FileSystemService fileSystemService;
  @Inject private UrlLinkService urlLinkService;
  @Inject private ItemService itemService;
  @Inject private ItemFileService itemFileService;

  @Override
  public Response createStaging() {
    stagingService.checkStagingPrivileges();
    final StagingFile stagingFile = stagingService.createStagingArea();
    // Need compatibility with EPS endpoint :(
    return createdStagingResponse(stagingFile.getUuid());
  }

  /** The parameters shaping a staging listing, threaded through the recursive tree walk. */
  private record ListingContext(
      StagingFile stagingFile,
      String stagingUuid,
      @Nullable String scope,
      boolean includeFolders,
      boolean checksums) {

    /**
     * The path of {@code relativePath} from the staging area root: listing names are relative to
     * the scope, but filesystem access and links need the full path.
     */
    String storagePath(String relativePath) {
      return scope == null ? relativePath : PathUtils.filePath(scope, relativePath);
    }
  }

  @Override
  public StagingBean getStaging(
      UriInfo uriInfo, String stagingUuid, String path, boolean folders, boolean checksums) {
    stagingService.checkStagingPrivileges();
    StagingFile stagingFile = stagingService.getStagingFile(stagingUuid);
    final ListingContext context =
        new ListingContext(
            stagingFile, stagingUuid, StringUtils.stripToNull(path), folders, checksums);

    try {
      FileEntry base = fileSystemService.enumerateTree(stagingFile, context.scope(), null);
      List<BlobBean> blobs =
          base.getFiles().stream()
              .flatMap(entry -> blobBeans(context, entry, ""))
              .sorted(Comparator.comparing(BlobBean::getName, String.CASE_INSENSITIVE_ORDER))
              .toList();
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

  /**
   * Recursively flattens a file tree into blob beans: one bean per file, plus one per folder when
   * folder entries were requested (they are omitted by default, keeping the default response
   * unchanged).
   */
  private Stream<BlobBean> blobBeans(ListingContext context, FileEntry entry, String parentPath) {
    final String entryPath = PathUtils.filePath(parentPath, entry.getName());
    if (!entry.isFolder()) {
      return Stream.of(fileBean(context, entry, entryPath));
    }

    Stream<BlobBean> children =
        entry.getFiles().stream().flatMap(child -> blobBeans(context, child, entryPath));
    return context.includeFolders()
        ? Stream.concat(Stream.of(folderBean(entryPath)), children)
        : children;
  }

  private BlobBean folderBean(String path) {
    final BlobBean bean = new BlobBean();
    bean.setName(path);
    bean.setFolder(Boolean.TRUE);
    return bean;
  }

  private BlobBean fileBean(ListingContext context, FileEntry entry, String path) {
    final String storagePath = context.storagePath(path);
    final BlobBean bean = new BlobBean();
    bean.setName(path);
    bean.setSize(entry.getLength());
    bean.setContentType(mimeService.getMimeTypeForFilename(entry.getName()));
    if (context.checksums()) {
      md5Etag(context.stagingFile(), storagePath).ifPresent(bean::setEtag);
    }
    bean.set("links", Map.of("self", stagingUri(context.stagingUuid(), storagePath)));
    return bean;
  }

  private Optional<String> md5Etag(StagingFile stagingFile, String path) {
    try {
      return Optional.of(
          new EntityTag(fileSystemService.getMD5Checksum(stagingFile, path)).toString());
    } catch (IOException e) {
      LOGGER.debug("Unable to compute a checksum for staging file [{}]", path, e);
      return Optional.empty();
    }
  }

  @Override
  public Response headFile(String uuid, String filepath) {
    stagingService.checkStagingPrivileges();
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
  public Response getFile(Request request, HttpHeaders headers, String uuid, String filepath) {
    stagingService.checkStagingPrivileges();
    final StagingFile stagingFile = stagingService.getStagingFile(uuid);
    stagingService.ensureFileExists(uuid, filepath);

    try {
      RequestContext ctx = new RequestContext(request, headers, stagingFile, filepath);

      // Declarative validation chain: ETag check takes priority
      Optional<ResponseBuilder> preconditionResponse =
          checkEtagPrecondition(ctx).or(() -> checkModifiedSincePrecondition(ctx));

      if (preconditionResponse.isPresent()) {
        return preconditionResponse.get().build();
      }

      return makeResponseHeaders(uuid, filepath)
          .entity((StreamingOutput) output -> streamFileContent(stagingFile, filepath, output))
          .build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Response deleteFile(String stagingUuid, String filepath) {
    stagingService.checkStagingPrivileges();
    if (!stagingService.deleteFile(stagingUuid, filepath)) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response deleteStaging(String uuid) throws IOException {
    stagingService.checkStagingPrivileges();
    StagingFile stagingFile = stagingService.getStagingFile(uuid);
    stagingService.removeStagingArea(stagingFile, true);
    return Response.status(Status.NO_CONTENT).build();
  }

  @Override
  public Response completeMultipart(
      String uuid, String filepath, String uploadId, MultipartCompleteBean completion) {
    stagingService.checkStagingPrivileges();
    StagingFile stagingFile = stagingService.getStagingFile(uuid);
    String folderPath = multipartFolderPath(uploadId);

    stagingService.ensureFileExists(stagingFile, folderPath);
    processUploadParts(stagingFile, folderPath, filepath, completion.getParts());
    fileSystemService.removeFile(stagingFile, folderPath);

    return Response.ok().location(stagingUri(uuid, filepath)).build();
  }

  @Override
  public Response startMultipart(String uuid) {
    stagingService.checkStagingPrivileges();
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
    stagingService.checkStagingPrivileges();
    checkValidContentType(contentType);

    if (partNumber <= 0) {
      throw new BadRequestException("partNumber must be greater than 0.");
    }

    final StagingFile stagingFile = stagingService.getStagingFile(uuid);
    final String chunkPath =
        PathUtils.filePath(multipartFolderPath(uploadId), Integer.toString(partNumber));

    if (fileSystemService.fileExists(stagingFile, chunkPath)) {
      throw new BadRequestException("Part " + partNumber + " has already been uploaded.");
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

  sealed interface PutAction {}

  record CopyAction(String source) implements PutAction {}

  record UnzipAction(String destination) implements PutAction {}

  record WriteAction() implements PutAction {}

  @Override
  public Response putFile(
      Request request,
      HttpHeaders headers,
      String uuid,
      String filepath,
      InputStream data,
      String unzipTo,
      String copySource)
      throws IOException {
    String contentType = headers.getHeaderString(HttpHeaders.CONTENT_TYPE);
    String ifNoneMatch = headers.getHeaderString(HttpHeaders.IF_NONE_MATCH);

    stagingService.checkStagingPrivileges();
    checkValidContentType(contentType);

    final StagingFile stagingFile = stagingService.getStagingFile(uuid);

    if (StringUtils.isNotEmpty(ifNoneMatch)) {
      validateConditionalWriteRequest(request, stagingFile, filepath);
    }

    return switch (resolveAction(copySource, unzipTo)) {
      case CopyAction c -> handleCopy(stagingFile, c.source(), filepath, uuid);
      case UnzipAction u -> handleWriteAndUnzip(stagingFile, data, u.destination(), filepath, uuid);
      case WriteAction ignored -> handleWrite(stagingFile, data, filepath, uuid);
    };
  }

  @Override
  public Response createStagingFromItem(String itemUuid, int itemVersion) {
    stagingService.checkStagingPrivileges();
    validateCopyRequest(itemUuid, itemVersion);

    Item item = fetchExistingItem(itemUuid, itemVersion);
    stagingService.checkCopyPrivileges(item);

    ItemFile itemFile = fetchExistingItemFile(item);
    StagingFile stagingFile = stagingService.createStagingArea();

    fileSystemService.copy(itemFile, stagingFile);

    return createdStagingResponse(stagingFile.getUuid());
  }

  private PutAction resolveAction(String copySource, String unzipTo) {
    boolean isCopy = StringUtils.isNotEmpty(copySource);
    boolean isUnzip = StringUtils.isNotEmpty(unzipTo);

    if (isCopy && isUnzip) {
      throw new BadRequestException("copyfrom and unzipto cannot be used together.");
    }

    if (isCopy) {
      return new CopyAction(copySource);
    } else if (isUnzip) {
      return new UnzipAction(unzipTo);
    } else {
      return new WriteAction();
    }
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
    builder.tag(fileSystemService.getMD5Checksum(handle, filepath));
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

  private String multipartFolderPath(String uploadId) {
    return PathUtils.filePath("multipart", uploadId);
  }

  private Response handleCopy(
      StagingFile stagingFile, String copySource, String targetPath, String uuid)
      throws IOException {
    fileSystemService.copy(stagingFile, copySource, stagingFile, targetPath);
    return buildFileResponse(
        fileSystemService.getMD5Checksum(stagingFile, targetPath), uuid, targetPath);
  }

  private Response handleWrite(StagingFile stagingFile, InputStream data, String path, String uuid)
      throws IOException {
    try (data) {
      FileInfo info = fileSystemService.write(stagingFile, path, data, false, true);
      return buildFileResponse(info.getMd5CheckSum(), uuid, path);
    }
  }

  private Response handleWriteAndUnzip(
      StagingFile stagingFile, InputStream data, String dest, String path, String uuid)
      throws IOException {
    handleWrite(stagingFile, data, path, uuid);
    fileSystemService.mkdir(stagingFile, dest);
    FileInfo info = fileSystemService.unzipFile(stagingFile, path, dest);
    return buildFileResponse(info.getMd5CheckSum(), uuid, path);
  }

  private Response buildFileResponse(String md5, String uuid, String targetPath) {
    return Response.ok().tag(md5).location(stagingUri(uuid, targetPath)).build();
  }

  private Response buildChunkResponse(String md5) {
    return Response.ok().tag(md5).build();
  }

  private void processUploadParts(
      StagingFile file, String folder, String dest, List<PartBean> parts) {
    parts.stream()
        .sorted(Comparator.comparingInt(PartBean::getPartNumber))
        .map(part -> toMultipartChunk(part, folder))
        .forEach(chunk -> assembleChunk(file, chunk, dest));
  }

  private void assembleChunk(StagingFile file, MultipartChunk chunk, String dest) {
    try {
      stagingService.ensureFileExists(file, chunk.chunkPath());
      validatePartEtag(file, chunk);
      appendPartToFile(file, chunk, dest);
    } catch (IOException e) {
      LOGGER.error("Failed to validate chunk during multipart assembly", e);
      throw new InternalServerErrorException("An error occurred while assembling the file parts.");
    }
  }

  private MultipartChunk toMultipartChunk(PartBean partBean, String folderPath) {
    return new MultipartChunk(
        partBean.getPartNumber(),
        partBean.getEtag(),
        PathUtils.filePath(folderPath, Integer.toString(partBean.getPartNumber())));
  }

  private record MultipartChunk(int partNumber, String expectedEtag, String chunkPath) {}

  private void validatePartEtag(StagingFile stagingFile, MultipartChunk chunk) throws IOException {
    String expectedEtag = chunk.expectedEtag();
    if (StringUtils.isEmpty(expectedEtag)) {
      return;
    }

    String actualMd5 = fileSystemService.getMD5Checksum(stagingFile, chunk.chunkPath());
    String parsedEtag = EntityTag.valueOf(expectedEtag).getValue();
    if (!parsedEtag.equals(actualMd5)) {
      throw new BadRequestException(
          String.format(
              "ETag mismatch for part %s. Expected: %s, Actual: %s",
              chunk.partNumber(), parsedEtag, actualMd5));
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

  private void validateCopyRequest(String itemUuid, int itemVersion) {
    if (StringUtils.isEmpty(itemUuid)) {
      throw new BadRequestException("Item UUID is required");
    }
    if (itemVersion < 1) {
      throw new BadRequestException("Valid item version is required");
    }
  }

  private Item fetchExistingItem(String itemUuid, int itemVersion) {
    var itemId = new ItemId(itemUuid, itemVersion);
    return Optional.ofNullable(itemService.get(itemId))
        .orElseThrow(
            () -> {
              LOGGER.warn("Attempted to copy from non-existent item: {}", itemId);
              return new NotFoundException("Item not found");
            });
  }

  private ItemFile fetchExistingItemFile(Item item) {
    ItemFile itemFile = itemFileService.getItemFile(item);
    if (!fileSystemService.fileExists(itemFile)) {
      throw new NotFoundException("Item file not found");
    }
    return itemFile;
  }

  private Response createdStagingResponse(String stagingUuid) {
    return Response.created(stagingUri(stagingUuid))
        .header(HEADER_EPS_STAGING_ID, stagingUuid)
        .build();
  }

  private void validateConditionalWriteRequest(
      Request request, StagingFile stagingFile, String filepath) throws IOException {
    if (!fileSystemService.fileExists(stagingFile, filepath)) {
      return;
    }

    String fileMd5 = fileSystemService.getMD5Checksum(stagingFile, filepath);
    EntityTag existingFileEtag = new EntityTag(fileMd5);

    if (request.evaluatePreconditions(existingFileEtag) != null) {
      throw new WebApplicationException(
          String.format(
              "File '%s' already exists and violates the If-None-Match precondition.", filepath),
          Status.PRECONDITION_FAILED);
    }
  }

  private record RequestContext(
      Request request, HttpHeaders headers, StagingFile stagingFile, String filepath) {}

  @FunctionalInterface
  interface PreconditionEvaluator {
    ResponseBuilder evaluate() throws IOException;
  }

  private Optional<ResponseBuilder> checkPrecondition(
      RequestContext ctx, String headerName, PreconditionEvaluator evaluator) {
    if (StringUtils.isEmpty(ctx.headers().getHeaderString(headerName))) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(evaluator.evaluate());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Optional<ResponseBuilder> checkEtagPrecondition(RequestContext ctx) {
    return checkPrecondition(
        ctx,
        HttpHeaders.IF_NONE_MATCH,
        () -> {
          String md5 = fileSystemService.getMD5Checksum(ctx.stagingFile(), ctx.filepath());
          return ctx.request().evaluatePreconditions(new EntityTag(md5));
        });
  }

  private Optional<ResponseBuilder> checkModifiedSincePrecondition(RequestContext ctx) {
    return checkPrecondition(
        ctx,
        HttpHeaders.IF_MODIFIED_SINCE,
        () -> {
          Date lastModified =
              new Date(fileSystemService.lastModified(ctx.stagingFile(), ctx.filepath()));
          return ctx.request().evaluatePreconditions(lastModified);
        });
  }

  private void streamFileContent(StagingFile stagingFile, String filepath, OutputStream output)
      throws IOException {
    try (InputStream in = fileSystemService.read(stagingFile, filepath)) {
      in.transferTo(output);
    }
  }
}
