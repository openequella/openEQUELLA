package com.tle.web.payment.notification;

import static com.tle.core.payment.storefront.operation.PurchasedItemUpdatedOperation.REASON_PURCHASE_UPDATED;

import java.io.StringWriter;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ListMultimap;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.notification.ItemNotification;
import com.tle.web.workflow.notification.StandardNotifications;

@Bind
@Singleton
public class PurchasedItemUpdatedNotifications extends StandardNotifications
{
	@PlugKey("notereason.purchaseupdate.filter")
	private static Label LABEL_REASON_FILTER;
	@PlugKey("notificationlist.purchaseupdate.reason")
	private static Label LABEL_REASON;

	@PlugKey("email.purchaseupdate.header")
	private static Label LABEL_HEADER;

	@Inject
	private ExtendedFreemarkerFactory viewFactory;

	@SuppressWarnings("nls")
	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		final StringWriter writer = new StringWriter();
		final List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(REASON_PURCHASE_UPDATED));
		if( !itemNotifications.isEmpty() )
		{
			final PurchasedItemUpdatedEmailNotifications model = new PurchasedItemUpdatedEmailNotifications();
			model.setHeader(LABEL_HEADER);
			model.setNotifications(itemNotifications);
			viewFactory.render(viewFactory.createResultWithModel("notification-item.ftl", model), writer);
		}
		return writer.toString();
	}

	@Override
	public Label getReasonLabel(String type)
	{
		return LABEL_REASON;
	}

	@Override
	public Label getReasonFilterLabel(String type)
	{
		return LABEL_REASON_FILTER;
	}

	public static class PurchasedItemUpdatedEmailNotifications extends EmailNotifications
	{
		private Label emptyLabel;

		public Label getEmptyLabel()
		{
			return emptyLabel;
		}

		public void setEmptyLabel(Label emptyLabel)
		{
			this.emptyLabel = emptyLabel;
		}
	}
}
