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

package com.tle.core.ffmpeg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.services.FileSystemService;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Bind(FfmpegService.class)
@Singleton
@SuppressWarnings("nls")
public class FfmpegServiceImpl implements FfmpegService {
  private String ffmpegPath;
  private File ffmpegExe;
  private File ffprobeExe;
  @Inject private ObjectMapperService objectMapperService;
  @Inject private FileSystemService fileSystemService;

  final int MAX_VIDEO_DIMENSTIONS = 320;
  final int MIN_VIDEO_LENGTH = 5;
  final int TRANSCODE_LENGTH = 10;

  @Inject(optional = true)
  public void setFfmpegPath(@Named("ffmpeg.path") String ffmpegPath) {
    this.ffmpegPath = ffmpegPath.trim();
  }

  @PostConstruct
  public void afterPropertiesSet() throws Exception {
    if (ffmpegPath != null) {
      final File ffmpegDir = new File(ffmpegPath);
      ffmpegExe = ExecUtils.findExe(ffmpegDir, "ffmpeg");
      ffprobeExe = ExecUtils.findExe(ffmpegDir, "ffprobe");
      if (ffprobeExe == null || ffmpegExe == null) {
        throw new RuntimeException(
            "FFmpeg was not found, specifically the ffmpeg and ffprobe programs.  The configured"
                + " path is "
                + ffmpegDir.getCanonicalPath());
      }
    }
  }

  @Override
  public void screenshotVideo(File srcFile, File dstFile) throws IOException {
    ObjectNode videoJson = getVideoInfo(srcFile);
    int videoDuration = getVideoDuration(videoJson);
    ExecResult result;
    // no duration in metadata, lets just take the first frame
    if (videoDuration == 0) {
      result =
          ExecUtils.exec(
              ffmpegExe.getAbsolutePath(),
              "-i",
              new String(srcFile.getAbsolutePath().getBytes("UTF-8")),
              "-b:v",
              "350k",
              "-q:v",
              "1",
              "-vframes",
              "1",
              new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
      result.ensureOk();
    } else if (videoDuration > MIN_VIDEO_LENGTH) {
      result =
          ExecUtils.exec(
              ffmpegExe.getAbsolutePath(),
              "-ss",
              "00:00:0" + MIN_VIDEO_LENGTH,
              "-i",
              new String(srcFile.getAbsolutePath().getBytes("UTF-8")),
              "-b:v",
              "350k",
              "-q:v",
              "1",
              "-vframes",
              "1",
              new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
      result.ensureOk();
    }
    // video is under 5 seconds, screenshot half way into video
    else {
      result =
          ExecUtils.exec(
              ffmpegExe.getAbsolutePath(),
              "-ss",
              "00:00:0" + videoDuration / 2,
              "-i",
              new String(srcFile.getAbsolutePath().getBytes("UTF-8")),
              "-b:v",
              "350k",
              "-q:v",
              "1",
              "-vframes",
              "1",
              new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
      result.ensureOk();
    }
  }

  @Override
  public void generatePreviewVideo(FileHandle handle, String filename) throws IOException {
    if (!isFfmpegInstalled()) {
      return;
    }
    List<String> opts = new ArrayList<String>();
    final String thumbFile = FileSystemService.VIDEO_PREVIEW_FOLDER + '/' + filename + ".mp4";
    final File srcFile = fileSystemService.getExternalFile(handle, filename);
    final File dstFile = fileSystemService.getExternalFile(handle, thumbFile);
    boolean madeDirs = dstFile.getParentFile().mkdirs();
    ObjectNode videoJson = getVideoInfo(srcFile);
    Dimension vidDimensions = getVideoDimensions(videoJson);
    int videoDuration = getVideoDuration(videoJson);

    if (!(madeDirs || dstFile.getParentFile().exists())) {
      throw new IOException(
          "Could not create/confirm directory " + dstFile.getParentFile().getAbsolutePath());
    }

    opts.add(ffmpegExe.getAbsolutePath());
    // skip 5 into the video, and capture 10 seconds. but only if it's long
    // enough
    if (videoDuration > MIN_VIDEO_LENGTH) {
      // video is long enough to capture 10 seconds from 5 seconds into
      // the
      // video
      if (videoDuration >= MIN_VIDEO_LENGTH + TRANSCODE_LENGTH) {
        opts.add("-ss");
        opts.add("00:00:0" + MIN_VIDEO_LENGTH);
      }
      // how long to transcode
      opts.add("-t");
      opts.add(Integer.toString(TRANSCODE_LENGTH));
    }
    opts.add("-i");
    opts.add(new String(srcFile.getAbsolutePath().getBytes("UTF-8")));
    // bitrate of transcoded video
    opts.add("-b:v");
    opts.add("350k");
    // audio sampling rate (experimental)
    opts.add("-ar");
    opts.add("44100");
    // audio codec
    opts.add("-acodec");
    opts.add("aac");
    // overwrite if file already exists
    opts.add("-y");
    if (vidDimensions.width > MAX_VIDEO_DIMENSTIONS
        || vidDimensions.height > MAX_VIDEO_DIMENSTIONS) {
      // scaling needful
      opts.add("-vf");
      if (vidDimensions.width > vidDimensions.height) {
        // wide video
        opts.add("scale=320:trunc\\(ow/a/2\\)*2");
      } else if (vidDimensions.height > vidDimensions.width) {
        // tall video
        opts.add("scale=trunc\\(oh*a/2\\)*2:320");
      } else {
        // it's hip to be a square
        opts.add("scale=320:320");
      }
    }
    opts.add(new String(dstFile.getAbsolutePath().getBytes("UTF-8")));
    ExecResult result = ExecUtils.exec(opts);
    result.ensureOk();
  }

  private int getVideoDuration(ObjectNode videoJson) {
    return videoJson.findValue("duration").asInt();
  }

  private Dimension getVideoDimensions(ObjectNode videoJson) {
    return new Dimension(
        videoJson.findValue("width").asInt(), videoJson.findValue("height").asInt());
  }

  @Override
  public ObjectNode getVideoInfo(File srcFile) throws IOException {
    final ObjectMapper mapper = objectMapperService.createObjectMapper();
    ObjectNode videoJson;
    ExecResult result =
        ExecUtils.exec(
            ffprobeExe.getAbsolutePath(),
            "-of",
            "json",
            "-show_streams",
            srcFile.getAbsolutePath());
    result.ensureOk();
    videoJson = (ObjectNode) mapper.readTree(result.getStdout());
    return videoJson;
  }

  @Override
  public boolean isFfmpegInstalled() {
    if (ffmpegPath != null) {
      return true;
    }
    return false;
  }
}
