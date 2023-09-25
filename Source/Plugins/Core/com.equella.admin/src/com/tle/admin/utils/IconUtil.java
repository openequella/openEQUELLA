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

package com.tle.admin.utils;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class IconUtil {
  private static final Log LOGGER = LogFactory.getLog(IconUtil.class);
  private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 72, 96, 128, 256};
  public static final List<Image> ICONS;

  static {
    ICONS =
        Arrays.stream(ICON_SIZES)
            .mapToObj(IconUtil::getIconURL)
            .map(
                icon -> {
                  try {
                    return ImageIO.read(icon);
                  } catch (IOException e) {
                    LOGGER.error("Failed to load icon: ", e);
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private static URL getIconURL(int dim) {
    String path = "/icons/windowicon." + dim + "x" + dim + "px.png";
    return Objects.requireNonNull(
        IconUtil.class.getResource(path), "Failed to get icon of: " + path);
  }

  private IconUtil() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
