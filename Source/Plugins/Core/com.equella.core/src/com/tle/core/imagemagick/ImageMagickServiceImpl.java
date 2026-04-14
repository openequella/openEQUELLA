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
import java.util.Optional;
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

  /** Border width in pixels applied when a background colour is specified. */
  private static final String BORDER_WIDTH_PX = "50";

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
    validateAgainstTimer(srcFile);
    File input = handleGifFrame(srcFile);

    try {
      updateDimensions(input, options);
      ensureParentDirectoryExists(dstFile);
      buildThumbnailCommand(input, dstFile, options).exec().ensureOk();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      cleanupGifFrame(input);
    }

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
        newBuilder().subCommand("identify").inputOption("-format", "%wx%h").from(image).exec();
    result.ensureOk();

    Matcher m = DIMENSIONS_PATTERN.matcher(result.getStdout());
    if (m.matches()) {
      return new Dimension(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
    }

    String output =
        Optional.of(result.getStderr()).filter(s -> !s.isEmpty()).orElse(result.getStdout());
    throw new RuntimeException("Output is not in expected format: " + output);
  }

  @Override
  public void sample(File src, File dest, String width, String height, String... options)
      throws IOException {
    newBuilder()
        .from(src)
        .sample(width, height, ResizeOperator.DEFAULT)
        .rawOptions(options)
        .to(dest)
        .exec()
        .ensureOk();
  }

  @Override
  public void sampleNoRatio(File src, File dest, String width, String height, String... options)
      throws IOException {
    newBuilder()
        .from(src)
        .sample(width, height, ResizeOperator.EXACT)
        .rawOptions(options)
        .to(dest)
        .exec()
        .ensureOk();
  }

  @Override
  public void crop(File src, File dest, String width, String height, String... options)
      throws IOException {
    newBuilder()
        .from(src)
        .crop(new ImageDimensions(Integer.parseInt(width), Integer.parseInt(height)))
        .rawOptions(options)
        .to(dest)
        .exec()
        .ensureOk();
  }

  @Override
  public void rotate(File src, File dest, int angle, String... options) throws IOException {
    newBuilder().from(src).rotate(angle).rawOptions(options).to(dest).exec().ensureOk();
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
      ExecResult result = newBuilder().subCommand("identify").inputOption("-version").exec();

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
   * Validates the image against a timed process to prevent indefinite thumbnailing on problematic
   * files. The timeout is configured via {@code thumbnail.timeout} in config.properties; defaults
   * to 20 seconds, or 0 to disable.
   */
  private void validateAgainstTimer(File image) {
    newBuilder()
        .subCommand("identify")
        .inputOption("-format", "%wx%h")
        .from(image)
        .execWithTimeLimit(thumbnailingTimeout)
        .ensureOk();
  }

  /**
   * If the source is a GIF, extracts its first frame to a temporary file and returns it. Otherwise
   * returns the original file unchanged.
   */
  private File handleGifFrame(File srcFile) {
    if (!hasExtension(srcFile, "gif")) {
      return srcFile;
    }

    File frameFile = new File(srcFile.getParent(), "frame.gif");
    newBuilder().from(srcFile, "[0]").to(frameFile).exec();
    return frameFile;
  }

  /**
   * Reads the actual dimensions of {@code imageFile} and stores them in {@code options} so that the
   * command builder can make informed sizing decisions.
   */
  private void updateDimensions(File imageFile, ThumbnailOptions options) throws IOException {
    Dimension imgDimensions = getImageDimensions(imageFile);
    options.setImgWidth(imgDimensions.width);
    options.setImgHeight(imgDimensions.height);
  }

  /** Ensures the parent directory of {@code file} exists, creating it if necessary. */
  private void ensureParentDirectoryExists(File file) throws IOException {
    File parent = file.getParentFile();
    boolean created = parent.mkdirs();
    boolean exists = parent.exists();
    if (!created && !exists) {
      throw new IOException(
          String.format(
              "Failed to create directory '%s'. Check filesystem permissions.",
              parent.getAbsolutePath()));
    }
  }

  /** Deletes a temporary GIF frame file after thumbnailing is complete. */
  private void cleanupGifFrame(File srcFile) {
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
        newBuilder().from(dstFile).threshold("99%").format("\"%[fx:100*mean]\"").to("info:").exec();

    result.ensureOk();
    if (result.getStdout().contains("100")) {
      if (!dstFile.delete()) {
        LOGGER.warn("Unable to delete presumed blank thumbnail: " + dstFile.getAbsolutePath());
      }
    }
  }

  /** Assembles the full ImageMagick command for thumbnail generation. */
  private MagickCommandBuilder buildThumbnailCommand(
      File input, File dstFile, ThumbnailOptions options) {
    MagickCommandBuilder builder = newBuilder();

    if (!options.isNoSize()) {
      applySizeHint(builder, options);
    }

    builder.from(input);

    if (!options.isNoSize()) {
      applyThumbnailTransforms(builder, options);
    }

    return builder.to(dstFile);
  }

  /** Calculates the optimal size hint for memory allocation and appends it to the builder. */
  private void applySizeHint(MagickCommandBuilder builder, ThumbnailOptions options) {
    int w = options.getImgWidth() == 0 ? options.getWidth() * 2 : options.getImgWidth();
    int h = options.getImgHeight() == 0 ? options.getHeight() * 2 : options.getImgHeight();
    builder.sizeHint(new ImageDimensions(w, h));
  }

  /** Appends resize, gravity, border and crop transforms. */
  private void applyThumbnailTransforms(MagickCommandBuilder builder, ThumbnailOptions options) {
    ImageDimensions thumbSize = new ImageDimensions(options.getWidth(), options.getHeight());
    builder.thumbnail(thumbSize, determineResizeOperator(options));

    Optional.ofNullable(options.getGravity()).ifPresent(builder::gravity);

    if (!Check.isEmpty(options.getBackgroundColour())) {
      builder.border(options.getBackgroundColour(), BORDER_WIDTH_PX);
    }

    if (options.getCropWidth() > 0 && options.getCropHeight() > 0) {
      builder.crop(
          new ImageDimensions(options.getCropWidth(), options.getCropHeight()),
          new Offset(options.getCropX(), options.getCropY()));
    }
  }

  private ResizeOperator determineResizeOperator(ThumbnailOptions options) {
    boolean isAlreadySmaller =
        options.getImgWidth() < options.getWidth() && options.getImgHeight() < options.getHeight();
    return (options.isKeepAspect() || isAlreadySmaller)
        ? ResizeOperator.SHRINK_ONLY
        : ResizeOperator.FILL_AREA;
  }

  private boolean hasExtension(File file, String ext) {
    return file.getName().toLowerCase().endsWith("." + ext.toLowerCase());
  }

  private String getMagickExePath() {
    return magickExe.getAbsolutePath();
  }

  private MagickCommandBuilder newBuilder() {
    return new MagickCommandBuilder(getMagickExePath());
  }
}
