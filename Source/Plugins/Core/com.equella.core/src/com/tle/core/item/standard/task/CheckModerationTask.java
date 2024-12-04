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

package com.tle.core.item.standard.task;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.filter.workflow.CheckModerationFilter;
import com.tle.core.scheduler.ScheduledTask;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class CheckModerationTask implements ScheduledTask {
  @Inject private ItemService itemService;
  @Inject private Provider<CheckModerationFilter> filterFactory;

  @Override
  public void execute() {
    itemService.operateAll(filterFactory.get());
  }
}
