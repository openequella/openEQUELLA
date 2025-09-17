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

package com.tle.core.security.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginService;
import com.tle.core.security.impl.MethodSecurityInterceptor;
import com.tle.core.security.impl.SecurityAttributeSource;
import java.lang.reflect.Method;

public class SecurityModule extends AbstractModule {

  @Override
  protected void configure() {
    PluginService pluginService = AbstractPluginService.get();
    MethodSecurityInterceptor interceptor =
        pluginService
            .getBeanLocator(pluginService.getPluginIdForObject(getClass()))
            .getBeanForType(MethodSecurityInterceptor.class);
    final SecurityAttributeSource source = interceptor.getAttributeSource();
    final TargetClassMatcher targetClassMatcher = new TargetClassMatcher();
    bindInterceptor(
        targetClassMatcher,
        new AbstractMatcher<Method>() {
          @Override
          public boolean matches(Method t) {
            if (!t.isSynthetic()) {
              return source.getAttribute(t, targetClassMatcher.getTargetClass()) != null;
            }
            return false;
          }
        },
        interceptor);
  }

  public static class TargetClassMatcher extends AbstractMatcher<Class<?>> {

    private Class<?> targetClass;

    @Override
    public boolean matches(Class<?> t) {
      this.targetClass = t;
      return true;
    }

    public Class<?> getTargetClass() {
      return targetClass;
    }
  }
}
