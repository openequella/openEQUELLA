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

package com.tle.core.institution.convert;

import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.common.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.services.FileSystemService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.log4j.Logger;

@Bind
@Singleton
public class JsonHelper {
  private static final Logger LOGGER = Logger.getLogger(JsonHelper.class);

  @Inject private FileSystemService fileSystemService;
  @Inject private ObjectMapperService objectMapperService;

  private ObjectMapper mapper;

  public synchronized ObjectMapper getMapper() {
    if (mapper == null) {
      mapper = objectMapperService.createObjectMapper();
    }

    return mapper;
  }

  public List<String> getFileList(final TemporaryFileHandle folder) {
    return fileSystemService.grep(folder, "", "*/*.json");
  }

  @SuppressWarnings("unchecked")
  public <O> O read(final TemporaryFileHandle file, String path, Class<O> type) {
    try (Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8)) {
      return getMapper().readValue(reader, type);
    } catch (IOException re) {
      LOGGER.error("Error reading: " + file.getAbsolutePath());
      throw new RuntimeException(re);
    }
  }

  public void write(TemporaryFileHandle file, String path, Object obj) {
    try (OutputStream outStream = fileSystemService.getOutputStream(file, path, false)) {
      getMapper().writeValue(outStream, obj);
    } catch (IOException ioe) {
      throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
    }
  }
}
