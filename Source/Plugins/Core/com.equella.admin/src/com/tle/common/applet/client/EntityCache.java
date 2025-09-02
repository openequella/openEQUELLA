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

import com.tle.admin.service.AdminSchemaService;
import com.tle.beans.NameId;
import com.tle.common.Format;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.i18n.BundleCache;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides cached access to common entities - item definitions, schemas and power searches. */
public class EntityCache {
  private static final Logger LOGGER = LoggerFactory.getLogger(EntityCache.class);

  private final Map<Long, NameId> schemas;
  private final Map<Long, NameId> itemDefinitions;
  private final Map<Long, NameId> powerSearches;

  /**
   * Construct the cache, loading all entities from the server.
   *
   * @param clientService the client service
   */
  public EntityCache(ClientService clientService) {
    LOGGER.debug("Loading entity cache ⌛");

    itemDefinitions = transform(clientService.getService(RemoteItemDefinitionService.class));
    powerSearches = transform(clientService.getService(RemotePowerSearchService.class));
    schemas = transform(clientService.getService(AdminSchemaService.class));

    LOGGER.debug("Entity cache loaded ✅");
  }

  public Map<Long, NameId> getItemDefinitionMap() {
    LOGGER.debug("Retrieving item definition map from entity cache");

    return itemDefinitions;
  }

  public Map<Long, NameId> getSchemaMap() {
    LOGGER.debug("Retrieving schema map from entity cache");

    return schemas;
  }

  public Collection<NameId> getPowerSearches() {
    LOGGER.debug("Retrieving power searches from entity cache");

    return powerSearches.values();
  }

  private Map<Long, NameId> transform(RemoteAbstractEntityService<?> service) {
    List<NameId> nis = BundleCache.getNameIds(service.listAll());
    nis.sort(Format.NAME_ID_COMPARATOR);

    Map<Long, NameId> results = new LinkedHashMap<>(nis.size());
    for (NameId ni : nis) {
      results.put(ni.getId(), ni);
    }

    return Collections.unmodifiableMap(results);
  }
}
