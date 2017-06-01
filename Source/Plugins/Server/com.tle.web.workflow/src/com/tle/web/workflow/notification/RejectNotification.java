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
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.collect.ListMultimap;
import com.google.inject.Singleton;
import com.tle.beans.item.Item;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;

@Bind
@Singleton
public class RejectNotification extends StandardNotifications
{
	@PlugKey("emailheader.reason.rejected")
	private static String KEY_HEADER;
	@PlugKey("email.msgformat")
	private static String KEY_MSG_FRMT;
	@PlugKey("email.unknownuser")
	private static Label LABEL_UNKNOWN_USER;

	@Inject
	private ExtendedFreemarkerFactory viewFactory;
	@Inject
	private UserService userService;

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		StringWriter writer = new StringWriter();
		List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(Notification.REASON_REJECTED));
		if( !itemNotifications.isEmpty() )
		{
			PluralKeyLabel header = new PluralKeyLabel(KEY_HEADER, itemNotifications.size());
			final EmailNotifications model = new EmailNotifications();

			model.setHeader(header);
			model.setNotifications(itemNotifications);
			viewFactory.render(viewFactory.createResultWithModel("notification-rejected.ftl", model), writer);
		}
		return writer.toString();
	}

	@Override
	protected ItemNotification createItemNotification(Item item)
	{
		RejectedNotification rejectNotification = new RejectedNotification();
		ModerationStatus modStatus = item.getModeration();
		UserBean rejector = userService.getInformationForUser(item.getModeration().getRejectedBy());
		rejectNotification.setRejectMessage(new KeyLabel(KEY_MSG_FRMT, modStatus.getRejectedMessage(),
			buildUserString(rejector)));
		return rejectNotification;
	}

	@SuppressWarnings("nls")
	private String buildUserString(UserBean user)
	{
		return user == null ? LABEL_UNKNOWN_USER.getText() : user.getFirstName() + " " + user.getLastName() + " ("
			+ user.getUsername() + ")";
	}

	public static class RejectedNotification extends ItemNotification
	{
		private KeyLabel rejectMessage;

		public KeyLabel getRejectMessage()
		{
			return rejectMessage;
		}

		public void setRejectMessage(KeyLabel rejectMessage)
		{
			this.rejectMessage = rejectMessage;
		}

	}

}
