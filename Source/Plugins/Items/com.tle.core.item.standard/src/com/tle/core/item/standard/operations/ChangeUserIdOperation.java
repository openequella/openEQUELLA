package com.tle.core.item.standard.operations;

import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ModerationStatus;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class ChangeUserIdOperation extends AbstractStandardWorkflowOperation // NOSONAR
{
	private String fromUser;
	private String toUser;

	@AssistedInject
	private ChangeUserIdOperation(@Assisted("fromUserId") String fromUser, @Assisted("toUserId") String toUser)
	{
		this.fromUser = fromUser;
		this.toUser = toUser;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		boolean updated = false;

		if( item.getOwner().equals(fromUser) )
		{
			item.setOwner(toUser);
			updated = true;
		}

		updated = swap(item.getCollaborators()) || updated;
		updated = swap(item.getNotifications()) || updated;
		// update moderation status 'rejectedBy' where not null & matching
		ModerationStatus moderation = item.getModeration();
		if( moderation != null && moderation.getRejectedBy() != null && moderation.getRejectedBy().equals(fromUser) )
		{
			moderation.setRejectedBy(toUser);
			updated = true;
		}
		updated = notificationService.userIdChanged(getItemId(), fromUser, toUser) || updated;

		if( updated )
		{
			params.setUpdateSecurity(true);
		}
		return updated;
	}

	private boolean swap(Set<String> userIds)
	{
		if( userIds.remove(fromUser) )
		{
			userIds.add(toUser);
			return true;
		}
		return false;
	}
}
