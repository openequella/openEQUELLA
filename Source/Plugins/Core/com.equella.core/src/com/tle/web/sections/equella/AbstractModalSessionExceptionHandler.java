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

package com.tle.web.sections.equella;

import com.tle.web.errors.AbstractExceptionHandler;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;

public abstract class AbstractModalSessionExceptionHandler<S extends ModalSession>
    extends AbstractExceptionHandler {
  public static final String MODAL_ERROR_KEY = "$MODAL_ERROR$"; // $NON-NLS-1$

  @Override
  public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event) {
    return super.canHandle(info, ex, event)
        && getModalService().getCurrentSession(SectionUtils.getOriginalInfo(info)) != null
        && shouldHandle(info);
  }

  protected boolean shouldHandle(SectionInfo info) {
    return true;
  }

  @Override
  public void handle(
      Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event) {
    markHandled(info);
    info.setAttribute(MODAL_ERROR_KEY, getFirstCause(exception));
    info.setErrored();
    info.preventGET();
    info.renderNow();
  }

  protected abstract AbstractModalSessionServiceImpl<S> getModalService();
}
