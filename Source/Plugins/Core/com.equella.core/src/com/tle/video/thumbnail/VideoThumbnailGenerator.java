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

package com.tle.video.thumbnail;

import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.ffmpeg.FfmpegService;
import com.tle.core.guice.Bind;
import com.tle.core.imagemagick.ImageMagickService;
import com.tle.core.imagemagick.ThumbnailOptions;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.thumbnail.ThumbnailGenerator;
import com.tle.core.workflow.thumbnail.ThumbnailType;
import java.awt.Dimension;
import java.io.File;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class VideoThumbnailGenerator implements ThumbnailGenerator {
  @Inject private FileSystemService fileSystemService;
  @Inject private ImageMagickService imageMagickService;
  @Inject private FfmpegService ffmpegService;

  @Override
  public void generateThumbnail(File src, File dest) throws Exception {
    if (ffmpegService.isFfmpegInstalled()) {
      final StagingFile stagingFile = new StagingFile(UUID.randomUUID().toString());
      final File temp =
          fileSystemService.getExternalFile(
              stagingFile, src.getName() + FileSystemService.THUMBNAIL_EXTENSION);
      try {
        fileSystemService.mkdir(stagingFile, null);
        ffmpegService.screenshotVideo(src, temp);
        imageMagickService.generateStandardThumbnail(temp, dest);
      } finally {
        fileSystemService.removeFile(stagingFile);
      }
    }
  }

  @Override
  public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options)
      throws Exception {
    if (ffmpegService.isFfmpegInstalled()) {
      final StagingFile stagingFile = new StagingFile(UUID.randomUUID().toString());
      final File temp =
          fileSystemService.getExternalFile(
              stagingFile, srcFile.getName() + FileSystemService.THUMBNAIL_EXTENSION);
      try {
        fileSystemService.mkdir(stagingFile, null);
        ffmpegService.screenshotVideo(srcFile, temp);
        imageMagickService.generateThumbnailAdvanced(temp, dstFile, options);
      } finally {
        fileSystemService.removeFile(stagingFile);
      }
    }
  }

  @Override
  public Dimension getImageDimensions(File srcFile) {
    return null;
  }

  @Override
  public boolean supportsThumbType(ThumbnailType type) {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return ffmpegService.isFfmpegInstalled();
  }
}
