package com.tle.webtests.framework;

import java.util.Objects;

public class Assert {

  public static void assertEquals(Object o1, Object o2) {
    assertEquals(o1, o2, null);
  }

  public static void assertEquals(Object o1, Object o2, String msg) {
    if (!Objects.equals(o1, o2)) {
      throw new AssertionError(
          msg != null
              ? msg
              : "Assertion failed: " + o1.toString() + " does not match: " + o2.toString());
    }
  }

  public static void assertTrue(boolean t) {
    assertEquals(true, t, null);
  }

  public static void assertTrue(boolean t, String msg) {
    assertEquals(true, t, msg);
  }

  public static void assertFalse(boolean t) {
    assertEquals(false, t, null);
  }
}
