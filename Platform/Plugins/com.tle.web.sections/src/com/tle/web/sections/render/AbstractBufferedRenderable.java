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

package com.tle.web.sections.render;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import java.io.IOException;
import java.io.StringWriter;

@NonNullByDefault
public abstract class AbstractBufferedRenderable implements SectionRenderable {
  private boolean rendered;
  private SimpleSectionResult renderedResult;

  @Override
  public void preRender(PreRenderContext info) {
    if (!rendered) {
      StringWriter out = new StringWriter();
      try {
        render(new SectionWriter(out, info));
      } catch (IOException e) {
        throw new SectionsRuntimeException(e);
      }
      renderedResult = new SimpleSectionResult(out.toString());
      rendered = true;
    }
  }

  @Override
  public void realRender(SectionWriter writer) throws IOException {
    if (!rendered) {
      render(writer);
    } else {
      renderedResult.realRender(writer);
    }
  }

  public abstract void render(SectionWriter writer) throws IOException;
}
