package com.tle.web.payment.notification.portal;

import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.web.workflow.portal.TaskListExtension;
import com.tle.web.workflow.portal.TaskListSubsearch;

/**
 * 
 */
@Bind
@Singleton
public class SalesNotificationPortalExtension implements TaskListExtension
{
	@Inject
	private PaymentNotificationFilterFactory factory;

	@Override
	public List<TaskListSubsearch> getTaskFilters()
	{
		Builder<TaskListSubsearch> notificationFilters = ImmutableList.builder();
		notificationFilters.add(factory.createPurchased());
		return notificationFilters.build();
	}

	@BindFactory
	public interface PaymentNotificationFilterFactory
	{
		ItemSaleNotificationFilter createPurchased();
	}
}
