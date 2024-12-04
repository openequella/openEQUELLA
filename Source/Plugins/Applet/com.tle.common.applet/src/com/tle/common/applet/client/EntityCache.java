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

package com.tle.common.applet.client;

import com.tle.beans.NameId;
import com.tle.common.Format;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntityCache {
  private final Map<Long, NameId> schemas;
  private final Map<Long, NameId> itemDefinitions;
  private final Map<Long, NameId> powerSearches;

  public EntityCache(ClientService clientService) {
    itemDefinitions = transform(clientService.getService(RemoteItemDefinitionService.class), true);
    powerSearches = transform(clientService.getService(RemotePowerSearchService.class), true);
    schemas = transform(clientService.getService(RemoteSchemaService.class), true);
  }

  public Map<Long, NameId> getItemDefinitionMap() {
    return itemDefinitions;
  }

  public Map<Long, NameId> getSchemaMap() {
    return schemas;
  }

  public Collection<NameId> getItemDefinitions() {
    return itemDefinitions.values();
  }

  public Collection<NameId> getPowerSearches() {
    return powerSearches.values();
  }

  public Collection<NameId> getSchemas() {
    return schemas.values();
  }

  private Map<Long, NameId> transform(
      RemoteAbstractEntityService<?> service, boolean showArchived) {
    List<NameId> nis =
        BundleCache.getNameIds(showArchived ? service.listAll() : service.listEnabled());
    Collections.sort(nis, Format.NAME_ID_COMPARATOR);

    Map<Long, NameId> results = new LinkedHashMap<Long, NameId>(nis.size());
    for (NameId ni : nis) {
      results.put(ni.getId(), ni);
    }

    return Collections.unmodifiableMap(results);
  }
}
