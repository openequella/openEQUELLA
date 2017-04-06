package com.tle.web.payment.fake.section;

import javax.inject.Inject;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.fake.FakeGatewayService;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.web.payment.fake.section.FakeCheckoutSection.FakeCheckoutModel;
import com.tle.web.payment.section.pay.AbstractCheckoutSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.AfterParametersListener;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.RenderEventContext;

public class FakeCheckoutSection extends AbstractCheckoutSection<FakeCheckoutModel> implements AfterParametersListener
{
	@Inject
	private FakeGatewayService fakeGatewayService;
	@Inject
	private PaymentGatewayService gatewayService;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return null;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new FakeCheckoutModel();
	}

	@Override
	public void afterParameters(SectionInfo info, ParametersEvent event)
	{
		final FakeCheckoutModel model = getModel(info);
		final PaymentGateway gateway = gatewayService.getByUuid(model.getGatewayUuid());

		fakeGatewayService.pay(gateway, model.getCheckoutUuid());

		// redirect back to store front
		info.forwardToUrl(model.getReturnUrl());
	}

	public static class FakeCheckoutModel extends AbstractCheckoutSection.AbstractCheckoutModel
	{
		// Nada
	}
}
