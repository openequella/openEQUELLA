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

package com.tle.web.scripting.objects;

import com.google.inject.assistedinject.Assisted;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.scripting.objects.ImagesScriptObject;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.services.FileSystemService;
import java.awt.*;
import java.io.IOException;
import javax.inject.Inject;

public class ImagesScriptWrapper extends AbstractScriptWrapper implements ImagesScriptObject {
  private static final long serialVersionUID = 1L;

  @Inject private ImageMagickService imageMagick;
  @Inject private FileSystemService fileSystem;

  private final FileHandle handle;

  @Inject
  protected ImagesScriptWrapper(@Assisted("handle") FileHandle handle) {
    this.handle = handle;
  }

  @Override
  public Dimension getDimensions(String path) throws IOException {
    return imageMagick.getImageDimensions(handle, path);
  }

  @Override
  public void resize(String path, int newWidth, int newHeight, String newPath) throws IOException {
    imageMagick.sample(
        fileSystem.getExternalFile(handle, path),
        fileSystem.getExternalFile(handle, newPath),
        Integer.toString(newWidth),
        Integer.toString(newHeight));
  }
}
