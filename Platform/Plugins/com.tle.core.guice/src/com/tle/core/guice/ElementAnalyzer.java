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

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.spi.DefaultElementVisitor;
import com.google.inject.spi.Element;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.StaticInjectionRequest;
import java.util.HashSet;
import java.util.Set;

public class ElementAnalyzer extends DefaultElementVisitor<Void> {
  private Binder binder;
  private DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();
  private Set<Key<?>> localBindings = new HashSet<Key<?>>();

  public ElementAnalyzer(Binder binder) {
    this.binder = binder;
  }

  @Override
  public <T> Void visit(Binding<T> binding) {
    binding.acceptTargetVisitor(dependencyAnalyzer);
    localBindings.add(binding.getKey());
    binding.applyTo(binder);
    return null;
  }

  @Override
  public <T> Void visit(ProviderLookup<T> providerLookup) {
    dependencyAnalyzer.addDependency(providerLookup.getKey());
    providerLookup.applyTo(binder);
    return null;
  }

  @Override
  public Void visit(InjectionRequest<?> injectionRequest) {
    dependencyAnalyzer.analyzeInjectionPoints(injectionRequest.getInjectionPoints());
    injectionRequest.applyTo(binder);
    return null;
  }

  @Override
  public Void visit(StaticInjectionRequest staticInjectionRequest) {
    dependencyAnalyzer.analyzeInjectionPoints(staticInjectionRequest.getInjectionPoints());
    staticInjectionRequest.applyTo(binder);
    return null;
  }

  @Override
  protected Void visitOther(Element element) {
    element.applyTo(binder);
    return null;
  }

  public Set<Key<?>> getExternalDependencies() {
    Set<Key<?>> dependentKeys = dependencyAnalyzer.getDependentKeys();
    dependentKeys.removeAll(localBindings);
    return dependentKeys;
  }

  public void throwErrorIfNeeded() {
    dependencyAnalyzer.getErrors().throwCreationExceptionIfErrorsExist();
  }
}
