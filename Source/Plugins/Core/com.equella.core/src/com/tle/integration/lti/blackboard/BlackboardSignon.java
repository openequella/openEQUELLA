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

package com.tle.integration.lti.blackboard;

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
public class BlackboardSignon extends AbstractPrototypeSection<SingleSignonForm>
    implements AfterParametersListener {
  @Inject private BlackboardLtiIntegration blackboardLtiIntegration;

  @Override
  public void afterParameters(SectionInfo info, ParametersEvent event) {
    final SingleSignonForm model = getModel(info);
    blackboardLtiIntegration.setupSingleSignOn(info, model);
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
