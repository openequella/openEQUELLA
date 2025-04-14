/*
 * Created on Nov 5, 2004
 */
package com.dytech.common.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestCase;

public class Iter8Test extends TestCase {
  private final String value1 = "111";
  private final String value2 = "222";
  private final String value3 = "333";

  private Collection<String> collection;

  public Iter8Test() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    collection = new ArrayList<String>();
    collection.add(value1);
    collection.add(value2);
    collection.add(value3);
  }

  @Override
  protected void tearDown() throws Exception {
    collection = null;

    super.tearDown();
  }

  public void testCollectionConstructor() {
    commonTest(new Iter8<String>(collection));
  }

  public void testIteratorConstructor() {
    commonTest(new Iter8<String>(collection.iterator()));
  }

  public void testEnumerationConstructor() {
    commonTest(new Iter8<String>(Collections.enumeration(collection)));
  }

  public void testRemove() {
    Iter8<String> iter1 = new Iter8<String>(collection);
    iter1.next();
    iter1.remove();
    assertTrue(collection.size() == 2);

    Iter8<String> iter2 = new Iter8<String>(collection.iterator());
    iter2.next();
    iter2.remove();
    assertTrue(collection.size() == 1);

    Iter8<String> iter3 = new Iter8<String>(Collections.enumeration(collection));
    iter3.next();
    try {
      iter3.remove();
      assertTrue("Enumeration back-end should have thrown an UnsupportedOperationException", false);
    } catch (UnsupportedOperationException ex) {
      // This should always happen with Enumeration back-ends.
    }
  }

  private void commonTest(Iter8<String> iter) {
    assertEquals(iter.getCount(), 0);
    assertTrue(iter.hasNext());
    assertEquals(iter.next(), value1);

    assertEquals(iter.getCount(), 1);
    assertTrue(iter.hasNext());
    assertEquals(iter.next(), value2);

    assertEquals(iter.getCount(), 2);
    assertTrue(iter.hasNext());
    assertEquals(iter.next(), value3);

    assertEquals(iter.getCount(), 3);
    assertFalse(iter.hasNext());
  }
}
