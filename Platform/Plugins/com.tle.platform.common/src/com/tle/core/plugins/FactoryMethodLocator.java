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

package com.tle.core.plugins;

import java.io.Serializable;

import org.apache.commons.beanutils.MethodUtils;

public class FactoryMethodLocator<T> implements BeanLocator<T> {
  private static final long serialVersionUID = 1L;

  private final String methodName;
  private final Serializable[] args;
  private final Class<?> clazz;

  public FactoryMethodLocator(Class<?> clazz, String methodName, Serializable... args) {
    this.clazz = clazz;
    this.methodName = methodName;
    this.args = args;
  }

  protected Object[] getArgs() {
    return args;
  }

  @SuppressWarnings({"unchecked", "nls"})
  protected <F> F getFactory() {
    PluginService pluginService = AbstractPluginService.get();
    String pluginId = pluginService.getPluginIdForObject(clazz);
    return (F) AbstractPluginService.get().getBean(pluginId, "bean:" + clazz.getName());
  }

  @Override
  public T get() {
    Object factory = getFactory();
    return invokeFactoryMethod(factory);
  }

  @SuppressWarnings("unchecked")
  protected T invokeFactoryMethod(Object factory) {
    try {
      return (T) MethodUtils.invokeMethod(factory, getMethodName(), getArgs());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String getMethodName() {
    return methodName;
  }
}
