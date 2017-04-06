package com.tle.web.payment.paypal;

import static com.tle.core.payment.PaymentGatewayConstants.SANDBOX_KEY;
import static com.tle.core.payment.paypal.PaypalConstants.USERNAME_KEY;

import java.util.Map;

import com.tle.common.Check;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.payment.paypal.PaypalGatewayEditor.PaypalGatewayEditorModel;
import com.tle.web.payment.section.gateway.AbstractGatewayEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class PaypalGatewayEditor extends AbstractGatewayEditorSection<PaypalGatewayEditorModel>
{
	@PlugKey("editor.error.username")
	private static Label LABEL_ERROR_USERNAME;

	@Component(stateful = false)
	private TextField apiUsername;
	@Component(stateful = false)
	private Checkbox sandboxMode;

	@ViewFactory
	private FreemarkerFactory view;

	@SuppressWarnings("nls")
	@Override
	protected SectionRenderable renderFields(RenderEventContext context, EntityEditingSession session)
	{
		return view.createResult("paypal-editor.ftl", this);
	}

	@SuppressWarnings("nls")
	@Override
	protected void customValidate(SectionInfo info, PaymentGatewayEditingBean gateway, Map<String, Object> errors)
	{
		String username = gateway.getAttribute(USERNAME_KEY);
		if( Check.isEmpty(username) )
		{
			errors.put("username", LABEL_ERROR_USERNAME.getText());
		}
	}

	@Override
	protected void customLoad(SectionInfo info, PaymentGatewayEditingBean gateway)
	{
		apiUsername.setValue(info, gateway.getAttribute(USERNAME_KEY));
		sandboxMode.setChecked(info, gateway.getAttribute(SANDBOX_KEY, true));
	}

	@Override
	protected void customSave(SectionInfo info, PaymentGatewayEditingBean gateway)
	{
		gateway.setAttribute(USERNAME_KEY, apiUsername.getValue(info));
		gateway.setAttribute(SANDBOX_KEY, sandboxMode.isChecked(info));
		gateway.setType("paypal"); //$NON-NLS-1$
	}

	@Override
	public boolean isTestable()
	{
		return false;
	}

	@Override
	protected PaymentGateway createNewPaymentGateway()
	{
		return new PaymentGateway("paypal"); //$NON-NLS-1$
	}

	public TextField getApiUsername()
	{
		return apiUsername;
	}

	public Checkbox getSandboxMode()
	{
		return sandboxMode;
	}

	public class PaypalGatewayEditorModel
		extends
			AbstractGatewayEditorSection<PaypalGatewayEditorModel>.AbstractGatewayEditorModel
	{

	}

}
