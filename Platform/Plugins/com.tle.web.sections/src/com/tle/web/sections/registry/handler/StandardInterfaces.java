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

package com.tle.web.sections.registry.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.RegistrationHandler;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.BroadcastEventListener;
import com.tle.web.sections.events.RenderEventListener;
import com.tle.web.sections.events.SectionEventListener;
import com.tle.web.sections.events.TargetedEventListener;
import com.tle.web.sections.generic.AbstractSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.HtmlRendererListener;
import com.tle.web.sections.render.ModalListener;
import com.tle.web.sections.render.ModalRenderer;

/**
 * Handler for standard event listener interfaces.
 *
 * <p>This handler checks the {@link Section} class to see if it implements a number of standard
 * event listener interfaces, and registers them with the {@link SectionTree}.
 *
 * <p>The interfaces it listens for are:
 *
 * <ul>
 *   <li>{@link HtmlRenderer}
 *   <li>{@link com.tle.web.sections.events.ParametersEventListener}
 *   <li>{@link com.tle.web.sections.events.BookmarkEventListener}
 *   <li>{@link com.tle.web.sections.events.ForwardEventListener}
 *   <li>{@link ModalRenderer}
 * </ul>
 *
 * It also checks if the {@code Section} is a subclass of {@link AbstractSection} and if so, sets
 * the {@code Section} as an attribute on the tree, with the key being {@link
 * AbstractSection#getClassToRegister()}.
 *
 * @author jmaginnis
 */
@Bind
@Singleton
public class StandardInterfaces implements RegistrationHandler {
  private final Map<Class<?>, Collection<Class<? extends SectionEventListener>>> interfaceCache =
      new HashMap<Class<?>, Collection<Class<? extends SectionEventListener>>>();

  @Override
  public void registered(String id, SectionTree tree, Section section) {
    if (section instanceof HtmlRenderer) {
      tree.addListener(id, RenderEventListener.class, new HtmlRendererListener(id, section, tree));
    }
    Collection<Class<? extends SectionEventListener>> interfaces =
        getAllSectionEventListeners(section.getClass());
    for (Class<? extends SectionEventListener> clazz : interfaces) {
      if (BroadcastEventListener.class.isAssignableFrom(clazz)) {
        tree.addListener(null, clazz, id);
      }
      if (TargetedEventListener.class.isAssignableFrom(clazz)) {
        tree.addListener(id, clazz, id);
      }
    }
    if (section instanceof ModalRenderer) {
      ModalListener modalListener = new ModalListener(id, (ModalRenderer) section, tree);
      tree.addListener(id, RenderEventListener.class, modalListener);
      tree.addApplicationEvent(modalListener);
    }
  }

  private synchronized Collection<Class<? extends SectionEventListener>>
      getAllSectionEventListeners(Class<?> clazz) {
    if (clazz == null) {
      return Collections.emptyList();
    }
    Collection<Class<? extends SectionEventListener>> listeners = interfaceCache.get(clazz);
    if (listeners == null) {
      listeners = new HashSet<Class<? extends SectionEventListener>>();
      Class<?>[] interfaces = clazz.getInterfaces();
      for (Class<?> iClazz : interfaces) {
        listeners.addAll(getAllSectionEventListeners(iClazz));
      }
      listeners.addAll(getAllSectionEventListeners(clazz.getSuperclass()));
      if (SectionEventListener.class.isAssignableFrom(clazz)) {
        listeners.add(clazz.asSubclass(SectionEventListener.class));
      }
      interfaceCache.put(clazz, listeners);
    }
    return listeners;
  }

  @Override
  public void treeFinished(SectionTree tree) {
    // nothing
  }
}
