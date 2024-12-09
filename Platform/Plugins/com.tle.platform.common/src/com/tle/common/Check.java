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

package com.tle.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("nls")
public final class Check {
  public static UUID checkValidUuid(String uuid) {
    UUID jUuid = null;
    try {
      jUuid = UUID.fromString(uuid);
      if (!uuid.equals(jUuid.toString())) {
        jUuid = null;
      }
    } catch (Exception e) {
      jUuid = null;
    }
    if (jUuid == null) {
      throw new IllegalArgumentException("Invalid UUID '" + uuid + "'");
    }
    return jUuid;
  }

  public static boolean isValidUuid(String uuid) {
    try {
      UUID jUuid = UUID.fromString(uuid);
      if (uuid.equals(jUuid.toString())) {
        return true;
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  public static void checkNotNull(Object... os) {
    for (Object o : os) {
      Preconditions.checkNotNull(o);
    }
  }

  public static void checkNotEmpty(String... ss) {
    for (String s : ss) {
      if (isEmpty(s)) {
        throw new IllegalArgumentException("Argument length must be greater than zero");
      }
    }
  }

  public static void checkNotNegative(Number... ns) {
    for (Number n : ns) {
      if (n.longValue() < 0) {
        throw new IllegalArgumentException("Argument must be zero or greater");
      }
    }
  }

  /**
   * Returns {@code true} if the string is {@code null} or is zero length <strong>after</strong>
   * trimming.
   *
   * @param s a possible string to test
   * @return true if empty, false if has length greater than zero after trimming.
   */
  public static boolean isEmpty(String s) {
    return s == null || s.trim().length() == 0;
  }

  public static boolean isEmpty(Collection<?> s) {
    return s == null || s.isEmpty();
  }

  public static <T> boolean isEmpty(T[] s) {
    return s == null || s.length == 0;
  }

  public static boolean isEmpty(Map<?, ?> s) {
    return s == null || s.isEmpty();
  }

  /**
   * Does an equality check, but checks for nulls on either the LHS or RHS at the same time. It is
   * true if both 'a' and 'b' are null, 'a == b', or 'a.equals(b)'
   */
  public static boolean bothNullOrDeepEqual(Object a, Object b) {
    if ((a == null) ^ (b == null)) {
      return false;
    } else {
      return a == b || deepEqual(a, b);
    }
  }

  /**
   * Does an equality check, but checks for nulls on either the LHS or RHS at the same time. It is
   * true if both 'a' and 'b' are null, 'a == b', or 'a.equals(b)'
   */
  public static boolean deepEqual(Object a, Object b) {
    return Arrays.equals(getSerializedForm(a), getSerializedForm(b));
  }

  private static byte[] getSerializedForm(Object o) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(o);
    } catch (IOException ex) {
      throw new RuntimeException("Error serializing object", ex);
    }
    return baos.toByteArray();
  }

  /**
   * Performs the standard checks, and then does a callback if necessary. See the following web page
   * for why this is like this: http://www.javaworld.com/javaworld/jw-06-2004/jw-0614-equals.html
   * Example: <code>
   * 	public boolean equals(Object obj)
   * 	{
   * 		return Check.commonEquals(this, obj, this);
   * 	}
   *
   *  public boolean checkFields(SomeClass rhs)
   *  {
   *  	return this.field.equals(rhs.field);
   *  }
   * </code>
   */
  public static <T extends FieldEquality<T>> boolean commonEquals(T a, Object b) {
    if (a == b) {
      // Reflexitivity
      return true;
    } else if (b == null) {
      // Non-null
      return false;
    } else if (a.getClass() != b.getClass()) {
      // Symmetry
      return false;
    } else {
      @SuppressWarnings("unchecked")
      T b2 = (T) b;
      return a.checkFields(b2);
    }
  }

  /**
   * @deprecated Use Arrays.hashCode() instead.
   */
  @Deprecated
  public static int getHashCode(Object... fieldValues) {
    return Arrays.hashCode(fieldValues);
  }

  /**
   * @deprecated Use Strings.nullToEmpty() instead.
   */
  @Deprecated
  public static String nullToEmpty(String s) {
    return Strings.nullToEmpty(s);
  }

  private Check() {
    throw new Error("Do not invoke");
  }

  public interface FieldEquality<T> {
    boolean checkFields(T rhs);
  }
}
