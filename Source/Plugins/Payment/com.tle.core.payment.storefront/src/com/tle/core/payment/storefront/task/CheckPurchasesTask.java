package com.tle.core.payment.storefront.task;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.services.impl.AlwaysRunningTask;
import com.tle.core.services.impl.SimpleMessage;

/*
 * TODO: Why is this an always-running task? Rather than messages everywhere, it
 * should be two simple SingleShotTasks defined inside OrderService and
 * PurcahseService. There should then be things implementing ScheduledTask that
 * also call those tasks. If the same ID is used for the task, it won't be
 * duplicated if it's already running.
 */
@SuppressWarnings("nls")
@Bind
public class CheckPurchasesTask extends AlwaysRunningTask<SimpleMessage>
{
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private PurchaseService purchasedContentService;
	@Inject
	private OrderService orderService;
	@Inject
	private InstitutionService institutionService;

	@Override
	protected SimpleMessage waitFor() throws Exception
	{
		return waitForMessage();
	}

	@Override
	public void runTask(SimpleMessage msg) throws Exception
	{
		if( msg == null )
		{
			return;
		}

		final PurchaseMessage purchaseMessage = msg.getContents();
		final Institution institution = institutionService.getInstitution(purchaseMessage.getInstitutionId());
		runAs.executeAsSystem(institution, new Callable<Void>()
		{
			@Override
			public Void call()
			{
				switch( purchaseMessage.getType() )
				{
					case ORDERS:
						orderService.checkCurrentOrders();
						break;
					case DOWNLOADS:
						purchasedContentService.checkDownloadableContentAndCheckSubscriptions();
						break;
				}
				return null;
			}
		});
	}

	@Override
	protected String getTitleKey()
	{
		return "com.tle.core.payment.storefront.purchase.task.downloadupdates";
	}
}
