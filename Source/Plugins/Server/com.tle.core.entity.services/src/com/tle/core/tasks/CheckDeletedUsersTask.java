package com.tle.core.tasks;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.Constants;
import com.tle.beans.entity.BaseEntity;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.guice.Bind;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.services.EventService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityRegistry;
import com.tle.core.services.item.ItemService;
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
			eventService.publishApplicationEvent(new UserDeletedEvent(delUser));
		}
	}
}
