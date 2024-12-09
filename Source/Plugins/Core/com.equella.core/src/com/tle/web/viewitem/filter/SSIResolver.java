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

package com.tle.web.viewitem.filter;

import com.google.common.io.ByteStreams;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.services.FileSystemService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.ssi.SSIExternalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSIResolver implements SSIExternalResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(SSIResolver.class);

  private FileSystemService fsys;
  private FileHandle handle;
  private String extrapath;
  private Map<String, String> vars = new HashMap<String, String>();

  public SSIResolver(FileSystemService fsys, FileHandle handle, String extrapath) {
    this.fsys = fsys;
    this.handle = handle;

    int slash = extrapath.lastIndexOf('/');
    if (slash > 0) {
      extrapath = extrapath.substring(0, slash + 1);
    }
    this.extrapath = extrapath;
  }

  @Override
  public void addVariableNames(@SuppressWarnings("rawtypes") Collection arg0) {
    LOGGER.info("addVariableNames():" + arg0); // $NON-NLS-1$
  }

  @Override
  public String getVariableValue(String name) {
    return vars.get(name);
  }

  @Override
  public void setVariableValue(String name, String value) {
    vars.put(name, value);
  }

  @Override
  public Date getCurrentDate() {
    return new Date();
  }

  private String getFileName(String fname) {
    fname = extrapath + fname;
    // LOGGER.info("File="+fname);
    return fname;
  }

  @Override
  public long getFileSize(String fname, boolean virtual) throws IOException {
    fname = getFileName(fname);

    return fsys.fileLength(handle, fname);
  }

  @Override
  public long getFileLastModified(String fname, boolean virtual) throws IOException {
    fname = getFileName(fname);

    return fsys.lastModified(handle, fname);
  }

  @Override
  public String getFileText(String fname, boolean virtual) throws IOException {
    fname = getFileName(fname);

    int length = (int) fsys.fileLength(handle, fname);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(length);

    try (InputStream in = fsys.read(handle, fname)) {
      ByteStreams.copy(in, baos);
    }

    return baos.toString("UTF-8"); // $NON-NLS-1$
  }

  @Override
  public void log(String msg, Throwable t) {
    LOGGER.info(msg, t);
  }
}
