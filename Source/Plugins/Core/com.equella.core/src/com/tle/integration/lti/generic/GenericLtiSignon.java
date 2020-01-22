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

package com.tle.integration.lti.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import javax.inject.Inject;

/**
 * Not sign-on as such, but an extension of signon.do which doesn't do sign-on but sets up an
 * integration selection session
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class GenericLtiSignon extends AbstractPrototypeSection<SingleSignonForm>
    implements AfterParametersListener {
  @Inject private GenericLtiIntegration genericLtiIntegration;

  @Override
  public void afterParameters(SectionInfo info, ParametersEvent event) {
    final SingleSignonForm model = getModel(info);
    genericLtiIntegration.setupSingleSignOn(info, model);
  }

  @Override
  public String getDefaultPropertyName() {
    return "";
  }

  @Override
  public SingleSignonForm instantiateModel(SectionInfo info) {
    return new SingleSignonForm();
  }
}
