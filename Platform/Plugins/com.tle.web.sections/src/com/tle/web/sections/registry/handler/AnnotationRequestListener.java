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

package com.tle.web.sections.registry.handler;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;

public class AnnotationRequestListener extends TargetedListener implements ParametersEventListener {
  private final AnnotatedBookmarkScanner handler;

  public AnnotationRequestListener(
      String id, Section section, SectionTree tree, AnnotatedBookmarkScanner handler) {
    super(id, section, tree);
    this.handler = handler;
  }

  @Override
  public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception {
    handler.handleParameters(info, id + ".", info.getModelForId(id), event); // $NON-NLS-1$
  }
}
