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

import com.tle.common.Check;
import com.tle.common.util.ExecUtils.ExecResult;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encapsulates thumbnail-specific preparation, command assembly, and cleanup for ImageMagick
 * thumbnail generation.
 */
class ThumbnailCommandBuilder implements AutoCloseable {

  private static final Log LOGGER = LogFactory.getLog(ThumbnailCommandBuilder.class);

  /** Border width in pixels applied when a background colour is specified. */
  private static final String BORDER_WIDTH_PX = "50";

  private static final String TEMP_GIF_FRAME_SUFFIX = "-frame.gif";
  private static final String FIRST_GIF_FRAME_SELECTOR = "[0]";

  /**
   * Functional interface for reading image dimensions, allowing this class to remain decoupled from
   * {@link ImageMagickServiceImpl}.
   */
  @FunctionalInterface
  interface ImageDimensionsProvider {
    /**
     * Returns the pixel dimensions of the given image file.
     *
     * @param image the image file to measure
     * @return the image dimensions
     * @throws IOException if the dimensions cannot be read
     */
    Dimension getImageDimensions(File image) throws IOException;
  }

  private final Supplier<MagickCommandBuilder> builderFactory;
  private final ImageDimensionsProvider imageDimensionsProvider;

  private File srcFile;
  private File dstFile;
  private ThumbnailOptions options;
  private File preparedInput;

  /**
   * Creates a new thumbnail command builder.
   *
   * @param builderFactory supplier that creates fresh {@link MagickCommandBuilder} instances
   * @param imageDimensionsProvider function to read image dimensions via ImageMagick
   */
  ThumbnailCommandBuilder(
      Supplier<MagickCommandBuilder> builderFactory,
      ImageDimensionsProvider imageDimensionsProvider) {
    this.builderFactory = builderFactory;
    this.imageDimensionsProvider = imageDimensionsProvider;
  }

  /**
   * Sets the source image file.
   *
   * @param srcFile the source image
   * @return this builder for chaining
   */
  ThumbnailCommandBuilder input(File srcFile) {
    this.srcFile = srcFile;
    return this;
  }

  /**
   * Sets the destination thumbnail file.
   *
   * @param dstFile the output thumbnail file
   * @return this builder for chaining
   */
  ThumbnailCommandBuilder output(File dstFile) {
    this.dstFile = dstFile;
    return this;
  }

  /**
   * Sets the thumbnail options and performs all preparation steps: GIF frame extraction, dimension
   * reading, and parent directory creation. Requires {@link #input(File)} and {@link #output(File)}
   * to have been called first.
   *
   * @param options the thumbnail configuration
   * @return this builder for chaining
   * @throws IOException if dimension reading or directory creation fails
   */
  ThumbnailCommandBuilder withOptions(ThumbnailOptions options) throws IOException {
    this.options = options;
    this.preparedInput = handleGifFrame(srcFile);
    updateDimensions(preparedInput, options);
    ensureParentDirectoryExists(dstFile);
    return this;
  }

  /**
   * Assembles and executes the full ImageMagick thumbnail command.
   *
   * @return the {@link ExecResult} from the process execution
   */
  ExecResult execute() {
    return buildThumbnailCommand(preparedInput, dstFile, options).exec();
  }

  /**
   * Cleans up temporary resources. If a GIF frame was extracted, the temporary file is deleted.
   * Called automatically by try-with-resources.
   */
  @Override
  public void close() {
    if (preparedInput != null) {
      cleanupGifFrame(preparedInput);
    }
  }

  /**
   * If the source is a GIF, extracts its first frame to a temporary file and returns it. Otherwise
   * returns the original file unchanged.
   */
  private File handleGifFrame(File src) {
    if (!hasExtension(src, "gif")) {
      return src;
    }

    File frameFile = new File(src.getParent(), UUID.randomUUID() + TEMP_GIF_FRAME_SUFFIX);
    builderFactory.get().from(src, FIRST_GIF_FRAME_SELECTOR).to(frameFile).exec().ensureOk();
    return frameFile;
  }

  /** Deletes a temporary GIF frame file after thumbnailing is complete. */
  private void cleanupGifFrame(File file) {
    if (!file.getName().endsWith(TEMP_GIF_FRAME_SUFFIX)) {
      return;
    }
    if (!file.delete()) {
      LOGGER.warn("Unable to delete generated gif frame: " + file.getAbsolutePath());
    }
  }

  /**
   * Reads the actual dimensions of {@code imageFile} and stores them in {@code opts} so that the
   * command builder can make informed sizing decisions.
   */
  private void updateDimensions(File imageFile, ThumbnailOptions opts) throws IOException {
    Dimension imgDimensions = imageDimensionsProvider.getImageDimensions(imageFile);
    opts.setImgWidth(imgDimensions.width);
    opts.setImgHeight(imgDimensions.height);
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

  /** Assembles the full ImageMagick command for thumbnail generation. */
  private MagickCommandBuilder buildThumbnailCommand(File input, File dst, ThumbnailOptions opts) {
    MagickCommandBuilder builder = builderFactory.get();

    if (!opts.isNoSize()) {
      applySizeHint(builder, opts);
    }

    builder.from(input);

    if (!opts.isNoSize()) {
      applyThumbnailTransforms(builder, opts);
    }

    return builder.to(dst);
  }

  /** Calculates the optimal size hint for memory allocation and appends it to the builder. */
  private void applySizeHint(MagickCommandBuilder builder, ThumbnailOptions opts) {
    int w = opts.getImgWidth() == 0 ? opts.getWidth() * 2 : opts.getImgWidth();
    int h = opts.getImgHeight() == 0 ? opts.getHeight() * 2 : opts.getImgHeight();
    builder.sizeHint(ImageDimensions.pixels(w, h));
  }

  /** Appends resize, gravity, border and crop transforms. */
  private void applyThumbnailTransforms(MagickCommandBuilder builder, ThumbnailOptions opts) {
    ImageDimensions thumbSize = ImageDimensions.pixels(opts.getWidth(), opts.getHeight());
    builder.thumbnail(thumbSize, determineResizeOperator(opts));

    Optional.ofNullable(opts.getGravity()).ifPresent(builder::gravity);

    if (!Check.isEmpty(opts.getBackgroundColour())) {
      builder.border(opts.getBackgroundColour(), BORDER_WIDTH_PX);
    }

    if (opts.getCropWidth() > 0 && opts.getCropHeight() > 0) {
      builder
          .crop(
              ImageDimensions.pixels(opts.getCropWidth(), opts.getCropHeight()),
              new Offset(opts.getCropX(), opts.getCropY()))
          .repage();
    }
  }

  /** Determines the resize operator based on aspect-ratio and size constraints. */
  private ResizeOperator determineResizeOperator(ThumbnailOptions opts) {
    boolean isAlreadySmaller =
        opts.getImgWidth() < opts.getWidth() && opts.getImgHeight() < opts.getHeight();
    return (opts.isKeepAspect() || isAlreadySmaller)
        ? ResizeOperator.SHRINK_ONLY
        : ResizeOperator.FILL_AREA;
  }

  /** Checks whether the file has the given extension (case-insensitive). */
  private boolean hasExtension(File file, String ext) {
    return file.getName().toLowerCase().endsWith("." + ext.toLowerCase());
  }
}
