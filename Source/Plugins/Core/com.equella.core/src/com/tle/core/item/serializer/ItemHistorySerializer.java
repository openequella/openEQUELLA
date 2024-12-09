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

package com.tle.core.item.serializer;

import com.tle.beans.item.HistoryEvent;
import com.tle.core.guice.Bind;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import javax.inject.Singleton;

@Bind
@Singleton
public class ItemHistorySerializer {
  public HistoryEventBean serialize(HistoryEvent event) {
    final HistoryEventBean bean = new HistoryEventBean();
    UserBean user = new UserBean();
    user.setId(event.getUser());
    bean.setUser(user);
    bean.setImpersonatedBy(event.getImpersonatedBy());
    bean.setDate(event.getDate());
    // bean.setApplies(event.isApplies());
    bean.setComment(event.getComment());
    bean.setState(event.getState().toString());
    bean.setStep(event.getStep());
    bean.setStepName(event.getStepName());
    bean.setToStep(event.getToStep());
    bean.setToStepName(event.getToStepName());
    bean.setType(event.getType().toString());
    return bean;
  }
}
