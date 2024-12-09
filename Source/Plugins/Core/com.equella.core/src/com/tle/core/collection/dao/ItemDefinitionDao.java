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

package com.tle.core.collection.dao;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.core.entity.dao.AbstractEntityDao;
import java.util.List;

public interface ItemDefinitionDao extends AbstractEntityDao<ItemDefinition> {
  List<ItemDefinition> findByType(String type);

  List<BaseEntityLabel> listAllForSchema(long schemaID);

  /**
   * @deprecated Use an event to ask for reference
   */
  @Deprecated
  List<Class<?>> getReferencingClasses(long id);

  ItemDefinition getByItemId(ItemKey itemId);
}
