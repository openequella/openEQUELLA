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

package com.tle.web.wizard.section;

import com.dytech.edge.wizard.WizardException;
import com.google.common.base.Throwables;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;
import javax.inject.Singleton;

@Bind
@Singleton
public class WizardErrorHandler implements SectionsExceptionHandler {

  @Override
  public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event) {
    return ex instanceof WizardException;
  }

  @Override
  public void handle(
      Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event) {
    if (exception instanceof WizardException) {
      WizardException we = (WizardException) exception;
      Throwable cause = we.getCause();
      if (cause == null) {
        cause = we;
      }
      RootWizardSection rootWizard = info.lookupSection(RootWizardSection.class);
      rootWizard.setException(info, cause);
      info.preventGET();
      info.renderNow();
    } else {
      Throwables.throwIfUnchecked(exception);
      throw new RuntimeException(exception);
    }
  }
}
