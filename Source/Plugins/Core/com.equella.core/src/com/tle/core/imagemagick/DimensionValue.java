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

/**
 * A single dimension value with unit awareness, used as the building block for {@link
 * ImageDimensions}.
 *
 * @param value the numeric dimension value (must be non-negative)
 * @param unit the unit of measurement
 */
public record DimensionValue(int value, Unit unit) {

  /** The unit of measurement for a dimension value. */
  public enum Unit {
    /** Absolute pixel value. */
    PIXELS {
      @Override
      public String format(int value) {
        return Integer.toString(value);
      }
    },
    /** Relative percentage value. */
    PERCENT {
      @Override
      public String format(int value) {
        return value + "%";
      }
    };

    /** Formats the value for ImageMagick command-line arguments. */
    public abstract String format(int value);
  }

  /**
   * Validates that the dimension value is non-negative.
   *
   * @throws IllegalArgumentException if value is negative
   */
  public DimensionValue {
    if (value < 0) {
      throw new IllegalArgumentException("Dimension value must be non-negative, got: " + value);
    }
  }

  /**
   * Creates a pixel-based dimension value.
   *
   * @param value the pixel count
   * @return a new {@code DimensionValue} with {@link Unit#PIXELS}
   */
  public static DimensionValue pixels(int value) {
    return new DimensionValue(value, Unit.PIXELS);
  }

  /**
   * Creates a percentage-based dimension value.
   *
   * @param value the percentage (e.g. 50 for 50%)
   * @return a new {@code DimensionValue} with {@link Unit#PERCENT}
   */
  public static DimensionValue percent(int value) {
    return new DimensionValue(value, Unit.PERCENT);
  }

  /**
   * Returns the ImageMagick-compatible string representation.
   *
   * @return {@code "256"} for pixels, {@code "50%"} for percentages
   */
  @Override
  public String toString() {
    return unit.format(value);
  }
}
