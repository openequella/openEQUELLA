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

package com.tle.web.sections.render;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import java.util.HashMap;
import java.util.Map;

public class ExtraAttributes implements TagProcessor {
  private Map<String, String> extras = new HashMap<String, String>();

  public ExtraAttributes(String... attrs) {
    for (int i = 0; i < attrs.length; i++) {
      extras.put(attrs[i++], attrs[i]);
    }
  }

  @Override
  public void processAttributes(SectionWriter writer, Map<String, String> attrs) {
    attrs.putAll(extras);
  }

  @Override
  public void preRender(PreRenderContext info) {
    // nothing
  }
}
