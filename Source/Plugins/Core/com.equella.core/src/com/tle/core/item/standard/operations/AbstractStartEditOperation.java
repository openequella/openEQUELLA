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

package com.tle.core.item.standard.operations;

import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import javax.inject.Inject;

public abstract class AbstractStartEditOperation extends StartLockOperation {
  private final boolean modify;

  @Inject private StagingService stagingService;

  public AbstractStartEditOperation(boolean modify, boolean dontRelock) {
    super(dontRelock);
    this.modify = modify;
  }

  @Override
  public boolean execute() {
    super.execute();
    if (modify) {
      ItemFile file = itemFileService.getItemFile(getItem());
      StagingFile staging = stagingService.createStagingArea();
      fileSystemService.copy(file, staging);
      getItemPack().setStagingID(staging.getUuid());
    }
    return false;
  }
}
