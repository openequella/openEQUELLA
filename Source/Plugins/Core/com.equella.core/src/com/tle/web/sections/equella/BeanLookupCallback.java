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

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;

public class BeanLookupCallback implements ModalSessionCallback {
  private static final long serialVersionUID = 1L;
  protected final String beanName;
  protected final Object pluginObj;

  public BeanLookupCallback(Class<? extends ModalSessionCallback> clazz) {
    this.beanName = "bean:" + clazz.getName(); // $NON-NLS-1$
    this.pluginObj = clazz;
  }

  @Override
  public void executeModalFinished(SectionInfo info, ModalSession session) {
    ModalSessionCallback lookedUp = ResourcesService.getResourceHelper(pluginObj).getBean(beanName);
    lookedUp.executeModalFinished(info, session);
  }
}
