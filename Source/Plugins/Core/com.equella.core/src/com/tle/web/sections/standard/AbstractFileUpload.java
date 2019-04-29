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

package com.tle.web.sections.standard;

import com.google.common.base.Charsets;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.model.HtmlFileUploadState;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

public abstract class AbstractFileUpload<S extends HtmlFileUploadState>
    extends AbstractDisablerComponent<S> {

  public AbstractFileUpload(String defaultRenderer) {
    super(defaultRenderer);
  }

  public long getFileSize(SectionInfo info) {
    Part file = getMultipartFile(info);
    if (file != null) {
      return file.getSize();
    }
    return -1;
  }

  public InputStream getInputStream(SectionInfo info) {
    Part file = getMultipartFile(info);
    if (file != null) {
      try {
        return file.getInputStream();
      } catch (IOException e) {
        SectionUtils.throwRuntime(e);
      }
    }
    return null;
  }

  public String getMimeType(SectionInfo info) {
    Part file = getMultipartFile(info);
    if (file != null) {
      return file.getContentType();
    }
    return null;
  }

  public String getFilename(SectionInfo info) {
    Part file = getMultipartFile(info);
    if (file != null) {
      return new File(file.getSubmittedFileName()).getName();
    }
    return null;
  }

  public static boolean isMultipartRequest(HttpServletRequest request) {
    String contentType = request.getContentType();
    return contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/");
  }

  public Part getMultipartFile(SectionInfo info) {
    HttpServletRequest request = info.getRequest();
    if (!isMultipartRequest(request)) {
      if (request.getHeader("X_FILENAME") != null) {
        return new Part() {

          @Override
          public InputStream getInputStream() throws IOException {
            return request.getInputStream();
          }

          @Override
          public String getContentType() {
            return request.getContentType();
          }

          @Override
          public String getName() {
            String base64 = request.getHeader("X_FILENAME");
            return new String(Base64.getDecoder().decode(base64), Charsets.UTF_8);
          }

          @Override
          public String getSubmittedFileName() {
            return getName();
          }

          @Override
          public long getSize() {
            return request.getContentLength();
          }

          @Override
          public void write(String fileName) throws IOException {}

          @Override
          public void delete() throws IOException {}

          @Override
          public String getHeader(String name) {
            return null;
          }

          @Override
          public Collection<String> getHeaders(String name) {
            return Collections.emptyList();
          }

          @Override
          public Collection<String> getHeaderNames() {
            return Collections.emptyList();
          }
        };
      }
      return null;
    }
    try {
      return request.getPart(getSectionId());
    } catch (IOException | ServletException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param info
   * @param ajaxUploadUrl Typically generated thusly: BookmarkAndModify ajaxUploadUrl = new
   *     BookmarkAndModify(context, events.getNamedModifier("processUpload"));
   */
  public void setAjaxUploadUrl(SectionInfo info, Bookmark ajaxUploadUrl) {
    getState(info).setAjaxUploadUrl(ajaxUploadUrl);
  }

  public void setValidateFile(SectionInfo info, JSAssignable validateFunc) {
    getState(info).setValidateFile(validateFunc);
  }

  public void setAjaxAfterUpload(SectionInfo info, JSStatements after) {
    getState(info)
        .setValidateFile(
            new AnonymousFunction(
                new StatementBlock(Arrays.asList(after, new ReturnStatement(true)))));
  }
}
