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

package com.tle.web.qti.viewer.questions.renderer.xhtml;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.web.qti.viewer.QtiViewerContext;
import java.util.Map;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;

public class ObjectRenderer extends XhtmlElementRenderer {
  @SuppressWarnings("unused")
  private final Object model;

  @AssistedInject
  public ObjectRenderer(@Assisted Object model, @Assisted QtiViewerContext context) {
    super(model, context);
    this.model = model;
  }

  @Override
  protected void addAttributes(Map<String, String> attrs) {
    super.addAttributes(attrs);
    final String data = attrs.get("data");
    if (data != null) {
      attrs.put("data", getContext().getViewResourceUrl(data).getHref());
    }
  }
}
