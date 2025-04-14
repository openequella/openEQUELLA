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

package com.tle.core.collection.service;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import java.util.Collection;
import java.util.List;

public interface ItemDefinitionService
    extends AbstractEntityService<EntityEditingBean, ItemDefinition>, RemoteItemDefinitionService {
  List<ItemDefinition> enumerateForType(String type);

  List<ItemDefinition> enumerateForWorkflow(long workflowID);

  List<ItemDefinition> enumerateWithWorkflow();

  List<ItemDefinition> enumerateCreateable();

  List<ItemDefinition> enumerateSearchable();

  List<BaseEntityLabel> listAllForSchema(long schemaID);

  List<BaseEntityLabel> listSearchable();

  List<BaseEntityLabel> listCreateable();

  Collection<ItemDefinition> filterSearchable(Collection<ItemDefinition> collections);

  List<ItemDefinition> getMatchingSearchable(Collection<Long> itemdefs);

  List<ItemDefinition> getMatchingSearchableUuid(Collection<String> itemdefUuids);

  List<ItemDefinition> getMatchingCreatableUuid(Collection<String> itemdefs);

  ItemDefinition getForItemCreate(String uuid);

  ItemDefinition getByItemIdUnsecure(ItemKey itemId);
}
