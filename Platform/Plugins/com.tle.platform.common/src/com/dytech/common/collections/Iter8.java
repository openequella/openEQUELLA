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

package com.dytech.common.collections;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

// The purpose of this class is to wrap Enumeration instances, hence Sonar's
// warning about replacing Enumerations with Iterator is superfluous
public class Iter8<T> implements Iterator<T>, Enumeration<T>, Iterable<T> // NOSONAR
{
  private Iterator<T> iter;
  private int count;

  public Iter8(Collection<T> collection) {
    this(collection.iterator());
  }

  public Iter8(Iterator<T> iter) {
    this.iter = iter;
  }

  public Iter8(Enumeration<T> enumeration) {
    iter = new EnumerationWrapper<T>(enumeration);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Iterable#iterator()
   */
  @Override
  public Iterator<T> iterator() {
    return this;
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#remove()
   */
  @Override
  public void remove() {
    iter.remove();
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#hasNext()
   */
  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  /*
   * (non-Javadoc)
   * @see java.util.Iterator#next()
   */
  @Override
  public T next() {
    count++;
    return iter.next();
  }

  /*
   * (non-Javadoc)
   * @see java.util.Enumeration#hasMoreElements()
   */
  @Override
  public boolean hasMoreElements() {
    return hasNext();
  }

  /*
   * (non-Javadoc)
   * @see java.util.Enumeration#nextElement()
   */
  @Override
  public T nextElement() {
    return next();
  }

  /**
   * @return Returns the count.
   */
  public int getCount() {
    return count;
  }

  private static class EnumerationWrapper<T> implements Iterator<T> {
    private Enumeration<T> enumeration;

    public EnumerationWrapper(Enumeration<T> enumeration) {
      this.enumeration = enumeration;
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return enumeration.hasMoreElements();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public T next() {
      return enumeration.nextElement();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("Enumerations do not support remove");
    }
  }
}
