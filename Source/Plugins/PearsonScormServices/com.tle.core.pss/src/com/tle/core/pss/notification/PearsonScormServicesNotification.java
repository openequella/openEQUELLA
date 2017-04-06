package com.tle.core.pss.notification;

import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ListMultimap;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.pss.service.PearsonScormServicesCallbackService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.notification.ItemNotification;
import com.tle.web.workflow.notification.StandardNotifications;

@Bind
@Singleton
public class PearsonScormServicesNotification extends StandardNotifications
{
	@PlugKey("emailheader.reason.badpss")
	private static Label LABEL_HEADER;

	@Inject
	private ExtendedFreemarkerFactory viewFactory;

	@Inject
	private PearsonScormServicesCallbackService pssCallbackService;

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		StringWriter writer = new StringWriter();
		List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get("badpss"));
		if( !itemNotifications.isEmpty() )
		{
			final EmailNotifications model = new EmailNotifications();
			model.setHeader(LABEL_HEADER);
			model.setNotifications(itemNotifications);
			viewFactory.render(viewFactory.createResultWithModel("notification-badpss.ftl", model), writer);
		}
		return writer.toString();
	}

	@Override
	protected ItemNotification createItemNotification(Item item)
	{
		BadPearsonScormServicesNotification badPssNotification = new BadPearsonScormServicesNotification();
		PssCallbackLog logEntry = pssCallbackService.getCallbackLogEntry(item);
		if( logEntry != null )
		{
			String errMsg = logEntry.getMessage();
			badPssNotification.setError(errMsg);
		}
		return badPssNotification;
	}

	public static class BadPearsonScormServicesNotification extends ItemNotification
	{
		private String error;

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}
	}
}
