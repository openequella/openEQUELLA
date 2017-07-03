package com.tle.web.workflow.notification;

import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ListMultimap;
import com.tle.beans.item.Item;
import com.tle.beans.item.ModerationStatus;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;

/**
 * A placeholder pending implementation of the email template
 * 
 * @author larry
 */
@Bind
@Singleton
public class ReassignNotification extends StandardNotifications
{
	@PlugKey("emailheader.taskreason.other")
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
		List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(Notification.REASON_REASSIGN));
		if( !itemNotifications.isEmpty() )
		{
			PluralKeyLabel header = new PluralKeyLabel(KEY_HEADER, itemNotifications.size());
			final EmailNotifications model = new EmailNotifications();

			model.setHeader(header);
			model.setNotifications(itemNotifications);
			viewFactory.render(viewFactory.createResultWithModel("notification-reassigned.ftl", model), writer);
		}
		return writer.toString();
	}

	@Override
	public int countNotification(ListMultimap<String, Notification> typeMap)
	{
		int size = typeMap.get(Notification.REASON_REASSIGN).size();
		return size;
	}

	@Override
	protected ItemNotification createItemNotification(Item item)
	{
		ReassignedNotification reassignNotification = new ReassignedNotification();
		ModerationStatus modStatus = item.getModeration();
		// hijacking the 'rejectedBy'/'rejectedMessage' members
		if( !Check.isEmpty(modStatus.getRejectedMessage()) )
		{
			UserBean reassignor = userService.getInformationForUser(item.getModeration().getRejectedBy());
			reassignNotification.setReassignMessage(new KeyLabel(KEY_MSG_FRMT, modStatus.getRejectedMessage(),
				buildUserString(reassignor)));
		}
		return reassignNotification;
	}

	@SuppressWarnings("nls")
	private String buildUserString(UserBean user)
	{
		return user == null ? LABEL_UNKNOWN_USER.getText() : Format.format(user);
	}

	public static class ReassignedNotification extends ItemNotification
	{
		private KeyLabel reassignMessage;

		public KeyLabel getReassignMessage()
		{
			return reassignMessage;
		}

		public void setReassignMessage(KeyLabel reassignMessage)
		{
			this.reassignMessage = reassignMessage;
		}
	}
}
