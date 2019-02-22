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

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.standard.model.HtmlComponentState;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractElementRenderer extends AbstractComponentRenderer {

  public AbstractElementRenderer(HtmlComponentState state) {
    super(state);
  }

  @Override
  protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs)
      throws IOException {
    super.prepareFirstAttributes(writer, attrs);
    attrs.put("name", getName(writer)); // $NON-NLS-1$
  }

  protected String getName(SectionInfo info) {
    return state.getName();
  }
}
