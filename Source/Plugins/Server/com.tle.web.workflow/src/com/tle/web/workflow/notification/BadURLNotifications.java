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

package com.tle.web.workflow.notification;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ListMultimap;
import com.tle.beans.ReferencedURL;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
@Bind
@Singleton
public class BadURLNotifications extends StandardNotifications
{
	@PlugKey("emailheader.reason.badurl")
	private static Label LABEL_HEADER;

	@Inject
	private ExtendedFreemarkerFactory viewFactory;

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		StringWriter writer = new StringWriter();
		List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(Notification.REASON_BADURL));
		if( !itemNotifications.isEmpty() )
		{
			final EmailNotifications model = new EmailNotifications();
			model.setHeader(LABEL_HEADER);
			model.setNotifications(itemNotifications);
			viewFactory.render(viewFactory.createResultWithModel("notification-badurl.ftl", model), writer);
		}
		return writer.toString();
	}

	@Override
	protected ItemNotification createItemNotification(Item item)
	{
		BadUrlNotification badUrlNotification = new BadUrlNotification();
		badUrlNotification.setUrls(Collections2.transform(ReferencedURL.keepBadUrls(item.getReferencedUrls()),
			new Function<ReferencedURL, String>()
			{
				@Override
				public String apply(ReferencedURL input)
				{
					return input.getUrl();
				}
			}));
		return badUrlNotification;
	}

	public static class BadUrlNotification extends ItemNotification
	{
		private Collection<String> urls;

		public Collection<String> getUrls()
		{
			return urls;
		}

		public void setUrls(Collection<String> urls)
		{
			this.urls = urls;
		}
	}
}
