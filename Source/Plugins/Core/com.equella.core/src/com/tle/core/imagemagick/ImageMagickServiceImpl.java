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

package com.tle.core.imagemagick;

import com.google.inject.name.Named;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.events.services.EventService;
import com.tle.core.guice.Bind;
import com.tle.core.healthcheck.listeners.ServiceCheckRequestListener;
import com.tle.core.healthcheck.listeners.ServiceCheckResponseListener.CheckServiceResponseEvent;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.ServiceName;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.Status;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.FileSystemService;
import com.tle.core.zookeeper.ZookeeperService;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Bind(ImageMagickService.class)
@Singleton
public class ImageMagickServiceImpl implements ImageMagickService, ServiceCheckRequestListener {
  private static final Log LOGGER = LogFactory.getLog(ImageMagickServiceImpl.class);
  private static final String KEY_PFX =
      AbstractPluginService.getMyPluginId(ImageMagickServiceImpl.class) + ".";

  /** Pattern for parsing output from {@code magick identify}. */
  private static final Pattern DIMENSIONS_PATTERN =
      Pattern.compile(".*?(\\d+)x(\\d+).*?", Pattern.DOTALL);

  /** Standard thumbnail dimensions. */
  private static final int STD_THUMB_WIDTH = 88;

  private static final int STD_THUMB_HEIGHT = 66;

  @Inject private FileSystemService fileSystem;
  @Inject private EventService eventService;
  @Inject private ZookeeperService zkService;

  @Inject
  @Named("thumbnailing.timeout")
  private int thumbnailingTimeout;

  private String imageMagickPath;
  private File magickExe;

  @Inject
  public void setImageMagickPath(@Named("imageMagick.path") String imageMagickPath) {
    this.imageMagickPath = imageMagickPath.trim();
  }

  @Override
  public void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options) {
    List<String> args = new ArrayList<String>();
    validateAgainstTimer(srcFile);
    srcFile = extractGifFrameIfNeeded(srcFile, args);

    try {
      Dimension imgDimensions = getImageDimensions(srcFile);
      options.setImgWidth(imgDimensions.width);
      options.setImgHeight(imgDimensions.height);

      args.add(magickExe.getAbsolutePath());
      ensureParentDirectoryExists(dstFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    appendSizeArgs(args, options);
    args.add(srcFile.getAbsolutePath());
    appendThumbnailArgs(args, options);
    args.add(dstFile.getAbsolutePath());

    ExecUtils.exec(args).ensureOk();

    cleanupGifFrame(srcFile, options);
    checkForBlankThumbnail(dstFile, options);
  }

  /**
   * Resolves and validates the {@code magick} executable on startup.
   *
   * @throws RuntimeException if the executable cannot be found
   */
  @PostConstruct
  public void afterPropertiesSet() throws Exception {
    final File imageMagicDir = new File(imageMagickPath);
    magickExe = ExecUtils.findExe(imageMagicDir, "magick");
    if (magickExe == null) {
      throw new RuntimeException(
          "ImageMagick was not found, specifically the 'magick' program. The configured path is "
              + imageMagicDir.getCanonicalPath());
    }
  }

  @Override
  public Dimension getImageDimensions(FileHandle handle, String filename) throws IOException {
    return getImageDimensions(fileSystem.getExternalFile(handle, filename));
  }

  @Override
  public Dimension getImageDimensions(File image) throws IOException {
    ExecResult result =
        ExecUtils.exec(
            magickExe.getAbsolutePath(), "identify", "-format", "%wx%h", toUtf8Path(image));
    result.ensureOk();

    Matcher m = DIMENSIONS_PATTERN.matcher(result.getStdout());
    if (m.matches()) {
      return new Dimension(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }
    throw new RuntimeException(
        "Output is not in expected format: "
            + (Check.isEmpty(result.getStderr()) ? result.getStdout() : result.getStderr()));
  }

  @Override
  public void sample(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-sample", src, dest, null, width, height, true, options);
  }

  @Override
  public void sampleNoRatio(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-sample", src, dest, null, width, height, false, options);
  }

  @Override
  public void crop(File src, File dest, String width, String height, String... options)
      throws IOException {
    operation("-crop", src, dest, null, width, height, true, options);
  }

  @Override
  public void rotate(File src, File dest, int angle, String... options) throws IOException {
    operation("-rotate", src, dest, Integer.toString(angle), null, null, true, options);
  }

  @Override
  public void generateStandardThumbnail(File srcFile, File dstFile) {
    ThumbnailOptions opts = new ThumbnailOptions();
    opts.setWidth(STD_THUMB_WIDTH);
    opts.setHeight(STD_THUMB_HEIGHT);
    opts.setCropWidth(STD_THUMB_WIDTH);
    opts.setCropHeight(STD_THUMB_HEIGHT);
    opts.setGravity("center");
    opts.setBackgroundColour("White");
    generateThumbnailAdvanced(srcFile, dstFile, opts);
  }

  @Override
  public boolean supported(String mimeType) {
    return mimeType.startsWith("image/") || mimeType.equals("windows/metafile");
  }

  @Override
  public void checkServiceRequest(CheckServiceRequestEvent request) {
    ServiceStatus status = new ServiceStatus(ServiceName.IMAGEMAGICK);
    try {
      ExecResult result = ExecUtils.exec(magickExe.getAbsolutePath(), "identify", "-version");
      if (!result.getStderr().isEmpty()) {
        status.setServiceStatus(Status.BAD);
        status.setMoreInfo(
            CurrentLocale.get(
                KEY_PFX + "imagemagick.servicecheck.moreinfo.problem",
                imageMagickPath,
                result.getStderr()));
      } else {
        status.setServiceStatus(Status.GOOD);
        status.setMoreInfo(
            CurrentLocale.get(
                KEY_PFX + "imagemagick.servicecheck.moreinfo",
                imageMagickPath,
                result.getStdout()));
      }
    } catch (Exception e) {
      status.setServiceStatus(Status.BAD);
      status.setMoreInfo(
          CurrentLocale.get(
              KEY_PFX + "imagemagick.servicecheck.moreinfo.problem",
              imageMagickPath,
              e.getMessage()));
    }
    eventService.publishApplicationEvent(
        new CheckServiceResponseEvent(request.getRequetserNodeId(), zkService.getNodeId(), status));
  }

  /**
   * Runs a single ImageMagick operation (e.g. sample, crop, rotate).
   *
   * @param op the ImageMagick operation flag
   * @param opParam optional parameter for the operation (e.g. rotation angle), or {@code null}
   * @param keepRatio if {@code false}, appends {@code !} to force exact dimensions
   */
  private void operation(
      String op,
      File src,
      File dest,
      String opParam,
      String width,
      String height,
      boolean keepRatio,
      String[] options) {
    List<String> args = new ArrayList<>();
    args.add(magickExe.getAbsolutePath());
    args.add(src.getAbsolutePath());
    args.add(op);
    if (opParam != null) {
      args.add(opParam);
    }
    if (width != null && height != null) {
      String dim = width + "x" + height;
      if (!keepRatio) {
        dim += "!";
      }
      args.add(dim);
    }
    if (options != null) {
      args.addAll(Arrays.asList(options));
    }
    args.add(dest.getAbsolutePath());
    ExecUtils.exec(args.toArray(new String[0])).ensureOk();
  }

  /**
   * Validates the image against a timed process to prevent indefinite thumbnailing on problematic
   * files. The timeout is configured via {@code thumbnail.timeout} in config.properties; defaults
   * to 20 seconds, or 0 to disable.
   */
  private void validateAgainstTimer(File image) {
    ExecResult result =
        ExecUtils.execWithTimeLimit(
            thumbnailingTimeout,
            new String[] {
              magickExe.getAbsolutePath(), "identify", "-format", "%wx%h", toUtf8Path(image)
            });
    result.ensureOk();
  }

  /**
   * If the source is a GIF, extracts its first frame to a temporary file and returns it. Otherwise
   * returns the original file unchanged.
   */
  private File extractGifFrameIfNeeded(File srcFile, List<String> args) {
    if (!srcFile.getAbsolutePath().endsWith(".gif")) {
      return srcFile;
    }
    String framePath = srcFile.getParent() + "\\frame.gif";
    args.add(magickExe.getAbsolutePath());
    args.add(srcFile.getAbsolutePath() + "[0]");
    args.add(framePath);
    ExecUtils.exec(args);
    args.clear();
    return new File(framePath);
  }

  /** Ensures the parent directory of {@code file} exists, creating it if necessary. */
  private void ensureParentDirectoryExists(File file) throws IOException {
    File parent = file.getParentFile();
    if (!parent.mkdirs() && !parent.exists()) {
      throw new IOException("Could not create/confirm directory " + parent.getAbsolutePath());
    }
  }

  /** Appends {@code -size} arguments when the options do not suppress sizing. */
  private void appendSizeArgs(List<String> args, ThumbnailOptions options) {
    if (options.isNoSize()) {
      return;
    }
    int sizeX = options.getImgWidth() == 0 ? options.getWidth() * 2 : options.getImgWidth();
    int sizeY = options.getImgHeight() == 0 ? options.getHeight() * 2 : options.getImgHeight();
    args.add("-size");
    args.add(sizeX + "x" + sizeY);
  }

  /** Appends thumbnail, gravity, border, and crop arguments based on {@code options}. */
  private void appendThumbnailArgs(List<String> args, ThumbnailOptions options) {
    if (options.isNoSize()) {
      return;
    }
    int thumbWidth = options.getWidth();
    int thumbHeight = options.getHeight();

    args.add("-thumbnail");
    boolean fitInside =
        options.isKeepAspect()
            || (options.getImgHeight() < thumbHeight && options.getImgWidth() < thumbWidth);
    args.add(thumbWidth + "x" + thumbHeight + (fitInside ? ">" : "^"));

    if (options.getGravity() != null) {
      args.add("-gravity");
      args.add(options.getGravity());
    }

    if (!Check.isEmpty(options.getBackgroundColour())) {
      args.add("-bordercolor");
      args.add(options.getBackgroundColour());
      args.add("-border");
      args.add("50");
    }

    int cropWidth = options.getCropWidth();
    int cropHeight = options.getCropHeight();
    if (cropWidth > 0 && cropHeight > 0) {
      args.add("-crop");
      args.add(cropWidth + "x" + cropHeight + "+" + options.getCropX() + "+" + options.getCropY());
      args.add("+repage");
    }
  }

  /** Deletes a temporary GIF frame file after thumbnailing is complete. */
  private void cleanupGifFrame(File srcFile, ThumbnailOptions options) {
    if (!srcFile.getAbsolutePath().endsWith("frame.gif")) {
      return;
    }
    if (!srcFile.delete()) {
      LOGGER.warn("Unable to delete generated gif frame: " + srcFile.getAbsolutePath());
    }
  }

  /**
   * Checks whether the generated thumbnail is blank (all white). If it is, the file is deleted.
   * Skipped when {@link ThumbnailOptions#isSkipBlankCheck()} is {@code true}.
   */
  private void checkForBlankThumbnail(File dstFile, ThumbnailOptions options) {
    if (options.isSkipBlankCheck()) {
      return;
    }
    ExecResult result =
        ExecUtils.exec(
            magickExe.getAbsolutePath(),
            dstFile.getAbsolutePath(),
            "-threshold",
            "99%",
            "-format",
            "\"%[fx:100*mean]\"",
            "info:");
    result.ensureOk();
    if (result.getStdout().contains("100")) {
      if (!dstFile.delete()) {
        LOGGER.warn("Unable to delete presumed blank thumbnail: " + dstFile.getAbsolutePath());
      }
    }
  }

  /** Returns the absolute path of a file, round-tripped through UTF-8 bytes. */
  private String toUtf8Path(File file) {
    return new String(
        file.getAbsolutePath().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
  }
}
