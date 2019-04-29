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

package com.tle.core.guice;

import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.util.List;
import javax.inject.Inject;

public class ListProvider<T> implements Provider<List<T>> {
  @Inject private Injector injector;
  private Binder binder;

  private final List<Class<? extends T>> clazzes = Lists.newArrayList();

  public ListProvider(Binder binder) {
    this.binder = binder;
  }

  public ListProvider(Binder binder, List<Class<? extends T>> clazzes) {
    this.binder = binder;
    this.clazzes.addAll(clazzes);
    for (Class<? extends T> clazz : clazzes) {
      binder.bind(clazz);
    }
  }

  public void add(Class<? extends T> clazz) {
    binder.bind(clazz);
    clazzes.add(clazz);
  }

  @Override
  public List<T> get() {
    List<T> list = Lists.newArrayList();
    for (Class<? extends T> clazz : clazzes) {
      T instance = injector.getInstance(clazz);
      list.add(instance);
    }
    return list;
  }
}
