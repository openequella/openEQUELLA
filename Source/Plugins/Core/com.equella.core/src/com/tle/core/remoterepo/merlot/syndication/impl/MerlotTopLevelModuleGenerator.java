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

package com.tle.core.remoterepo.merlot.syndication.impl;

import com.rometools.rome.feed.module.Module;
import com.rometools.rome.io.ModuleGenerator;
import com.tle.core.remoterepo.merlot.syndication.MerlotTopLevelModule;
import java.util.Collections;
import java.util.Set;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class MerlotTopLevelModuleGenerator implements ModuleGenerator {
  private static final Set<Namespace> NAMESPACES =
      Collections.unmodifiableSet(Collections.singleton(MerlotTopLevelModule.NAMESPACE));

  @Override
  public void generate(Module module, Element element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getNamespaceUri() {
    return MerlotTopLevelModule.URI;
  }

  @Override
  public Set<Namespace> getNamespaces() {
    return NAMESPACES;
  }
}
