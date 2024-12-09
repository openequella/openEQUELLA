/*
 * Created on Nov 5, 2004
 */
package com.dytech.common.text;

import junit.framework.TestCase;

public class NumberStringComparatorTest extends TestCase {
  NumberStringComparator<String> comparator;

  public NumberStringComparatorTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    comparator = new NumberStringComparator<String>();
  }

  @Override
  protected void tearDown() throws Exception {
    comparator = null;

    super.tearDown();
  }

  protected void assertFirstComesFirst(int i) {
    assertTrue(i < 0);
  }

  protected void assertSecondComesFirst(int i) {
    assertTrue(i > 0);
  }

  protected void assertEqual(int i) {
    assertTrue(i == 0);
  }

  public void testCompare() {
    int result = 0;

    result = comparator.compare("aaa", "bbb"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("aaa", "bbb"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("bbb", "aaa"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("aaa", "aaa"); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);

    result = comparator.compare("", "aaa"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("aaa", ""); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("", ""); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);

    result = comparator.compare("1", "a"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("a", "1"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("1", "1"); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);

    result = comparator.compare("0000005", "5"); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);

    result = comparator.compare("1", "2"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("2", "1"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("10", "1"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("10", "2"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("plan number 1", "plan number 2"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("plan number 100", "plan number 3"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result = comparator.compare("copy 5 of 100", "copy 6 of 10"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("copy 5 of 100", "copy 4 of 200"); // $NON-NLS-1$ //$NON-NLS-2$
    assertSecondComesFirst(result);

    result =
        comparator.compare(
            "this8is8not8same", "this8is8really8not8the8same"); // $NON-NLS-1$ //$NON-NLS-2$
    assertFirstComesFirst(result);

    result = comparator.compare("dragon52eggs", "dragon52eggs"); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);

    comparator.setCaseInsensitive(false);
    result =
        comparator.compare(
            "OnCe UpOn A tImE, jUsT 15 yEaRs AgO", //$NON-NLS-1$
            "once upon a time, just 15 years ago"); //$NON-NLS-1$
    assertFirstComesFirst(result);

    comparator.setCaseInsensitive(true);
    result =
        comparator.compare(
            "OnCe UpOn A tImE, jUsT 15 yEaRs AgO", //$NON-NLS-1$
            "once upon a time, just 15 years ago"); //$NON-NLS-1$
    assertEqual(result);

    result = comparator.compare("aaa", null); // $NON-NLS-1$
    assertFirstComesFirst(result);

    result = comparator.compare(null, "bbb"); // $NON-NLS-1$
    assertSecondComesFirst(result);

    result = comparator.compare(null, null);
    assertEqual(result);

    result =
        new NumberStringComparator<String>() {
          private static final long serialVersionUID = 1L;

          @Override
          public String convertToString(String t) {
            return null;
          }
        }.compare("a", "b"); // $NON-NLS-1$ //$NON-NLS-2$
    assertEqual(result);
  }
}
