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

package com.tle.core.item.standard.operations;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.ReferencedURL;
import com.tle.core.item.service.impl.ItemUrlGatherer;
import com.tle.core.notification.beans.Notification;
import com.tle.core.url.URLCheckerPolicy;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;

public class UpdateReferencedURLsOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private URLCheckerService urlCheckerService;
	@Inject
	private ItemUrlGatherer urlGatherer;
	@Inject
	private URLCheckerPolicy policy;

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
			hasBadUrl |= tries >= policy.getTriesUntilWarning();
			reachedWarning |= tries == policy.getTriesUntilWarning();
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
