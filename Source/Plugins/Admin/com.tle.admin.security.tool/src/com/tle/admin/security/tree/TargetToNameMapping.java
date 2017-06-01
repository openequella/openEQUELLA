/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.remoting.RemotePrivilegeTreeService;
import com.tle.common.security.remoting.RemotePrivilegeTreeService.TargetId;
import com.tle.core.remoting.RemoteBaseEntityService;
import com.tle.core.remoting.RemoteItemService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class TargetToNameMapping
{
	private final ClientService services;
	private final RemotePrivilegeTreeService privilegeTreeService;

	/**
	 * Mapping of priorities to names for entries targeting *.
	 */
	@Deprecated
	private final Map<Integer, String> everythingMapping;

	/**
	 * Mapping of targets to names.
	 */
	private final Map<TargetId, String> mappingCache = Maps.newHashMap();

	public TargetToNameMapping(ClientService services)
	{
		this.services = services;
		this.privilegeTreeService = services.getService(RemotePrivilegeTreeService.class);

		// TODO: Delete the following rubbish
		everythingMapping = new HashMap<Integer, String>();
		everythingMapping.put(PRIORITY_INSTITUTION,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.institution"));
		everythingMapping.put(PRIORITY_ALL_COLLECTIONS,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allcollections"));
		everythingMapping.put(PRIORITY_ALL_POWER_SEARCHES,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allsearches"));
		everythingMapping.put(PRIORITY_ALL_SCHEMAS,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allschemas"));
		everythingMapping.put(PRIORITY_ALL_FEDERATED_SEARCHES,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allfed"));

		// TODO: some of these don't belong here. ie. they are in plugins
		everythingMapping.put(PRIORITY_ALL_COURSE_INFO,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allcourses"));
		everythingMapping.put(PRIORITY_ALL_SYSTEM_SETTINGS,
			CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.allsystemsettings"));
	}

	public void addEntries(List<ACLEntryMapping> entries)
	{
		List<TargetId> unknownIds = Lists.newArrayList();
		for( ACLEntryMapping entry : entries )
		{
			TargetId id = toId(entry);
			if( !mappingCache.containsKey(id) )
			{
				unknownIds.add(id);
			}
		}

		if( !Check.isEmpty(unknownIds) )
		{
			mappingCache.putAll(privilegeTreeService.mapTargetIdsToNames(unknownIds));
		}

		// TODO: Delete the following rubbish - should only be using the above
		// in a plug-in-inated world.
		for( ACLEntryMapping entry : entries )
		{
			TargetId targetId = toId(entry);
			String target = entry.getTarget();

			if( !mappingCache.containsKey(targetId) )
			{
				String name = null;

				if( target.equals(SecurityConstants.TARGET_EVERYTHING) )
				{
					name = everythingMapping.get(Math.abs(entry.getPriority()));
				}
				else if( target.startsWith(SecurityConstants.TARGET_BASEENTITY) )
				{
					long id = Long.parseLong(target.substring(2));
					name = CurrentLocale.get(services.getService(RemoteBaseEntityService.class).getNameForId(id));
				}
				else if( target.startsWith(SecurityConstants.TARGET_ITEM) )
				{
					long id = Long.parseLong(target.substring(2));
					name = services.getService(RemoteItemService.class).getNameForId(id);
				}
				else if( target.startsWith(SecurityConstants.TARGET_ITEM_STATUS) )
				{
					String id = target.substring(2);
					int index = id.indexOf(':');
					if( index > 0 )
					{
						name = CurrentLocale.get(
							"com.tle.admin.security.tree.targettonamemapping.itemsfor",
							id.substring(index + 1),
							CurrentLocale.get(services.getService(RemoteBaseEntityService.class).getNameForId(
								Long.parseLong(id.substring(0, index)))));
					}
					else
					{
						name = CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.items", id);
					}
				}
				else if( target.startsWith(SecurityConstants.TARGET_ITEM_METADATA) )
				{
					String id = target.substring(2);
					int index = id.indexOf(':');
					name = CurrentLocale.get(
						"com.tle.admin.security.tree.targettonamemapping.metarule",
						CurrentLocale.get(services.getService(RemoteBaseEntityService.class).getNameForId(
							Long.parseLong(id.substring(0, index)))));
				}
				else if( target.startsWith(SecurityConstants.TARGET_DYNAMIC_ITEM_METADATA) )
				{
					String id = target.substring(2);
					int index = id.lastIndexOf(':') + 1;
					name = CurrentLocale.get(
						"com.tle.admin.security.tree.targettonamemapping.dynametarule",
						CurrentLocale.get(services.getService(RemoteBaseEntityService.class).getNameForId(
							Long.parseLong(id.substring(index)))));
				}

				mappingCache.put(targetId, name);
			}
		}
	}

	public String getName(ACLEntryMapping entry)
	{
		TargetId target = toId(entry);
		String result = mappingCache.get(target);
		if( result == null )
		{
			result = CurrentLocale.get("com.tle.admin.security.tree.targettonamemapping.unknown", target);
		}
		return result;
	}

	private TargetId toId(ACLEntryMapping entry)
	{
		return new TargetId(entry.getPriority(), entry.getTarget());
	}
}
