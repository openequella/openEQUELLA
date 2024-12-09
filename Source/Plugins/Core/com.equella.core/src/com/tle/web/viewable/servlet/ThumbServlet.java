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

package com.tle.web.viewable.servlet;

import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.PathUtils;
import com.tle.common.Utils;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.FileSystemService;
import com.tle.web.mimetypes.service.WebMimeTypeService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.ViewableResource.ThumbRef;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import java.io.IOException;
import java.util.Collections;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet serves up thumbnails for attachments. The URL format is
 * /thumbs/{item-uuid}/{item-version}/{attach-uuid}.
 *
 * @author nick
 * @significantlybutcheredby aholland
 */
@NonNullByDefault
@Bind
@Singleton
@SuppressWarnings("nls")
public class ThumbServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  public static enum GalleryParameter {
    THUMBNAIL,
    PREVIEW,
    ORIGINAL;
  }

  @Inject private FileSystemService fileSystemService;
  @Inject private WebMimeTypeService mimeService;
  @Inject private ViewableItemFactory viewableFactory;
  @Inject private ItemService itemService;
  @Inject private AttachmentResourceService attachmentResourceService;
  @Inject private SectionsController sectionsController;
  @Inject private ContentStreamWriter contentStreamWriter;
  @Inject private InstitutionService institutionService;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    final String[] path = parsePath(request.getPathInfo());

    GalleryParameter gParam = null;
    final String gallery = request.getParameter("gallery");
    if (gallery != null) {
      try {
        gParam = GalleryParameter.valueOf(gallery.toUpperCase());
      } catch (IllegalArgumentException e) {
        // do nothing, gParam = null
      }
    }

    if ("$".equals(path[1])) {
      final StagingFile staging = new StagingFile(path[0]);
      // Note: this filename always contains an additional .jpeg extension
      final String filename = stripJpeg(path[2]);

      final String thumbPath = getThumbForPath(filename, gParam);
      if (fileSystemService.fileExists(staging, thumbPath)) {
        final FileContentStream stream =
            fileSystemService.getContentStream(staging, thumbPath, "image/jpeg");
        contentStreamWriter.outputStream(request, response, stream);
      } else {
        redirectToPlaceholder(request, response, gParam);
      }
    } else {
      final String uuid = path[0];
      final int version = Integer.parseInt(path[1]);
      final ItemId itemId =
          new ItemId(uuid, version == 0 ? itemService.getLiveItemVersion(uuid) : version);
      final ViewableItem<Item> vitem = viewableFactory.createNewViewableItem(itemId, version == 0);

      final ThumbRef thumb = getThumbRef(request, vitem, path[2], gParam);
      if (thumb == null) {
        response.sendRedirect(
            mimeService
                .getIconForEntry(mimeService.getEntryForMimeType("equella/item"))
                .toString());
        return;
      }

      if (thumb.isUsePlaceholder()) {
        redirectToPlaceholder(request, response, gParam);
      } else if (thumb.isUrl()) {
        response.sendRedirect(thumb.getUrl().toString());
      } else {
        final String mimeType = mimeService.getMimeTypeForFilename(thumb.getLocalFile());
        final FileContentStream stream =
            fileSystemService.getContentStream(thumb.getHandle(), thumb.getLocalFile(), mimeType);
        contentStreamWriter.outputStream(request, response, stream);
      }
    }
  }

  @Nullable
  private ThumbRef getThumbRef(
      HttpServletRequest request,
      ViewableItem<Item> vitem,
      @Nullable String attachmentUuid,
      @Nullable GalleryParameter gallery) {
    final SectionInfo info =
        sectionsController.createInfo(
            "/viewitem/viewitem.do",
            request,
            null,
            null,
            null,
            Collections.singletonMap(SectionInfo.KEY_FOR_URLS_ONLY, true));

    if (!Strings.isNullOrEmpty(attachmentUuid)) {
      return attachmentResourceService
          .getViewableResource(info, vitem, vitem.getAttachmentByUuid(attachmentUuid))
          .getThumbnailReference(info, gallery);
    }

    ViewableResource backup = null;
    final Item item = vitem.getItem();
    for (IAttachment a : item.getAttachmentsUnmodifiable()) {
      ViewableResource vr = attachmentResourceService.getViewableResource(info, vitem, a);

      // Keep hold of the first ViewableResource as a backup in case there
      // are no custom thumbs.
      if (backup == null) {
        backup = vr;
      }

      if (vr.isCustomThumb()) {
        return vr.getThumbnailReference(info, gallery);
      }
    }

    if (backup == null) {
      return null;
    }
    return backup.getThumbnailReference(info, gallery);
  }

  private void redirectToPlaceholder(
      HttpServletRequest request, HttpServletResponse response, @Nullable GalleryParameter gParam)
      throws IOException {
    final String path;
    if (gParam == null) {
      path = ThumbInProgressServlet.STANDARD;
    } else {
      switch (gParam) {
        case THUMBNAIL:
          path = ThumbInProgressServlet.GALLERY_THUMBNAIL;
          break;
        // ORIGINAL and PREVIEW won't ever apply here anyway
        default:
          path = ThumbInProgressServlet.GALLERY_THUMBNAIL;
      }
    }
    response.sendRedirect(institutionService.institutionalise("thumbprogress/" + path));
  }

  private String getThumbForPath(String filename, GalleryParameter gParam) {
    if (gParam == null) {
      return PathUtils.filePath(FileSystemService.THUMBS_FOLDER, filename)
          + FileSystemService.THUMBNAIL_EXTENSION;
    } else {
      switch (gParam) {
        case ORIGINAL:
          return filename;

        case PREVIEW:
          return PathUtils.filePath(FileSystemService.THUMBS_FOLDER, filename)
              + FileSystemService.GALLERY_PREVIEW_EXTENSION;

        case THUMBNAIL:
          return PathUtils.filePath(FileSystemService.THUMBS_FOLDER, filename)
              + FileSystemService.GALLERY_THUMBNAIL_EXTENSION;
      }
    }
    return null;
  }

  private String stripJpeg(String filename) {
    if (filename.endsWith(FileSystemService.THUMBNAIL_EXTENSION)) {
      return Utils.safeSubstring(filename, 0, -FileSystemService.THUMBNAIL_EXTENSION.length());
    }
    return filename;
  }

  /**
   * @return UUID, VERSION, ATTACHMENT UUID or STAGING, $, PATH
   */
  private String[] parsePath(String path) {
    final String[] parts = new String[3];

    if (path.length() <= 1) {
      throw new NotFoundException(path, true);
    }

    if (path.startsWith("/")) {
      path = Utils.safeSubstring(path, 1);
    }

    int slash = path.indexOf('/');
    if (slash < 0) {
      throw new NotFoundException(path, true);
    }
    // uuid
    parts[0] = path.substring(0, slash);
    path = Utils.safeSubstring(path, slash + 1);

    slash = path.indexOf('/');
    if (slash < 0) {
      throw new NotFoundException(path, true);
    }
    // version
    parts[1] = path.substring(0, slash);
    path = Utils.safeSubstring(path, slash + 1);

    // leftovers
    parts[2] = path;

    return parts;
  }
}
