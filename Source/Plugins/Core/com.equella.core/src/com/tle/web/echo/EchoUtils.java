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

package com.tle.web.echo;

import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.controls.youtube.YoutubeUtils;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("nls")
public final class EchoUtils {
  public static final List<String> VIEWERS =
      Lists.newArrayList(
          "echoCenterViewer", "echoPlayerViewer", "echoVodcastViewer", "echoPodcastViewer");
  public static final String ATTACHMENT_TYPE = "echo";
  public static final String MIME_TYPE = "equella/attachment-echo";
  public static final String MIME_DESC = "Echo presentation";
  public static final String MIME_ICON_PATH = "icons/echo.png";

  public static final String PROPERTY_ECHO_DATA = "echoData";

  public static final String DEFAULT_SECURITY_REALM = "default";

  private static final PluginResourceHelper RESOURCES =
      ResourcesService.getResourceHelper(EchoUtils.class);

  // TODO refactor this out to be used elsewhere (eg. Youtube)
  public static String formatDuration(long duration) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime plus = now.plus(Duration.ofMillis(duration * 1000));
    Duration interval = Duration.between(now, plus);
    return YoutubeUtils.formatDuration(interval.toString());
  }

  private EchoUtils() {
    throw new Error();
  }

  public static String ds(String key) {
    return MessageFormat.format(" {0} ", CurrentLocale.get(RESOURCES.key("duration." + key)));
  }
}
