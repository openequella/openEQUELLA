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

package com.tle.core.scheduler.standard.task;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.Constants;
import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.events.services.EventService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserService;

@Bind
@Singleton
public class CheckDeletedUsersTask implements ScheduledTask
{
	@Inject
	private EntityRegistry entityRegistry;
	@Inject
	private ItemService itemService;
	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private UserService userService;
	@Inject
	private EventService eventService;

	@Override
	@Transactional
	public void execute()
	{
		final Set<String> allUsers = new HashSet<String>();

		for( AbstractEntityService<?, BaseEntity> service : entityRegistry.getAllEntityServices() )
		{
			Set<String> referencedUsers = service.getReferencedUsers();
			allUsers.addAll(referencedUsers);
		}

		allUsers.addAll(itemService.getReferencedUsers());
		allUsers.addAll(userPreferenceService.getReferencedUsers());

		// Remove all users that information could be found for
		allUsers.removeAll(userService.getInformationForUsers(allUsers).keySet());

		// Remove any nulls or blank strings that made there way in
		allUsers.remove(null);
		allUsers.remove(Constants.BLANK);

		// ...IDs left over are for users that don't exist anymore
		for( String delUser : allUsers )
		{
			// Post these synchronously, as we are in danger of absolutely swamping the connections
			eventService.publishApplicationEvent(new UserDeletedEvent(delUser, true));
		}
	}
}
