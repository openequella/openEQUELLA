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

package com.tle.cal.web;

import com.tle.cal.service.CALService;
import com.tle.cal.web.viewitem.summary.CALAgreementDialog;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightResourceViewerFilter;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class CALResourceViewerFilter extends AbstractCopyrightResourceViewerFilter {

  @Inject private CALService calService;

  @Override
  protected Class<? extends AbstractCopyrightAgreementDialog> getDialogClass() {
    return CALAgreementDialog.class;
  }

  @Override
  protected CopyrightService<?, ?, ?> getCopyrightService() {
    return calService;
  }
}
