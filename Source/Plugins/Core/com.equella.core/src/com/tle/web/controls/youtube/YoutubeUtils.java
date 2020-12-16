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

package com.tle.web.controls.youtube;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

@SuppressWarnings("nls")
public final class YoutubeUtils {
  public static final String ATTACHMENT_TYPE = "youtube";
  public static final String MIME_TYPE = "equella/attachment-youtube";

  public static final String PROPERTY_ID = "videoId";
  public static final String PROPERTY_TITLE = "title";
  public static final String PROPERTY_AUTHOR = "uploader";
  public static final String PROPERTY_DESCRIPTION = "description";
  public static final String PROPERTY_DATE = "uploaded";
  public static final String PROPERTY_PLAY_URL = "playUrl";
  public static final String PROPERTY_THUMB_URL = "thumbUrl";

  @Deprecated public static final String PROPERTY_TAGS = "tags";
  // ISO-8601 Duration string
  public static final String PROPERTY_DURATION = "duration";
  public static final String PROPERTY_PARAMETERS = "custom_parameters";

  // YouTube API V2 returned duration in seconds
  @Deprecated
  public static String formatDuration(long duration) {
    return formatDuration(Duration.ofSeconds(duration).toString());
  }

  public static String formatDuration(String duration) {
    Duration d = Duration.parse(duration);
    long hours = d.toHours();
    Duration minusHours = d.minus(Duration.ofHours(hours));
    long minutes = minusHours.toMinutes();
    long seconds = minusHours.minus(Duration.ofMinutes(minutes)).getSeconds();
    String format = hours > 0 ? "%3$d:%2$02d:%1$02d" : "%2$d:%1$02d";
    return String.format(format, seconds, minutes, hours);
  }

  /**
   * Parses a date string returned from the Youtube Data API V3's "modified" entry for a given
   * Youtube search result. Returns a date epoch long.
   *
   * @param date The string to parse (Expects it to be in ISO_DATE_TIME format)
   * @return a long containing the date represented as the number of milliseconds since midnight
   *     January 1, 1970.
   * @throws RuntimeException when the date cannot be parsed.
   */
  public static Optional<Long> parseDateModifiedToMillis(String date) {
    Optional<Long> parsedDate;
    try {
      parsedDate =
          Optional.of(
              ZonedDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
                  .toInstant()
                  .toEpochMilli());
    } catch (DateTimeParseException e) {
      throw new DateTimeParseException(
          "Unable to parse youtube date modified", date, e.getErrorIndex());
    }
    return parsedDate;
  }

  /**
   * Wrapper for parseDateModifiedToMillis which if present will return the date as a
   * java.util.Date.
   *
   * <p>Youtube Data API v3 returns ISO_DATE_TIME strings, but existing attachments returned from v2
   * will be stored as epoch millis, so this function supports both.
   *
   * @param date The date object to parse (Expects it to be a string in ISO_DATE_TIME format, or an
   *     epoch long)
   * @return The date represented as a java.util.Date.
   * @throws IllegalArgumentException if passed in Object is not an instance of long or string
   */
  public static Optional<Date> parseDateModifiedToDate(Object date) {
    Optional<Date> parsedDate = Optional.empty();
    if (date instanceof Long) {
      // if its a long, assume its an epoch long
      parsedDate = Optional.of(new Date((Long) date));
    } else if (date instanceof String) {
      // assume its an ISO_DATE_TIME string
      parsedDate =
          parseDateModifiedToMillis((String) date).flatMap(millis -> Optional.of(new Date(millis)));

    } else {
      throw new IllegalArgumentException(
          "Date object must be a long or a string. Unable to parse " + date + "as a date.");
    }
    return parsedDate;
  }

  private YoutubeUtils() {
    throw new Error();
  }
}
