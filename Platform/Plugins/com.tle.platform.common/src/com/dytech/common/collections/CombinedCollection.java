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
import java.util.Collections;
import java.util.Iterator;

public class CombinedCollection<T> implements Collection<T> {
  private final Collection<T> first;
  private final Collection<T> second;

  public CombinedCollection(Collection<T> first, Collection<T> second) {
    if (first != null) {
      this.first = first;
    } else {
      this.first = Collections.emptyList();
    }

    if (second != null) {
      this.second = second;
    } else {
      this.second = Collections.emptyList();
    }
  }

  @Override
  public int size() {
    return first.size() + second.size();
  }

  @Override
  public boolean isEmpty() {
    return first.isEmpty() && second.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return first.contains(o) || second.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      boolean isFirst = true;
      Iterator<? extends T> i = first.iterator();

      @Override
      public boolean hasNext() {
        ensureIter();
        return i.hasNext();
      }

      private void ensureIter() {
        if (isFirst && !i.hasNext()) {
          isFirst = false;
          i = second.iterator();
        }
      }

      @Override
      public T next() {
        ensureIter();
        return i.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  @Override
  public boolean containsAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> E[] toArray(E[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(T o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> coll) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
}
