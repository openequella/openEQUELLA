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

package com.tle.common.util;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("nls")
@NonNullByDefault
public class UtcDate implements TleDate {
  private static final long serialVersionUID = 1L;

  protected final long utc;
  @Nullable protected final String conceptualValue;
  protected final TimeZone zone;

  public UtcDate() {
    this(System.currentTimeMillis(), null, DateHelper.UTC_TIMEZONE);
  }

  public UtcDate(long utc) {
    this(utc, null, DateHelper.UTC_TIMEZONE);
  }

  public UtcDate(Date date) {
    this(date.getTime(), null, DateHelper.UTC_TIMEZONE);
  }

  public UtcDate(Calendar calendar) {
    this(calendar.getTime().getTime(), null, DateHelper.UTC_TIMEZONE);
  }

  /**
   * @param conceptualValue *Must* be in Dates.ISO_DATE_ONLY format
   */
  protected UtcDate(String conceptualValue) throws ParseException {
    this(0, conceptualValue, DateHelper.UTC_TIMEZONE);
    Dates.ISO_DATE_ONLY.parse(conceptualValue, DateHelper.UTC_TIMEZONE);
  }

  protected UtcDate(long utc, @Nullable String conceptualValue, TimeZone zone) {
    this.utc = utc;
    this.conceptualValue = conceptualValue;
    this.zone = zone;
  }

  /**
   * @param utcString This MUST be a UTC date.
   * @param dateFormat A specific date format to use to parse the string
   * @throws ParseException If the string cannot be parsed
   */
  public UtcDate(String utcString, Dates dateFormat) throws ParseException {
    this(
        dateFormat.parse(utcString, DateHelper.UTC_TIMEZONE).getTime(),
        null,
        DateHelper.UTC_TIMEZONE);
    if (dateFormat == Dates.ISO_DATE_ONLY) {
      throw new Error(
          "Use the UtcDate.conceptualDate(String) static method for 'conceptual' dates");
    }
  }

  /**
   * Milliseconds since 'epoch' in UTC time.
   *
   * @return The number of milliseconds since 1/1/1970 in UTC time.
   */
  @Override
  public long toLong() {
    if (conceptualValue != null) {
      try {
        return new UtcDate(
                Dates.ISO_DATE_ONLY.parse(conceptualValue, DateHelper.UTC_TIMEZONE).getTime())
            .toLong();
      } catch (ParseException pe) {
        throw new RuntimeException(pe);
      }
    }
    return utc;
  }

  /** A java Date initialised with UTC time. */
  @Override
  public Date toDate() {
    return new Date(toLong());
  }

  /** A ISO date representation of this date */
  @Override
  public String toString() {
    return (conceptualValue != null ? conceptualValue : format(Dates.ISO));
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UtcDate) {
      UtcDate otherUtc = (UtcDate) other;
      if (conceptualValue != null) {
        return (otherUtc.conceptualValue != null
            && otherUtc.conceptualValue.equals(conceptualValue));
      }
      return utc == ((UtcDate) other).utc;
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (conceptualValue != null) {
      return conceptualValue.hashCode();
    }
    return Long.valueOf(utc).hashCode();
  }

  @Override
  public String format(Dates dateFormat) {
    if (conceptualValue != null) {
      try {
        Date date = Dates.ISO_DATE_ONLY.parse(conceptualValue, zone);
        return dateFormat.format(date, zone);
      } catch (ParseException pe) {
        // Should never happen
        throw new AssertionError(pe);
      }
    }
    return dateFormat.format(toDate(), zone);
  }

  @Nullable
  @Override
  public String formatOrNull(Dates dateFormat) {
    return dateFormat.formatOrNull(toDate(), zone);
  }

  /** Comparable interface method */
  @Override
  public int compareTo(@SuppressWarnings("null") TleDate other) {
    if (toLong() == other.toLong()) {
      return 0;
    }
    return (toLong() > other.toLong() ? 1 : -1);
  }

  @Override
  public boolean isConceptual() {
    return conceptualValue != null;
  }

  @Nullable
  @Override
  public String getConceptualValue() {
    return conceptualValue;
  }

  @Override
  public boolean before(TleDate otherDate) {
    return compareTo(otherDate) < 0;
  }

  @Override
  public boolean after(TleDate otherDate) {
    return compareTo(otherDate) > 0;
  }

  @Override
  public TleDate toMidnight() {
    final Calendar c = Calendar.getInstance(zone);
    c.setTime(new Date(toLong()));

    final Calendar newCal = (Calendar) c.clone();
    newCal.set(Calendar.HOUR_OF_DAY, 0);
    newCal.set(Calendar.MINUTE, 0);
    newCal.set(Calendar.SECOND, 0);
    newCal.set(Calendar.MILLISECOND, 0);
    return new UtcDate(newCal.getTimeInMillis());
  }

  @Override
  public TimeZone getTimeZone() {
    return zone;
  }

  @Override
  public UtcDate addDays(int days) {
    Calendar cal = Calendar.getInstance(zone);
    cal.setTime(new Date(toLong()));
    cal.add(Calendar.DAY_OF_MONTH, days);
    return new UtcDate(cal.getTime());
  }

  public static UtcDate convertLocalMidnightToUtcMidnight(TleDate date, TimeZone zone) {
    // check date is actually local midnight
    LocalDate orig = new LocalDate(date.toLong(), zone);
    if (orig.compareTo(orig.toMidnight()) != 0) {
      throw new Error("A local midnight date was not supplied!");
    }
    return new UtcDate(orig.toLong() + zone.getOffset(orig.toLong()));
  }

  public static LocalDate convertUtcMidnightToLocalMidnight(TleDate date, TimeZone zone) {
    // check date is actually UTC midnight
    UtcDate orig = new UtcDate(date.toLong());
    if (orig.compareTo(orig.toMidnight()) != 0) {
      throw new Error("A UTC midnight date was not supplied!");
    }
    return new LocalDate(orig.toLong() - zone.getOffset(orig.toLong()), zone);
  }

  @Override
  public UtcDate conceptualDate() {
    try {
      return conceptualDate(format(Dates.ISO_DATE_ONLY));
    } catch (ParseException e) {
      // Not possible
      throw new AssertionError(e);
    }
  }

  /**
   * @param date ISO_DATE_ONLY format
   * @return an ISO representation of the date
   * @throws ParseException If the date cannot be parsed
   */
  public static UtcDate conceptualDate(String date) throws ParseException {
    return new UtcDate(date);
  }
}
