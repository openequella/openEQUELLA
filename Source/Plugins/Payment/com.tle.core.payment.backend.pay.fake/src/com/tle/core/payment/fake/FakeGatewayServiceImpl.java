package com.tle.core.payment.fake;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.Sale;
import com.tle.core.guice.Bind;
import com.tle.core.guice.Bindings;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.payment.gateway.PaymentGatewayImplementation;
import com.tle.core.payment.service.SaleService;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;
import com.tle.core.user.CurrentInstitution;

@Bindings({@Bind(FakeGatewayService.class), @Bind(PaymentGatewayImplementation.class)})
@Singleton
@SuppressWarnings("nls")
public class FakeGatewayServiceImpl implements FakeGatewayService, PaymentGatewayImplementation
{
	@Inject
	private SaleService saleService;
	@Inject
	private TaskService taskService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public String testCredentials(PaymentGatewayEditingBean gateway)
	{
		throw new Error("Not supported");
	}

	@Transactional
	@Override
	public void pay(PaymentGateway gateway, final String saleUuid)
	{
		final boolean instant = gateway.getAttribute(FakeGatewayConstants.NODELAY_KEY, false);
		if( instant )
		{
			doPay(saleUuid);
		}
		else
		{
			final Sale sale = saleService.getSale(null, saleUuid);
			saleService.setPending(null, sale);

			final Institution institution = CurrentInstitution.get();
			final BeanClusteredTask task = new BeanClusteredTask("FakeGatewayTask-" + institution.getUniqueId()
				+ sale.getUuid(), FakeGatewayService.class, "createFakeTask", institution, saleUuid);
			taskService.getGlobalTask(task, TimeUnit.SECONDS.toMillis(1));
		}
	}

	@Transactional
	public void doPay(String saleUuid)
	{
		final Sale sale = saleService.getSale(null, saleUuid);
		saleService.commit(null, sale, UUID.randomUUID().toString());
	}

	@Override
	public Task createFakeTask(Institution institution, String saleUuid)
	{
		return new FakeGatewayTask(institution, saleUuid);
	}

	public class FakeGatewayTask extends SingleShotTask
	{
		private final Institution institution;
		private final String saleUuid;

		protected FakeGatewayTask(Institution institution, String saleUuid)
		{
			this.institution = institution;
			this.saleUuid = saleUuid;
		}

		@Override
		public void runTask() throws Exception
		{
			runAs.executeAsSystem(institution, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Thread.sleep(60000);
						doPay(saleUuid);
					}
					catch( InterruptedException ie )
					{
						// Nada
					}
				}
			});
		}

		@Override
		protected String getTitleKey()
		{
			return "com.tle.core.payment.backend.pay.fake.task.title";
		}
	}
}
