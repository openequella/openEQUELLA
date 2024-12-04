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

package com.tle.core.scheduler.standard.task;

import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@Singleton
public class RemoveStagingAreas implements ScheduledTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoveStagingAreas.class);

  @Inject private StagingService stagingService;

  @com.google.inject.Inject(optional = true)
  @Named("com.tle.core.tasks.RemoveStagingAreas.enable")
  // Can be overrode by the optional-config.properties
  private boolean enableTask = true;

  @Override
  public void execute() {
    if (enableTask) {
      stagingService.removeUnusedStagingAreas();
    } else {
      LOGGER.debug("RemoveStagingAreas is disabled.  Not running task.");
    }
  }
}
