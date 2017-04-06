package com.tle.web.payment.fake;

import static com.tle.core.payment.PaymentGatewayConstants.SANDBOX_KEY;

import java.util.Map;

import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.guice.Bind;
import com.tle.core.payment.fake.FakeGatewayConstants;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.fake.FakeGatewayEditor.FakeGatewayEditorModel;
import com.tle.web.payment.section.gateway.AbstractGatewayEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class FakeGatewayEditor extends AbstractGatewayEditorSection<FakeGatewayEditorModel>
{
	@Component(stateful = false)
	private Checkbox noDelayMode;

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	protected SectionRenderable renderFields(RenderEventContext context, EntityEditingSession session)
	{
		return view.createResult("fake-editor.ftl", this);
	}

	@Override
	protected void customLoad(SectionInfo info, PaymentGatewayEditingBean gateway)
	{
		noDelayMode.setChecked(info, gateway.getAttribute(FakeGatewayConstants.NODELAY_KEY, false));
	}

	@Override
	protected void customSave(SectionInfo info, PaymentGatewayEditingBean gateway)
	{
		gateway.setAttribute(FakeGatewayConstants.NODELAY_KEY, noDelayMode.isChecked(info));
	}

	@Override
	protected void customValidate(SectionInfo info, PaymentGatewayEditingBean gateway, Map<String, Object> errors)
	{
		// Nada
	}

	@Override
	public boolean isTestable()
	{
		return false;
	}

	@Override
	protected PaymentGateway createNewPaymentGateway()
	{
		final PaymentGateway paymentGateway = new PaymentGateway("fake");
		paymentGateway.setAttribute(SANDBOX_KEY, true);
		return paymentGateway;
	}

	public Checkbox getNoDelayMode()
	{
		return noDelayMode;
	}

	public class FakeGatewayEditorModel
		extends
			AbstractGatewayEditorSection<FakeGatewayEditorModel>.AbstractGatewayEditorModel
	{
		// Nada
	}
}
