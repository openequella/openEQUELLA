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

/** Represents the width and height of an image with unit (pixel/percent) awareness. */
public record ImageDimensions(DimensionValue width, DimensionValue height) {

  /**
   * Creates pixel-based image dimensions.
   *
   * @param width the width in pixels
   * @param height the height in pixels
   * @return a new {@code ImageDimensions} with pixel units
   */
  public static ImageDimensions pixels(int width, int height) {
    return new ImageDimensions(DimensionValue.pixels(width), DimensionValue.pixels(height));
  }

  /**
   * Creates percentage-based image dimensions.
   *
   * @param width the width percentage
   * @param height the height percentage
   * @return a new {@code ImageDimensions} with percentage units
   */
  public static ImageDimensions percent(int width, int height) {
    return new ImageDimensions(DimensionValue.percent(width), DimensionValue.percent(height));
  }

  /**
   * Returns the ImageMagick geometry string (e.g. {@code "256x256"} or {@code "50%x50%"}).
   *
   * @return the geometry string
   */
  @Override
  public String toString() {
    return width + "x" + height;
  }
}
