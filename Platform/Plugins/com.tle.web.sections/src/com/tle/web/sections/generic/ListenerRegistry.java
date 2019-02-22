/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.generic;

import com.tle.common.Check;
import com.tle.web.sections.SectionId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ListenerRegistry {
  private final Map<ListenerKey, List<Object>> eventListeners =
      new HashMap<ListenerKey, List<Object>>();

  private static class ListenerKey {
    private final String target;
    private final Class<? extends EventListener> listenerClass;

    public ListenerKey(String target, Class<? extends EventListener> listenerClass) {
      this.target = target;
      this.listenerClass = listenerClass;
    }

    protected String getTarget() {
      return target;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (!(obj instanceof ListenerKey)) {
        return false;
      }

      ListenerKey key2 = (ListenerKey) obj;
      return Check.bothNullOrEqual(target, key2.target) && listenerClass.equals(key2.listenerClass);
    }

    @Override
    public int hashCode() {
      return Check.getHashCode(target, listenerClass);
    }

    @Override
    public String toString() {
      return (target != null ? target : "[All Targets]")
          + ':' //$NON-NLS-1$
          + (listenerClass != null
              ? listenerClass.getSimpleName()
              : "[All Classes]"); //$NON-NLS-1$
    }
  }

  public <T extends EventListener> void addListener(
      String target, Class<T> clazz, Object eventListener) {
    ListenerKey key = new ListenerKey(target, clazz);
    List<Object> listeners = eventListeners.get(key);
    if (listeners == null) {
      listeners = new ArrayList<Object>();
      eventListeners.put(key, listeners);
    }
    listeners.add(eventListener);
  }

  public <T extends EventListener> List<Object> getListeners(
      String target, Class<? extends T> clazz) {
    ListenerKey key = new ListenerKey(target, clazz);
    List<Object> listeners = eventListeners.get(key);
    if (listeners == null) {
      return Collections.emptyList();
    }
    return listeners;
  }

  public void removeListeners(String target) {
    List<ListenerKey> removal = new ArrayList<ListenerKey>();
    for (Entry<ListenerKey, List<Object>> entry : eventListeners.entrySet()) {
      final ListenerKey key = entry.getKey();

      String etarget = key.getTarget();
      if (etarget != null && etarget.equals(target)) {
        removal.add(key);
        continue;
      }

      final Iterator<Object> listenersIt = entry.getValue().iterator();

      while (listenersIt.hasNext()) {
        Object listener = listenersIt.next();
        if (listener instanceof String) {
          if (listener.equals(target)) {
            listenersIt.remove();
          }
        } else if (listener instanceof SectionId) {
          String sectionId = ((SectionId) listener).getSectionId();
          if (sectionId != null && sectionId.equals(target)) {
            listenersIt.remove();
          }
        }
      }
    }
    for (ListenerKey rem : removal) {
      eventListeners.remove(rem);
    }
  }
}
