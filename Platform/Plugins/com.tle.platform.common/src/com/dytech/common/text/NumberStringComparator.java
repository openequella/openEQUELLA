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

/*
 * Created on Nov 5, 2004
 */

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

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
@NonNullByDefault
public class NumberStringComparator<T> implements Comparator<T>, Serializable {
  private static final long serialVersionUID = 1L;

  /** Indicates if comparing alphabetical strings should be case insensitive. */
  private boolean caseInsensitive;

  @Nullable private Map<T, String> cache;

  /** Constructs a new case insensitive <code>NumberStringComparator</code>. */
  public NumberStringComparator() {
    this(true);
  }

  /** Constructs a new <code>NumberStringComparator</code>. */
  public NumberStringComparator(boolean caseInsensitive) {
    this(caseInsensitive, false);
  }

  public NumberStringComparator(boolean caseInsensitive, boolean cacheStringConversions) {
    setCaseInsensitive(caseInsensitive);

    if (cacheStringConversions) {
      cache = new WeakHashMap<T, String>();
    }
  }

  /*
   * (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare(@Nullable T o1, @Nullable T o2) {
    final String s1 = o1 == null ? null : cachedConvertToString(o1);
    final String s2 = o2 == null ? null : cachedConvertToString(o2);

    if (s1 == null) {
      if (s2 == null) {
        return 0;
      }
      return 1;
    } else if (s2 == null) {
      return -1;
    }

    int i1 = 0;
    int i2 = 0;

    while (true) {
      char c1 = getChar(s1, i1);
      char c2 = getChar(s2, i2);

      if (Character.isDigit(c1) && Character.isDigit(c2)) {
        int n1 = 0;
        do {
          n1 *= 10;
          n1 += Character.digit(c1, 10);
          c1 = getChar(s1, ++i1);
        } while (Character.isDigit(c1));

        int n2 = 0;
        do {
          n2 *= 10;
          n2 += Character.digit(c2, 10);
          c2 = getChar(s2, ++i2);
        } while (Character.isDigit(c2));

        if (n1 != n2) {
          return n1 - n2;
        }
      } else {
        if (c1 != c2) {
          if (!caseInsensitive) {
            return c1 - c2;
          } else {
            // This is ripped straight out of the Sun String class
            c1 = Character.toUpperCase(c1);
            c2 = Character.toUpperCase(c2);
            if (c1 != c2) {
              c1 = Character.toLowerCase(c1);
              c2 = Character.toLowerCase(c2);
              if (c1 != c2) {
                return c1 - c2;
              }
            }
          }
        }

        i1++;
        i2++;
      }

      // Length check
      boolean hasMore1 = i1 < s1.length();
      boolean hasMore2 = i2 < s2.length();
      if (!hasMore1 && hasMore2) {
        return -1;
      } else if (hasMore1 && !hasMore2) {
        return 1;
      } else if (!hasMore1 && !hasMore2) {
        return 0;
      }
    }
  }

  private char getChar(String s, int i) {
    return i < s.length() ? s.charAt(i) : 0;
  }

  @Nullable
  private String cachedConvertToString(T t) {
    if (cache == null) {
      return convertToString(t);
    }

    String s = cache.get(t);
    if (s == null && !cache.containsKey(t)) {
      s = convertToString(t);
      cache.put(t, s);
    }
    return s;
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

  public boolean isCachingEnabled() {
    return cache != null;
  }

  public void setCachingEnabled(boolean enableCaching) {
    // Do not create new Map if caching already enabled.
    cache = enableCaching ? (cache == null ? new WeakHashMap<T, String>() : cache) : null;
  }
}
