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

import com.tle.common.filesystem.handle.FileHandle;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

public interface ImageMagickService {
  /**
   * Returns whether the given MIME type is supported for image processing.
   *
   * @param mimeType the MIME type to check
   * @return {@code true} if supported
   */
  boolean supported(String mimeType);

  /**
   * Returns the pixel dimensions of an image identified by a file handle and filename.
   *
   * @param handle the file handle for the storage location
   * @param filename the image filename
   * @return the image dimensions
   * @throws IOException if the image cannot be read
   */
  Dimension getImageDimensions(FileHandle handle, String filename) throws IOException;

  /**
   * Returns the pixel dimensions of an image file.
   *
   * @param image the image file
   * @return the image dimensions
   * @throws IOException if the image cannot be read
   */
  Dimension getImageDimensions(File image) throws IOException;

  /**
   * Resamples an image to the given dimensions, preserving aspect ratio.
   *
   * @param src source image
   * @param dest destination image
   * @param size the target dimensions (supports both pixel and percentage units)
   * @param options additional ImageMagick options
   * @throws IOException on processing failure
   */
  void sample(File src, File dest, ImageDimensions size, String... options) throws IOException;

  /**
   * Crops an image to the given dimensions.
   *
   * @param src source image
   * @param dest destination image
   * @param size the crop dimensions
   * @param options additional ImageMagick options
   * @throws IOException on processing failure
   */
  void crop(File src, File dest, ImageDimensions size, String... options) throws IOException;

  /**
   * Crops an image to the given dimensions at a specific offset.
   *
   * @param src source image
   * @param dest destination image
   * @param size the crop dimensions
   * @param offset the x/y offset at which to begin the crop
   * @param options additional ImageMagick options
   * @throws IOException on processing failure
   */
  void crop(File src, File dest, ImageDimensions size, Offset offset, String... options)
      throws IOException;

  /**
   * Rotates an image by the given angle.
   *
   * @param src source image
   * @param dest destination image
   * @param angle rotation angle in degrees
   * @param options additional ImageMagick options
   * @throws IOException on processing failure
   */
  void rotate(File src, File dest, int angle, String... options) throws IOException;

  /**
   * Generates a thumbnail using the provided {@link ThumbnailOptions}.
   *
   * @param srcFile source image
   * @param dstFile destination thumbnail file
   * @param options thumbnail configuration
   */
  void generateThumbnailAdvanced(File srcFile, File dstFile, ThumbnailOptions options);

  /**
   * Generates a standard 88×66 thumbnail, cropped and centred.
   *
   * @param srcFile source image
   * @param dstFile destination thumbnail file
   */
  void generateStandardThumbnail(File srcFile, File dstFile);
}
