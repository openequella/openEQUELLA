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

package com.dytech.common.text;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This comparator implements sorting for strings that also contain numbers. For example, in your
 * normal string comparison the following would happen:
 *
 * <ul>
 *   <li>&apos;a&apos; comes before &apos;b&apos;
 *   <li>&apos;boy&apos; comes before &apos;girl&apos;
 *   <li>&apos;7&apos; comes before &apos;77&apos;
 *   <li>&apos;7&apos; comes before &apos;8&apos;
 *   <li>&apos;7&apos; comes before &apos;9&apos;
 *   <li><strong>&apos;10&apos; comes before &apos;7&apos;</strong>
 *       <ul>
 *         ...and this is where the problem occurs. This comparator gets around this though, by
 *         search through the string for sequences of digits, and sorts them based on their
 *         numerical value rather than character values.
 *         <p>The following examples will work with this comparator:
 *         <ul>
 *           <li>&apos;7&apos; comes before &apos;10&apos;
 *           <li>&apos;Student#: 435&apos; comes before &apos;Student#: 1025&apos;
 *           <li>&apos;Copy 5 of 30&apos; comes before &apos;Copy 6 of 30&apos;
 *           <li>&apos;Copy 52 of 5000&apos; comes before &apos;Copy 75 of 100&apos;
 *               <ul>
 *
 * @author Adam Eijdenberg
 * @author Nicholas Read
 */
public class RegexNumberStringComparator<T> implements Comparator<T>, Serializable {
  /** The regular expression pattern used to grab each group of digits or non-digits. */
  private static final Pattern PATTERN = Pattern.compile("\\d+|\\D+"); // $NON-NLS-1$

  /** Indicates if comparing alphabetical strings should be case insensitive. */
  private boolean caseInsensitive;

  /** Constructs a new case insensitive <code>NumberStringComparator</code>. */
  public RegexNumberStringComparator() {
    this(true);
  }

  /** Constructs a new <code>NumberStringComparator</code>. */
  public RegexNumberStringComparator(boolean caseInsensitive) {
    super();
    setCaseInsensitive(caseInsensitive);
  }

  /*
   * (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(T o1, T o2) {
    final String s1 = o1 == null ? null : convertToString(o1);
    final String s2 = o2 == null ? null : convertToString(o2);

    if (s1 == null) {
      if (s2 == null) {
        return 0;
      }
      return 1;
    } else if (s2 == null) {
      return -1;
    }

    final Matcher m1 = PATTERN.matcher(s1);
    final Matcher m2 = PATTERN.matcher(s2);

    boolean b1 = m1.find();
    boolean b2 = m2.find();
    while (b1 && b2) {
      final String g1 = m1.group();
      final String g2 = m2.group();

      int result = 0;
      try {
        // See if the grouping is a digit or not.
        final int n1 = Integer.parseInt(g1);
        final int n2 = Integer.parseInt(g2);

        result = n1 - n2;
      } catch (NumberFormatException e) {
        // They are not numbers, so fallback to string comparison
        if (isCaseInsensitive()) {
          result = g1.compareToIgnoreCase(g2);
        } else {
          result = g1.compareTo(g2);
        }
      }

      if (result != 0) {
        return result;
      }

      b1 = m1.find();
      b2 = m2.find();
    }

    // Check if one or the other has more tokens.
    if (!b1 && b2) {
      return -1;
    } else if (b1 && !b2) {
      return 1;
    } else {
      return 0;
    }
  }

  public String convertToString(T t) {
    return t.toString();
  }

  /**
   * @return Returns the caseInsensitive.
   */
  public boolean isCaseInsensitive() {
    return caseInsensitive;
  }

  /**
   * @param caseInsensitive The caseInsensitive to set.
   */
  public void setCaseInsensitive(boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }
}
