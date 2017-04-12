package com.tle.web.payment.section.gateway;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.PaymentGatewayConstants;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.services.UrlService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingSession;
import com.tle.web.DebugSettings;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public abstract class AbstractGatewayEditorSection<M extends AbstractGatewayEditorSection<M>.AbstractGatewayEditorModel>
	extends
		AbstractEntityEditor<PaymentGatewayEditingBean, PaymentGateway, M>
{
	@PlugKey("gateway.edit.label.enable")
	private static Label LABEL_ENABLE;
	@PlugKey("gateway.edit.label.test.error")
	private static Label LABEL_TEST_ERROR;
	@PlugKey("gateway.edit.label.must.test")
	private static Label LABEL_MUST_TEST;
	@PlugKey("gateway.edit.google.help")
	private static String GOOGLE_HELP;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	protected EventGenerator events;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Inject
	protected PaymentGatewayService gatewayService;
	@Inject
	private UrlService urlService;
	@Component(name = "e", stateful = false)
	private Checkbox enabled;

	@PlugKey("gateway.edit.button.test")
	@Component
	private Button testButton;

	protected abstract void customValidate(SectionInfo info, PaymentGatewayEditingBean gateway,
		Map<String, Object> errors);

	protected abstract void customLoad(SectionInfo info, PaymentGatewayEditingBean gateway);

	protected abstract void customSave(SectionInfo info, PaymentGatewayEditingBean gateway);

	protected abstract PaymentGateway createNewPaymentGateway();

	public abstract boolean isTestable();

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);

		testButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("testGateway"),
			"testDiv"));
	}

	@Override
	public final SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final M model = getModel(context);
		EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session = gatewayService.loadSession(model
			.getSessionId());

		model.setEntityUuid(session.getBean().getUuid());
		model.setErrors(session.getValidationErrors());
		model.setCustomEditor(renderFields(context, session));
		model.setStoreRedirectUrl(new KeyLabel(GOOGLE_HELP, urlService.institutionalise("/google-notification/")));

		if( !DebugSettings.isAutoTestMode() )
		{
			context.getBody().addEventStatements(JSHandler.EVENT_BEFOREUNLOAD,
				new ReturnStatement(getWarningNavigateAway()));
		}

		return view.createResult("editgatewaycommon.ftl", context);
	}

	@EventHandlerMethod
	public void testGateway(SectionInfo info)
	{
		M model = getModel(info);
		EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session = gatewayService.loadSession(model
			.getSessionId());
		PaymentGatewayEditingBean bean = session.getBean();

		String result = gatewayService.testGateway(bean);

		if( Check.isEmpty(result) )
		{
			getModel(info).setTestStatus("ok");
			bean.setAttribute(PaymentGatewayConstants.FIELD_TESTED, true);
		}
		else
		{
			getModel(info).setTestStatus("fail");
			bean.setAttribute(PaymentGatewayConstants.FIELD_TESTED, false);
			session.getValidationErrors()
				.put("testgateway", AppendedLabel.get(LABEL_TEST_ERROR, new TextLabel(result)));
		}
	}

	@Override
	protected AbstractEntityService<PaymentGatewayEditingBean, PaymentGateway> getEntityService()
	{
		return gatewayService;
	}

	@Override
	protected PaymentGateway createNewEntity(SectionInfo info)
	{
		return createNewPaymentGateway();
	}

	@Override
	protected void loadFromSession(SectionInfo info,
		EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session)
	{
		PaymentGatewayEditingBean gatewayBean = session.getBean();
		enabled.setChecked(info, gatewayBean.isEnabled());
		customLoad(info, gatewayBean);
	}

	@Override
	protected void saveToSession(SectionInfo info,
		EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session, boolean validate)
	{
		final PaymentGatewayEditingBean gatewayBean = session.getBean();
		gatewayBean.setEnabled(enabled.isChecked(info));
		customSave(info, gatewayBean);
	}

	@Override
	protected void validate(SectionInfo info, EntityEditingSession<PaymentGatewayEditingBean, PaymentGateway> session)
	{
		final PaymentGatewayEditingBean gateway = session.getBean();

		super.validate(info, session);

		if( isTestable() )
		{
			if( !gateway.getAttribute(PaymentGatewayConstants.FIELD_TESTED, false) )
			{
				session.getValidationErrors().put("testgateway", LABEL_MUST_TEST);
			}
		}

		customValidate(info, gateway, session.getValidationErrors());
	}

	public Label getEnabledLabel()
	{
		return LABEL_ENABLE;
	}

	public Checkbox getEnabled()
	{
		return enabled;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new AbstractGatewayEditorModel();
	}

	public Button getTestButton()
	{
		return testButton;
	}

	public class AbstractGatewayEditorModel
		extends
			AbstractEntityEditor<PaymentGatewayEditingBean, PaymentGateway, M>.AbstractEntityEditorModel
	{
		private String testStatus;
		private KeyLabel storeRedirectUrl;

		public KeyLabel getStoreRedirectUrl()
		{
			return storeRedirectUrl;
		}

		public void setStoreRedirectUrl(KeyLabel storeRedirectUrl)
		{
			this.storeRedirectUrl = storeRedirectUrl;
		}

		public String getTestStatus()
		{
			return testStatus;
		}

		public void setTestStatus(String testStatus)
		{
			this.testStatus = testStatus;
		}
	}
}
