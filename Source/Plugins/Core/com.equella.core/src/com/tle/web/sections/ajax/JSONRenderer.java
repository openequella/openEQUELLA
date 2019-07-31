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

package com.tle.web.sections.ajax;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import java.io.IOException;

public class JSONRenderer implements SectionRenderable {
  private static ObjectMapper mapper;
  private final Object object;
  private final boolean setContentType;

  static {
    mapper = new ObjectMapper();
  }

  public JSONRenderer(Object object, boolean setContentType) {
    Class<? extends Object> clazz = object.getClass();
    // Sonar may complain about boolean expression > 3, but this is as
    // expressive as required
    if (clazz == String.class
        || clazz == Boolean.class
        || clazz == Integer.class
        || clazz == Long.class // NOSONAR
        || clazz == Byte.class
        || clazz == Character.class) {
      object = new SimpleValue(object);
    }
    this.object = object;
    this.setContentType = setContentType;
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    mapper.writeValue(writer, object);
  }

  @Override
  public void preRender(PreRenderContext info) {
    if (setContentType) {
      info.getResponse().setContentType("application/json"); // $NON-NLS-1$
    }
  }

  public static class SimpleValue {
    private final Object value;

    public SimpleValue(Object value) {
      this.value = value;
    }

    public Object getValue() {
      return value;
    }
  }
}
