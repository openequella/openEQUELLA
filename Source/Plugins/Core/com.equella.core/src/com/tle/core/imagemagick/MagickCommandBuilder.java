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
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for assembling and executing ImageMagick CLI commands.
 *
 * <p>Encapsulates the strict argument ordering required by {@code magick}:
 *
 * <pre>
 *   magick [input-options] input-file [output-options] output-file
 * </pre>
 *
 * <p>See <a href="https://imagemagick.org/script/command-line-processing.php">ImageMagick CLI
 * docs</a>.
 *
 * <p>Example usage for thumbnail generation:
 *
 * <pre>{@code
 * new MagickCommandBuilder(magickExePath)
 *     .withSizeHint(opts)
 *     .from(srcFile)
 *     .withTransforms(opts)
 *     .to(dstFile)
 *     .exec()
 *     .ensureOk();
 * }</pre>
 *
 * <p>Example usage for a simple operation (sample, crop, rotate):
 *
 * <pre>{@code
 * new MagickCommandBuilder(magickExePath)
 *     .from(src)
 *     .operation("-sample", "200x150")
 *     .to(dest)
 *     .exec()
 *     .ensureOk();
 * }</pre>
 */
class MagickCommandBuilder {

  private static final String BORDER_WIDTH_PX = "50";

  private final List<String> cmd = new ArrayList<>();

  /**
   * Creates a new builder initialised with the path to the {@code magick} executable.
   *
   * @param exePath absolute path to the {@code magick} binary
   */
  MagickCommandBuilder(String exePath) {
    cmd.add(exePath);
  }

  /**
   * Appends a {@code -size} hint so ImageMagick can allocate memory efficiently before decoding the
   * source image. Skipped when {@link ThumbnailOptions#isNoSize()} is {@code true}.
   */
  MagickCommandBuilder withSizeHint(ThumbnailOptions opts) {
    if (!opts.isNoSize()) {
      int w = opts.getImgWidth() == 0 ? opts.getWidth() * 2 : opts.getImgWidth();
      int h = opts.getImgHeight() == 0 ? opts.getHeight() * 2 : opts.getImgHeight();
      cmd.addAll(List.of("-size", w + "x" + h));
    }
    return this;
  }

  /**
   * Appends the source file path.
   *
   * @param src the input image file
   */
  MagickCommandBuilder from(File src) {
    cmd.add(src.getAbsolutePath());
    return this;
  }

  /**
   * Appends thumbnail transform arguments: resize, gravity, border, and crop. Skipped when {@link
   * ThumbnailOptions#isNoSize()} is {@code true}.
   */
  MagickCommandBuilder withTransforms(ThumbnailOptions opts) {
    if (opts.isNoSize()) {
      return this;
    }

    int thumbWidth = opts.getWidth();
    int thumbHeight = opts.getHeight();

    boolean shrinkOnly =
        opts.isKeepAspect() || isAlreadySmallerThanTarget(opts, thumbWidth, thumbHeight);
    String resizeOperator = shrinkOnly ? ">" : "^";
    cmd.addAll(List.of("-thumbnail", thumbWidth + "x" + thumbHeight + resizeOperator));

    if (opts.getGravity() != null) {
      cmd.addAll(List.of("-gravity", opts.getGravity()));
    }

    if (!Check.isEmpty(opts.getBackgroundColour())) {
      cmd.addAll(List.of("-bordercolor", opts.getBackgroundColour(), "-border", BORDER_WIDTH_PX));
    }

    int cropWidth = opts.getCropWidth();
    int cropHeight = opts.getCropHeight();
    if (cropWidth > 0 && cropHeight > 0) {
      cmd.addAll(
          List.of(
              "-crop",
              cropWidth + "x" + cropHeight + "+" + opts.getCropX() + "+" + opts.getCropY(),
              "+repage"));
    }
    return this;
  }

  /**
   * Appends the destination file path.
   *
   * @param dst the output image file
   */
  MagickCommandBuilder to(File dst) {
    cmd.add(dst.getAbsolutePath());
    return this;
  }

  /**
   * Appends a generic ImageMagick operation flag and its arguments. Useful for operations like
   * {@code -sample}, {@code -crop}, {@code -rotate}, etc.
   *
   * @param op the operation flag (e.g. {@code "-sample"})
   * @param param the primary parameter for the operation, or {@code null} to skip
   * @param extra any additional arguments to append after the parameter
   */
  MagickCommandBuilder operation(String op, String param, String... extra) {
    cmd.add(op);
    if (param != null) {
      cmd.add(param);
    }
    if (extra != null) {
      cmd.addAll(Arrays.asList(extra));
    }
    return this;
  }

  /**
   * Executes the assembled command.
   *
   * @return the {@link ExecResult} from the process execution
   */
  ExecResult exec() {
    return ExecUtils.exec(cmd);
  }

  private boolean isAlreadySmallerThanTarget(
      ThumbnailOptions opts, int targetWidth, int targetHeight) {
    return opts.getImgHeight() < targetHeight && opts.getImgWidth() < targetWidth;
  }
}
