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

package com.tle.web.freemarker;

import com.tle.core.guice.Bind;
import com.tle.web.DebugSettings;
import freemarker.cache.NullCacheStorage;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import javax.inject.Singleton;

@Bind
@Singleton
public class BasicConfiguration extends Configuration {
  public BasicConfiguration() {
    setDateFormat("full");
    setTimeFormat("short");
    setDateTimeFormat("long_short");
    setLocalizedLookup(false);
    // --- Security hardening: prevent FreeMarker template injection (SSTI) -> RCE ---
    //
    // This configuration compiles template text that authenticated users can supply
    // (collection summary sections, advanced-script wizard controls, dashboard portlets,
    // MIME display templates). If that text isn't sandboxed, a template author can run
    // arbitrary OS commands on the server.
    //
    // (1) Restrict the ?new() built-in, which instantiates a Java class by name, e.g.
    //         <#assign ex="freemarker.template.utility.Execute"?new()>${ex("id")}
    //     Execute's constructor/call runs Runtime.exec(...), so with FreeMarker's default
    //     resolver (UNRESTRICTED_RESOLVER) that one line is remote code execution.
    //     SAFER_RESOLVER blocks the three classes that enable this (Execute,
    //     ObjectConstructor, JythonRuntime) but still lets ?new() create the product's own
    //     directive classes, which the bundled .ftl templates rely on (via the subclass
    //     SectionsConfiguration). So legitimate templates keep working; the exploit doesn't.
    setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
    //
    // (2) Disable the ?api built-in. ?api exposes an object's raw Java API to the template
    //     (e.g. someObject?api.getClass()...), which is a reflection path that could reach
    //     dangerous classes and defeat the resolver in (1). We don't use ?api anywhere, so
    //     turn it off. (It already defaults to off in FreeMarker; set explicitly to be safe.)
    setAPIBuiltinEnabled(false);
    if (DebugSettings.isDebuggingMode()) {
      setCacheStorage(new NullCacheStorage());
    }
  }
}
