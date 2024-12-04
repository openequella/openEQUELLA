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

package com.tle.web.scripting.objects;

import com.dytech.edge.exceptions.ItemNotFoundException;
import com.tle.beans.item.*;
import com.tle.common.scripting.objects.ItemScriptObject;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.scripting.ScriptTypeFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(ItemScriptObject.class)
@Singleton
public class ItemScriptWrapper extends AbstractScriptWrapper implements ItemScriptObject {
  private static final long serialVersionUID = 1L;

  @Inject private ScriptTypeFactory scriptTypeFactory;

  @Inject private ItemService itemService;

  @Override
  public ItemScriptType getItem(String uuid, int version) {
    try {
      return scriptTypeFactory.createItem(new ItemId(uuid, version));
    } catch (ItemNotFoundException e) {
      return null;
    }
  }

  @Override
  public ItemScriptType getLatestVersionItem(String uuid) {
    try {
      int version = itemService.getLatestVersion(uuid);
      return scriptTypeFactory.createItem(new ItemId(uuid, version));
    } catch (ItemNotFoundException e) {
      return null;
    }
  }

  @Override
  public ItemScriptType getLiveItem(String uuid) {
    try {
      int version = itemService.getLiveItemVersion(uuid);
      return scriptTypeFactory.createItem(new ItemId(uuid, version));
    } catch (ItemNotFoundException e) {
      return null;
    }
  }
}
