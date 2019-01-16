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

package com.tle.web.sections.standard.impl;

import java.lang.reflect.Constructor;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.standard.AbstractHtmlComponent;
import com.tle.web.sections.standard.AbstractRenderedComponent;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;

@Bind(ComponentFactory.class)
@Singleton
public class ComponentFactoryImpl implements ComponentFactory {
  private static final Log LOGGER = LogFactory.getLog(ComponentFactoryImpl.class);

  @Inject private RendererFactory renderFactory;

  @Override
  public Button createButton(String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, Button.class, true);
  }

  @Override
  public Link createLink(String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, Link.class, true);
  }

  @Override
  public MultiSelectionList<?> createMultiSelectionList(
      String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, MultiSelectionList.class, true);
  }

  @Override
  public SingleSelectionList<?> createSingleSelectionList(
      String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, SingleSelectionList.class, true);
  }

  @Override
  public TextField createTextField(String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, TextField.class, true);
  }

  @Override
  public Checkbox createCheckbox(String parentId, String id, SectionTree tree) {
    return createComponent(parentId, id, tree, Checkbox.class, true);
  }

  @Override
  public void registerComponent(
      String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component) {
    setupAndRegister(parentId, id, tree, component, true);
  }

  @Override
  public void setupComponent(
      String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component) {
    setupAndRegister(parentId, id, tree, component, false);
  }

  private void setupAndRegister(
      String parentId,
      String id,
      SectionTree tree,
      AbstractHtmlComponent<?> component,
      boolean register) {
    component.setPreferredId(tree.getSubId(parentId, id));
    if (component instanceof AbstractRenderedComponent<?>) {
      ((AbstractRenderedComponent<?>) component).setRenderFactory(renderFactory);
    }
    if (register) {
      tree.registerInnerSection(component, parentId);
    }
  }

  @Override
  @SuppressWarnings("nls")
  public <T extends AbstractHtmlComponent<?>> T createComponent(
      String parentId, String id, SectionTree tree, Class<T> clazz, boolean register) {
    try {
      Constructor<T> cons = clazz.getConstructor();
      T component = cons.newInstance();
      setupAndRegister(parentId, id, tree, component, register);
      return component;
    } catch (Exception ex) {
      LOGGER.error(
          "Error creating component " + id + " of type " + clazz + " in parent " + parentId);
      SectionUtils.throwRuntime(ex);
    }
    return null;
  }
}
