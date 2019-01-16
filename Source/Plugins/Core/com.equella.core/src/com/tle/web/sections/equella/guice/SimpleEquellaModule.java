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

package com.tle.web.sections.equella.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;

public class SimpleEquellaModule extends AbstractModule {
  @Override
  protected void configure() {
    bindListener(
        Matchers.any(),
        new TypeListener() {
          @Override
          public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            PluginResourceHandler.inst().getForClass(type.getRawType());
          }
        });
  }
}
