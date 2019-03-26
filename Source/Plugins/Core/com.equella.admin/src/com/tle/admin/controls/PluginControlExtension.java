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

package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.base.Throwables;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginService;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.PluginDescriptor;

public class PluginControlExtension implements ControlDefinition {

  private final PluginService pluginService;
  private final String id;
  private final String nameKey;
  private final Set<String> contexts;
  private final Extension extension;

  public PluginControlExtension(PluginService pluginService, Extension extension) {

    this.pluginService = pluginService;
    this.id = extension.getParameter("id").valueAsString();
    this.nameKey = extension.getParameter("name").valueAsString(); // $NON-NLS-1$
    this.extension = extension;

    Set<String> contexts = new HashSet<>();
    for (Parameter cparam : extension.getParameters("context")) // $NON-NLS-1$
    {
      contexts.add(cparam.valueAsString());
    }
    this.contexts = contexts;
  }

  @Override
  public Set<String> getContexts() {
    return contexts;
  }

  @Override
  public String getName() {
    return CurrentLocale.get(nameKey);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean hasContext(String context) {
    return contexts.contains(context);
  }

  @Override
  public String getIcon() {
    Parameter param = extension.getParameter("icon");
    return param != null ? param.valueAsString() : null;
  }

  @Override
  public Editor createEditor(Control control, int type, SchemaModel schema) {
    String editorClassName = extension.getParameter("editorClass").valueAsString(); // $NON-NLS-1$

    try {
      Class<?> editorClass =
          pluginService
              .getClassLoader(extension.getDeclaringPluginDescriptor())
              .loadClass(editorClassName);
      Constructor<?> cons = editorClass.getConstructor(WizardHelper.getEditorParamTypes());

      // Invoke the constructor for the new editor.
      Object[] params = {control, type, schema};

      return (Editor) cons.newInstance(params);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public EditorFactory editorFactory() {
    final Parameter param = extension.getParameter("factoryClass"); // $NON-NLS-1$
    final String editorClass =
        param != null ? param.valueAsString() : StandardEditorFactory.class.getName();
    return (EditorFactory)
        pluginService.getBean(extension.getDeclaringPluginDescriptor(), editorClass);
  }

  @Override
  public Control createControlModel() {

    PluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
    pluginService.ensureActivated(plugin);
    ClassLoader classLoader = pluginService.getClassLoader(plugin);
    try {
      Class<?> modelClass =
          classLoader.loadClass(extension.getParameter("modelClass").valueAsString());
      Control controlModel =
          (Control) modelClass.getConstructor(ControlDefinition.class).newInstance(this);
      return controlModel;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Object createWrappedObject() {
    Parameter modelParam = extension.getParameter("wrappedClass"); // $NON-NLS-1$
    PluginDescriptor descriptor = extension.getDeclaringPluginDescriptor();
    pluginService.ensureActivated(descriptor);
    ClassLoader classLoader = pluginService.getClassLoader(descriptor);
    try {
      Class<?> modelClass =
          classLoader.loadClass(
              modelParam != null ? modelParam.valueAsString() : CustomControl.class.getName());
      return modelClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
