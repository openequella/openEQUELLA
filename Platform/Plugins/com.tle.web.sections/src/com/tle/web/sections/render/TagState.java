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

package com.tle.web.sections.render;

import com.tle.common.Check;
import com.tle.web.sections.events.js.HandlerMap;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a HTML tag.
 *
 * <p>Contains properties for adding inline styles and CSS classes, as well as JavaScript event
 * handlers. It also extends {@link AbstractWrappedElementId}, so can also have a dynamically
 * generated ID. By default it uses a {@link PageUniqueId}.
 *
 * @author jmaginnis
 */
public class TagState extends AbstractWrappedElementId {
  private Set<String> styleClasses;
  private String style;
  private Map<Object, Object> attrs;
  protected Map<String, String> data;
  private List<PreRenderable> preRenderables;
  private List<TagProcessor> processors;
  private final HandlerMap handlerMap = new HandlerMap();

  public TagState() {
    super(new PageUniqueId());
  }

  public TagState(String id) {
    super(new SimpleElementId(id));
    registerUse();
  }

  public TagState(ElementId id) {
    super(id);
  }

  public List<TagProcessor> getProcessors() {
    return processors;
  }

  public void addTagProcessor(TagProcessor processor) {
    if (processors == null) {
      processors = new ArrayList<TagProcessor>();
    }
    processors.add(processor);
  }

  public List<PreRenderable> getPreRenderables() {
    return preRenderables;
  }

  public void addPreRenderable(PreRenderable pr) {
    if (preRenderables == null) {
      preRenderables = new ArrayList<PreRenderable>();
    }
    preRenderables.add(pr);
  }

  public void setClickHandler(JSHandler handler) {
    setEventHandler(JSHandler.EVENT_CLICK, handler);
  }

  public void addEventStatements(String event, JSStatements... statements) {
    handlerMap.addEventStatements(event, statements);
  }

  public void setEventHandler(String event, JSHandler handler) {
    handlerMap.setEventHandler(event, handler);
  }

  public void setAttribute(Object key, Object attr) {
    if (attrs == null) {
      attrs = new HashMap<Object, Object>();
    }
    attrs.put(key, attr);
  }

  @SuppressWarnings("unchecked")
  public <T> T getAttribute(Object key) {
    if (attrs == null) {
      return null;
    }
    return (T) attrs.get(key);
  }

  /**
   * Used for data-xxx attributes. They should always be rendered.
   *
   * @param key
   * @param value
   */
  public void setData(String key, String value) {
    if (data == null) {
      data = new HashMap<String, String>();
    }
    data.put(key, value);
  }

  public String getData(String key) {
    if (data == null) {
      return null;
    }
    return data.get(key);
  }

  /**
   * Used for data-xxx attributes.
   *
   * @return
   */
  public Map<String, String> getData() {
    if (data != null) {
      return Collections.unmodifiableMap(data);
    }
    return null;
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public Set<String> getEventKeys() {
    return handlerMap.getEventKeys();
  }

  public JSHandler getHandler(String event) {
    return handlerMap.getHandler(event);
  }

  @SuppressWarnings("unchecked")
  public <T extends TagState> T addClass(String extraClass) {
    if (!Check.isEmpty(extraClass)) {
      if (styleClasses == null) {
        styleClasses = new HashSet<String>();
      }
      styleClasses.add(extraClass);
    }
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends TagState> T addClasses(String styleClass) {
    String[] classes = styleClass.split("\\s+"); // $NON-NLS-1$
    for (String clazz : classes) {
      addClass(clazz);
    }
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends TagState> T addClasses(Set<String> classes) {
    for (String clazz : classes) {
      addClass(clazz);
    }
    return (T) this;
  }

  public boolean isClassesSet() {
    return !Check.isEmpty(styleClasses);
  }

  public Set<String> getStyleClasses() {
    return styleClasses;
  }

  public void addReadyStatements(JSCallable callable, Object... args) {
    JSHandler handler = getHandler(JSHandler.EVENT_READY);
    if (handler == null) {
      setEventHandler(JSHandler.EVENT_READY, new StatementHandler(callable, args));
    } else {
      addEventStatements(JSHandler.EVENT_READY, new FunctionCallStatement(callable, args));
    }
  }

  public void addReadyStatements(JSStatements... statements) {
    addEventStatements(JSHandler.EVENT_READY, statements);
  }

  public HandlerMap getHandlerMap() {
    return handlerMap;
  }
}
