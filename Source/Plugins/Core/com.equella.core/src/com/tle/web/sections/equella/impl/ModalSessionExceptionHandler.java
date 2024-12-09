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

package com.tle.web.sections.equella.impl;

import com.tle.web.sections.equella.AbstractModalSessionExceptionHandler;
import com.tle.web.sections.equella.AbstractModalSessionServiceImpl;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.equella.ModalSessionService;
import javax.inject.Inject;

public class ModalSessionExceptionHandler
    extends AbstractModalSessionExceptionHandler<ModalSession> {
  @Inject private ModalSessionService modalService;

  @Override
  protected AbstractModalSessionServiceImpl<ModalSession> getModalService() {
    return (ModalSessionServiceImpl) modalService;
  }
}
