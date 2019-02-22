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

package com.tle.core.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.spi.Element;
import com.google.inject.spi.Elements;
import com.tle.core.guice.GuicePlugin.GuiceBeanLocator;
import com.tle.core.plugins.PluginBeanLocator;
import java.util.List;
import java.util.Set;

public class ExternalProviders extends AbstractModule {
  private final Iterable<Module> modules;
  private final List<PluginBeanLocator> locators;

  public ExternalProviders(List<PluginBeanLocator> locators, Iterable<Module> modules) {
    this.modules = modules;
    this.locators = locators;
  }

  @Override
  protected void configure() {
    binder().requireExplicitBindings();
    List<Element> elements = Elements.getElements(modules);
    ElementAnalyzer analyzer = new ElementAnalyzer(binder());
    for (Element element : elements) {
      element.acceptVisitor(analyzer);
    }
    analyzer.throwErrorIfNeeded();
    Set<Key<?>> external = analyzer.getExternalDependencies();
    for (Key<?> key : external) {
      bindExternal(key);
    }
  }

  private <T> void bindExternal(Key<T> key) {
    bind(key).toProvider(new ExternalProvider<T>(key));
  }

  public class ExternalProvider<T> implements Provider<T> {

    private Key<T> key;

    public ExternalProvider(Key<T> key) {
      this.key = key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
      for (PluginBeanLocator locator : locators) {
        T obj;
        if (locator instanceof GuiceBeanLocator) {
          obj = ((GuiceBeanLocator) locator).getBeanForKey(key);
        } else {
          obj = (T) locator.getBeanForType(key.getTypeLiteral().getRawType());
        }
        if (obj != null) {
          return obj;
        }
      }
      throw new RuntimeException("Couldn't find bean for type:" + key); // $NON-NLS-1$
    }
  }
}
