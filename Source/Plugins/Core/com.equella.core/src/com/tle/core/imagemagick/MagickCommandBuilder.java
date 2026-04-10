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

import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for assembling and executing ImageMagick CLI commands.
 *
 * <p>This builder is purely responsible for constructing the correct CLI syntax. All business logic
 * (e.g. deciding <em>which</em> operations to apply) belongs in the calling service.
 */
class MagickCommandBuilder {
  /** Absolute path to the ImageMagick executable. */
  private final String exePath;

  /** Optional tool to run, placed immediately after the executable (e.g., "identify"). */
  private String subCommand;

  /**
   * Flags applied strictly before reading the image to optimize memory or decoding (e.g., -size).
   */
  private final List<String> inputOptions = new ArrayList<>();

  /** Path to the source image, optionally including a frame suffix like "[0]". */
  private String inputFile;

  /** Transformative operations applied to the image (e.g., -thumbnail, -crop, -gravity). */
  private final List<String> outputOptions = new ArrayList<>();

  /** Path to the destination file, or a pseudo-destination like "info:". */
  private String outputFile;

  /**
   * Creates a new builder initialised with the path to the {@code magick} executable.
   *
   * @param exePath absolute path to the {@code magick} binary
   */
  MagickCommandBuilder(String exePath) {
    this.exePath = exePath;
  }

  /** Sets a subcommand (e.g. {@code "identify"}) placed immediately after the executable. */
  MagickCommandBuilder subCommand(String subCommand) {
    this.subCommand = subCommand;
    return this;
  }

  /**
   * Appends a {@code -size} hint so ImageMagick can allocate memory efficiently before decoding the
   * source image.
   */
  MagickCommandBuilder sizeHint(ImageDimensions hint) {
    inputOptions.addAll(List.of("-size", hint.width() + "x" + hint.height()));
    return this;
  }

  /** Appends an option that must strictly appear before the input file. */
  MagickCommandBuilder inputOption(String flag, String param) {
    inputOptions.add(flag);
    if (param != null) {
      inputOptions.add(param);
    }
    return this;
  }

  /** Appends an option that takes no parameters. */
  MagickCommandBuilder inputOption(String flag) {
    return inputOption(flag, null);
  }

  /**
   * Sets the source file path.
   *
   * @param src the input image file
   */
  MagickCommandBuilder from(File src) {
    return from(src, "");
  }

  /** Sets the source file path with a frame selector suffix. */
  MagickCommandBuilder from(File src, String suffix) {
    this.inputFile = src.getAbsolutePath() + suffix;
    return this;
  }

  /** Appends a {@code -thumbnail} resize operation. */
  MagickCommandBuilder thumbnail(ImageDimensions size, ResizeOperator resize) {
    outputOptions.addAll(
        List.of("-thumbnail", size.width() + "x" + size.height() + resize.getOperator()));
    return this;
  }

  /** Appends a {@code -gravity} directive. */
  MagickCommandBuilder gravity(String gravity) {
    outputOptions.addAll(List.of("-gravity", gravity));
    return this;
  }

  /** Appends {@code -bordercolor} and {@code -border} directives. */
  MagickCommandBuilder border(String colour, String width) {
    outputOptions.addAll(List.of("-bordercolor", colour, "-border", width));
    return this;
  }

  /** Appends a {@code -crop} with structured dimensions and offset, followed by {@code +repage}. */
  MagickCommandBuilder crop(ImageDimensions size, Offset offset) {
    outputOptions.addAll(
        List.of(
            "-crop",
            size.width() + "x" + size.height() + "+" + offset.x() + "+" + offset.y(),
            "+repage"));
    return this;
  }

  /** Appends a {@code -crop} with a raw geometry string (e.g. {@code "200x150"}). */
  MagickCommandBuilder crop(String geometry) {
    outputOptions.addAll(List.of("-crop", geometry));
    return this;
  }

  /** Appends a {@code -sample} resize operation. */
  MagickCommandBuilder sample(String width, String height, ResizeOperator resize) {
    outputOptions.addAll(List.of("-sample", width + "x" + height + resize.getOperator()));
    return this;
  }

  /** Appends a {@code -rotate} operation. */
  MagickCommandBuilder rotate(int angle) {
    outputOptions.addAll(List.of("-rotate", String.valueOf(angle)));
    return this;
  }

  /** Appends a {@code -threshold} operation. */
  MagickCommandBuilder threshold(String value) {
    outputOptions.addAll(List.of("-threshold", value));
    return this;
  }

  /** Appends a {@code -format} output option. */
  MagickCommandBuilder format(String formatString) {
    outputOptions.addAll(List.of("-format", formatString));
    return this;
  }

  /** Appends arbitrary extra flags (e.g. additional options passed through from the caller). */
  MagickCommandBuilder rawOptions(String... options) {
    if (options != null) {
      outputOptions.addAll(Arrays.asList(options));
    }
    return this;
  }

  /**
   * Sets the destination file path.
   *
   * @param dst the output image file
   */
  MagickCommandBuilder to(File dst) {
    this.outputFile = dst.getAbsolutePath();
    return this;
  }

  /** Sets a pseudo-file output destination (e.g. {@code "info:"}). */
  MagickCommandBuilder to(String dst) {
    this.outputFile = dst;
    return this;
  }

  /**
   * Executes the assembled command.
   *
   * @return the {@link ExecResult} from the process execution
   */
  ExecResult exec() {
    return ExecUtils.exec(buildCommandList());
  }

  /** Executes the assembled command with a strict time limit. */
  ExecResult execWithTimeLimit(int timeoutSeconds) {
    List<String> finalCmd = buildCommandList();
    return ExecUtils.execWithTimeLimit(timeoutSeconds, finalCmd.toArray(new String[0]));
  }

  private List<String> buildCommandList() {
    validateState();

    List<String> cmd = new ArrayList<>();

    cmd.add(exePath);
    if (subCommand != null) cmd.add(subCommand);
    cmd.addAll(inputOptions);
    if (inputFile != null) cmd.add(inputFile);
    cmd.addAll(outputOptions);
    if (outputFile != null) cmd.add(outputFile);

    return cmd;
  }

  private void validateState() {
    if (subCommand == null) {
      if (inputFile == null)
        throw new IllegalStateException(
            "Cannot build magick command: An input file must be specified.");
      if (outputFile == null)
        throw new IllegalStateException(
            "Cannot build magick command: An output destination must be specified.");
    }
  }
}
