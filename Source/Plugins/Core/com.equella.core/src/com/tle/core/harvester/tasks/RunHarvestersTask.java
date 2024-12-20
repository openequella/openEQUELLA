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

package com.tle.core.harvester.tasks;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.scheduler.ScheduledTask;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RunHarvestersTask implements ScheduledTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(RunHarvestersTask.class);
  protected static final String KEY_PFX =
      AbstractPluginService.getMyPluginId(RunHarvestersTask.class) + ".";

  @Inject private HarvesterProfileService harvesterProfileService;
  @Inject private HarvesterProfileService harvesterService;

  @Override
  public void execute() {
    LOGGER.info(CurrentLocale.get(KEY_PFX + "log.runtask"));
    LOGGER.info("------------------");
    for (HarvesterProfile profile : harvesterProfileService.enumerateEnabledProfiles()) {
      harvesterService.startHarvesterTask(profile.getUuid(), false);
    }
  }
}
