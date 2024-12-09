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

package com.tle.common.gui.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.AbstractListModel;

public class GenericListModel<T> extends AbstractListModel<T> implements List<T> {
  private final List<T> delegate;

  public GenericListModel() {
    this(new ArrayList<T>());
  }

  public GenericListModel(List<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getSize() {
    return delegate.size();
  }

  @Override
  public T getElementAt(int index) {
    return delegate.get(index);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override
  public boolean add(T o) {
    int index = delegate.size();
    boolean changed = delegate.add(o);
    if (changed) {
      fireIntervalAdded(this, index, index);
    }
    return changed;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    int index = delegate.size();
    return addAll(index, c);
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override
  public boolean remove(Object o) {
    int index = delegate.indexOf(o);
    boolean found = index >= 0;
    if (found) {
      remove(index);
    }
    return found;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    for (Object obj : c) {
      changed = remove(obj) || changed;
    }
    return changed;
  }

  public boolean removeAll(Object[] objects) {
    boolean changed = false;
    for (Object obj : objects) {
      changed = remove(obj) || changed;
    }
    return changed;
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <U> U[] toArray(U[] a) {
    return delegate.toArray(a);
  }

  @Override
  public void clear() {
    if (!delegate.isEmpty()) {
      int last = delegate.size() - 1;
      delegate.clear();
      fireIntervalRemoved(this, 0, last);
    }
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    boolean changed = false;

    final int count = delegate.size();
    for (int i = count - 1; i >= 0; i--) {
      Object obj = delegate.get(i);
      if (!c.contains(obj)) {
        remove(obj);
      }
    }

    return changed;
  }

  @Override
  public Iterator<T> iterator() {
    return listIterator();
  }

  @Override
  public ListIterator<T> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<T> listIterator(final int startingIndex) {
    return new ListIterator<T>() {
      private ListIterator<T> delegateIter = delegate.listIterator(startingIndex);

      @Override
      public boolean hasNext() {
        return delegateIter.hasNext();
      }

      @Override
      public T next() {
        return delegateIter.next();
      }

      @Override
      public void remove() {
        int index = delegateIter.previousIndex() + 1;
        delegateIter.remove();
        fireIntervalRemoved(GenericListModel.this, index, index);
      }

      @Override
      public T previous() {
        return delegateIter.previous();
      }

      @Override
      public void add(T o) {
        int index = delegateIter.nextIndex();
        delegateIter.add(o);
        fireIntervalAdded(GenericListModel.this, index, index);
      }

      @Override
      public void set(T o) {
        int index = delegateIter.previousIndex() + 1;
        delegateIter.set(o);
        fireContentsChanged(GenericListModel.this, index, index);
      }

      @Override
      public boolean hasPrevious() {
        return delegateIter.hasPrevious();
      }

      /*
       * (non-Javadoc)
       * @see java.util.ListIterator#nextIndex()
       */
      @Override
      public int nextIndex() {
        return delegateIter.nextIndex();
      }

      @Override
      public int previousIndex() {
        return delegateIter.previousIndex();
      }
    };
  }

  @Override
  public void add(int index, T element) {
    delegate.add(index, element);
    fireIntervalAdded(this, index, index);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    if (c.isEmpty()) {
      return false;
    }

    boolean changed = delegate.addAll(index, c);
    if (changed) {
      int end = index + c.size() - 1;
      fireIntervalAdded(this, index, end);
    }
    return changed;
  }

  @Override
  public T get(int index) {
    return delegate.get(index);
  }

  @Override
  public T set(int index, T element) {
    T result = delegate.set(index, element);
    fireContentsChanged(this, index, index);
    return result;
  }

  @Override
  public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  @Override
  public T remove(int index) {
    T result = delegate.remove(index);
    fireIntervalRemoved(this, index, index);
    return result;
  }

  @Override
  public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return Collections.unmodifiableList(delegate.subList(fromIndex, toIndex));
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }
}
