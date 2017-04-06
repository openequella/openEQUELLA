/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.workflow.operations;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.ReferencedURL;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.item.impl.ItemUrlGatherer;
import com.tle.core.url.URLCheckerPolicy;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;

public class UpdateReferencedURLsOperation extends AbstractWorkflowOperation
{
	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private ItemUrlGatherer urlGatherer;

	@Override
	public boolean execute()
	{
		final List<ReferencedURL> oldUrls = getItem().getReferencedUrls();
		final List<ReferencedURL> newUrls = Lists.newArrayListWithCapacity(oldUrls.size());

		boolean hasBadUrl = false;
		boolean reachedWarning = false;
		for( String url : urlGatherer.gatherURLs(getItem(), getItemXml()) )
		{
			ReferencedURL rurl = urlCheckerService.getUrlStatus(url, URLCheckMode.RECORDS_FIRST);
			newUrls.add(rurl);

			final int tries = rurl.getTries();
			hasBadUrl |= tries >= URLCheckerPolicy.TRIES_UNTIL_WARNING;
			reachedWarning |= tries == URLCheckerPolicy.TRIES_UNTIL_WARNING;
		}

		if( !hasBadUrl )
		{
			// If all URLs are considered "good" or under the warning level we
			// shouldn't be notifying anyone of anything.
			removeNotificationsForItem(getItemId(), Notification.REASON_BADURL);
		}
		else if( (hasBadUrl && oldUrls.isEmpty()) || reachedWarning )
		{
			// Notify users if this is a newly contributed item and there are
			// already URLs at or over the warning limit, or if a URL is exactly
			// at the warning limited for an edited item.
			addNotifications(getItemId(), getAllOwnerIds(), Notification.REASON_BADURL, true);
		}

		getItem().setReferencedUrls(newUrls);

		return true;
	}
}
