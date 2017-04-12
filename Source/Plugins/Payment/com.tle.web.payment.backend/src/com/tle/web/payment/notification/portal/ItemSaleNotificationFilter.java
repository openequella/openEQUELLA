package com.tle.web.payment.notification.portal;

import javax.inject.Inject;

import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.Bind;
import com.tle.core.notification.indexer.NotificationIndex;
import com.tle.core.notification.indexer.NotificationSearch;
import com.tle.core.payment.PaymentConstants;
import com.tle.web.notification.filters.FilterByNotificationReason;
import com.tle.web.notification.portal.NotifcationPortalConstants;
import com.tle.web.notification.section.RootNotificationListSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.workflow.portal.TaskListSubsearch;

@Bind
public class ItemSaleNotificationFilter implements TaskListSubsearch
{
	@PlugKey("notification.filternames.itemsale")
	private static Label LABEL_NAME;

	@Inject
	private SectionsController sectionsController;

	@SuppressWarnings("nls")
	@Override
	public String getIdentifier()
	{
		return "notesold";
	}

	@Override
	public DefaultSearch getSearch()
	{
		NotificationSearch search = new NotificationSearch();
		search.addMust(NotificationIndex.FIELD_REASON, PaymentConstants.NOTIFICATION_REASON_ITEMSALE);
		return search;
	}

	@Override
	public boolean isSecondLevel()
	{
		return true;
	}

	@Override
	public SectionInfo setupForward(SectionInfo from)
	{
		SectionInfo forward;
		if( from != null )
		{
			forward = from.createForward(RootNotificationListSection.URL);
		}
		else
		{
			forward = sectionsController.createForward(RootNotificationListSection.URL);
		}
		FilterByNotificationReason filter = forward.lookupSection(FilterByNotificationReason.class);
		filter.setReason(forward, PaymentConstants.NOTIFICATION_REASON_ITEMSALE);
		return forward;
	}

	@Override
	public Label getName()
	{
		return LABEL_NAME;
	}

	@Override
	public String getParentIdentifier()
	{
		return NotifcationPortalConstants.ID_ALL;
	}
}
