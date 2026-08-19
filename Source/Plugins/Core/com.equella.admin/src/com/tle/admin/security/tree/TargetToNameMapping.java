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

package com.tle.admin.security.tree;

import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_COLLECTIONS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_COURSE_INFO;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_FEDERATED_SEARCHES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_POWER_SEARCHES;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_SCHEMAS;
import static com.tle.common.security.SecurityConstants.PRIORITY_ALL_SYSTEM_SETTINGS;
import static com.tle.common.security.SecurityConstants.PRIORITY_INSTITUTION;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.admin.service.AdminBaseEntityService;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemotePrivilegeTreeService;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.remoting.RemoteItemService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import scala.jdk.javaapi.OptionConverters;

@SuppressWarnings("nls")
public class TargetToNameMapping {
  private final ClientService services;
  private final RemotePrivilegeTreeService privilegeTreeService;
  private final AdminBaseEntityService adminBaseEntityService;

  /** Mapping of priorities to names for entries targeting *. */
  @Deprecated private final Map<Integer, String> everythingMapping;

  /** Mapping of targets to names. */
  private final Map<TargetId, String> mappingCache = Maps.newHashMap();

  public TargetToNameMapping(ClientService services) {
    this.services = services;
    this.privilegeTreeService = services.getService(RemotePrivilegeTreeService.class);
    this.adminBaseEntityService = services.getService(AdminBaseEntityService.class);

    // TODO: Delete the following rubbish
    everythingMapping = new HashMap<Integer, String>();
    everythingMapping.put(
        PRIORITY_INSTITUTION,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.institution"));
    everythingMapping.put(
        PRIORITY_ALL_COLLECTIONS,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allcollections"));
    everythingMapping.put(
        PRIORITY_ALL_POWER_SEARCHES,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allsearches"));
    everythingMapping.put(
        PRIORITY_ALL_SCHEMAS,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allschemas"));
    everythingMapping.put(
        PRIORITY_ALL_FEDERATED_SEARCHES,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allfed"));

    // TODO: some of these don't belong here. ie. they are in plugins
    everythingMapping.put(
        PRIORITY_ALL_COURSE_INFO,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allcourses"));
    everythingMapping.put(
        PRIORITY_ALL_SYSTEM_SETTINGS,
        CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allsystemsettings"));
  }

  public void addEntries(List<ACLEntryMapping> entries) {
    List<TargetId> unknownIds = Lists.newArrayList();
    for (ACLEntryMapping entry : entries) {
      TargetId id = toId(entry);
      if (!mappingCache.containsKey(id)) {
        unknownIds.add(id);
      }
    }

    if (!Check.isEmpty(unknownIds)) {
      mappingCache.putAll(privilegeTreeService.mapTargetIdsToNames(unknownIds));
    }

    // TODO: Delete the following rubbish - should only be using the above
    // in a plug-in-inated world.
    for (ACLEntryMapping entry : entries) {
      TargetId targetId = toId(entry);
      String target = entry.getTarget();

      if (!mappingCache.containsKey(targetId)) {
        String name = resolveTargetName(target, entry);
        mappingCache.put(targetId, name);
      }
    }
  }

  private String resolveTargetName(String target, ACLEntryMapping entry) {
    if (target.equals(SecurityConstants.TARGET_EVERYTHING)) {
      return everythingMapping.get(Math.abs(entry.getPriority()));
    } else if (target.startsWith(SecurityConstants.TARGET_BASEENTITY)) {
      long id = parseTargetId(target);
      return getNameById(id);
    } else if (target.startsWith(SecurityConstants.TARGET_ITEM)) {
      long id = parseTargetId(target);
      return services.getService(RemoteItemService.class).getNameForId(id);
    } else if (target.startsWith(SecurityConstants.TARGET_ITEM_STATUS)) {
      return resolveItemStatusName(target);
    } else if (target.startsWith(SecurityConstants.TARGET_ITEM_METADATA)) {
      return resolveItemMetadataName(target);
    } else if (target.startsWith(SecurityConstants.TARGET_DYNAMIC_ITEM_METADATA)) {
      return resolveDynamicItemMetadataName(target);
    }
    return null;
  }

  private String getTargetIdString(String target) {
    return target.substring(2);
  }

  private long parseTargetId(String target) {
    return Long.parseLong(getTargetIdString(target));
  }

  private String resolveItemStatusName(String target) {
    String id = getTargetIdString(target);
    int index = id.indexOf(':');
    Long entityID = Long.parseLong(id.substring(0, index));

    if (index > 0) {
      return CurrentLocale.get(
          "com.tle.admin.security.tree.targettonamemapping.itemsfor",
          id.substring(index + 1),
          getNameById(entityID));
    } else {
      return CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.items", id);
    }
  }

  private String resolveItemMetadataName(String target) {
    String id = getTargetIdString(target);
    int index = id.indexOf(':');
    Long entityID = Long.parseLong(id.substring(0, index));

    return CurrentLocale.get(
        "com.tle.admin.security.tree.targettonamemapping.metarule", getNameById(entityID));
  }

  private String resolveDynamicItemMetadataName(String target) {
    String id = getTargetIdString(target);
    int index = id.lastIndexOf(':') + 1;
    Long entityID = Long.parseLong(id.substring(index));

    return CurrentLocale.get(
        "com.tle.admin.security.tree.targettonamemapping.dynametarule", getNameById(entityID));
  }

  private String getNameById(Long id) {
    return CurrentLocale.get(getBundleOrNull(id));
  }

  @Nullable
  private LanguageBundle getBundleOrNull(long id) {
    return OptionConverters.toJava(adminBaseEntityService.getNameForId(id)).orElse(null);
  }

  public String getName(ACLEntryMapping entry) {
    TargetId target = toId(entry);
    String result = mappingCache.get(target);
    if (result == null) {
      result = CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.unknown", target);
    }
    return result;
  }

  private TargetId toId(ACLEntryMapping entry) {
    return new TargetId(entry.getPriority(), entry.getTarget());
  }
}
