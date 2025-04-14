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

package com.tle.core.item.scripting;

import com.tle.beans.item.ItemPack;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.item.service.ItemService;
import com.tle.core.scripting.service.StandardScriptContextParams;
import java.util.Map;

public class WorkflowScriptContextParams extends StandardScriptContextParams {
  private final ItemService service;

  public WorkflowScriptContextParams(
      ItemService service,
      ItemPack itemPack,
      FileHandle fileHandle,
      Map<String, Object> attributes) {
    super(itemPack, fileHandle, true, attributes);
    this.service = service;
  }

  @Override
  public boolean isAnOwner() {
    return service.isAnOwner(getItemPack().getItem(), CurrentUser.getDetails().getUniqueID());
  }
}
