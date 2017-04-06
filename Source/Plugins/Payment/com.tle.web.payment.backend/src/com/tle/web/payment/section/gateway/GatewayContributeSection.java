package com.tle.web.payment.section.gateway;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.gateway.GatewayTypeDescriptor;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.session.PaymentGatewayEditingBean;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.entities.section.AbstractEntityContributeSection;
import com.tle.web.entities.section.EntityEditor;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleNameValue;
import com.tle.web.payment.section.gateway.GatewayContributeSection.GatewayContributeModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

@TreeIndexed
public class GatewayContributeSection
	extends
		AbstractEntityContributeSection<PaymentGatewayEditingBean, PaymentGateway, GatewayContributeModel>

{
	@PlugKey("gateway.title.creating")
	private static Label LABEL_CREATING;
	@PlugKey("gateway.title.editing")
	private static Label LABEL_EDITING;
	@PlugKey("gateway.option.choosetype")
	private static String CHOOSE_TYPE_KEY;

	@Inject
	private PaymentGatewayService gatewayService;

	private PluginTracker<EntityEditor<PaymentGatewayEditingBean, PaymentGateway>> editorTracker;

	@Component(name = "gt")
	private SingleSelectionList<GatewayTypeDescriptor> gatewayTypes;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;
	@AjaxFactory
	private AjaxGenerator ajax;

	private UpdateDomFunction updateFunction;
	private Map<String, EntityEditor<PaymentGatewayEditingBean, PaymentGateway>> editorMap;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		gatewayTypes.setListModel(new DynamicHtmlListModel<GatewayTypeDescriptor>()
		{
			@Override
			protected Iterable<GatewayTypeDescriptor> populateModel(SectionInfo info)
			{
				return gatewayService.listAllAvailableTypes();
			}

			@Override
			protected Option<GatewayTypeDescriptor> getTopOption()
			{
				return new KeyOption<GatewayTypeDescriptor>(CHOOSE_TYPE_KEY, "", null);
			}

			@Override
			protected Option<GatewayTypeDescriptor> convertToOption(SectionInfo info, GatewayTypeDescriptor obj)
			{
				return new NameValueOption<GatewayTypeDescriptor>(new BundleNameValue(obj.getNameKey(), obj.getType()),
					obj);
			}
		});

		updateFunction = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("typeSelected"),
			"gatewayEditor", "actions");
		gatewayTypes.addChangeEventHandler(new OverrideHandler(updateFunction));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		GatewayContributeModel model = getModel(context);

		model.setPageTitle(getPageTitle(context));

		final EntityEditor<PaymentGatewayEditingBean, PaymentGateway> ed = getEditor(context);
		if( ed != null )
		{
			// check edit priv
			final PaymentGatewayEditingBean editedGateway = ed.getEditedEntity(context);
			if( editedGateway.getId() == 0 )
			{
				ensureCreatePriv(context);
			}
			else if( !canEdit(context, editedGateway) )
			{
				throw accessDenied(getEditPriv());
			}

			HelpAndScreenOptionsSection.addHelp(context, ed.renderHelp(context));
			model.setEditorRenderable(ed.renderEditor(context));
		}
		else
		{
			ensureCreatePriv(context);
		}

		final GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", view.createResult("editgateway.ftl", context));
		return templateResult;
	}

	@Override
	protected AbstractEntityService<PaymentGatewayEditingBean, PaymentGateway> getEntityService()
	{
		return gatewayService;
	}

	@Override
	protected Collection<EntityEditor<PaymentGatewayEditingBean, PaymentGateway>> getAllEditors()
	{
		editorMap = editorTracker.getNewBeanMap();
		return editorMap.values();
	}

	@Override
	protected Label getCreatingLabel(SectionInfo info)
	{
		return LABEL_CREATING;
	}

	@Override
	protected Label getEditingLabel(SectionInfo info)
	{
		return LABEL_EDITING;
	}

	@Override
	protected EntityEditor<PaymentGatewayEditingBean, PaymentGateway> getEditor(SectionInfo info)
	{
		final String type = gatewayTypes.getSelectedValueAsString(info);
		final GatewayContributeModel model = getModel(info);
		if( !Check.isEmpty(type) )
		{
			final EntityEditor<PaymentGatewayEditingBean, PaymentGateway> ed = editorMap.get(type);
			model.setEditor(ed);
			return ed;
		}
		return null;
	}

	@EventHandlerMethod
	public void typeSelected(SectionInfo info)
	{
		final EntityEditor<PaymentGatewayEditingBean, PaymentGateway> ed = getEditor(info);
		if( ed != null )
		{
			// start new session
			ed.create(info);
		}
	}

	@Override
	public void startEdit(SectionInfo info, String uuid, boolean clone)
	{
		final PaymentGateway connector = gatewayService.getForEdit(uuid);
		final String type = connector.getGatewayType();
		GatewayContributeModel model = getModel(info);
		final EntityEditor<PaymentGatewayEditingBean, PaymentGateway> ed = editorMap.get(type);
		model.setEditor(ed);
		model.setEditing(true);
		ed.edit(info, uuid, clone);
		gatewayTypes.setSelectedStringValue(info, type);
	}

	@Override
	public void returnFromEdit(SectionInfo info, boolean cancelled)
	{
		super.returnFromEdit(info, cancelled);
		gatewayTypes.setSelectedStringValue(info, null);
	}

	@Override
	protected String getCreatePriv()
	{
		return PaymentConstants.PRIV_CREATE_PAYMENT_GATEWAY;
	}

	@Override
	protected String getEditPriv()
	{
		return PaymentConstants.PRIV_EDIT_PAYMENT_GATEWAY;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		editorTracker = new PluginTracker<EntityEditor<PaymentGatewayEditingBean, PaymentGateway>>(pluginService,
			GatewayContributeSection.class, "paymentGatewayEditor", "id");
		editorTracker.setBeanKey("class");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new GatewayContributeModel(info);
	}

	public SingleSelectionList<GatewayTypeDescriptor> getGatewayTypes()
	{
		return gatewayTypes;
	}

	public class GatewayContributeModel
		extends
			AbstractEntityContributeSection<PaymentGatewayEditingBean, PaymentGateway, GatewayContributeModel>.EntityContributeModel
	{
		private final SectionInfo info;
		private EntityEditor<PaymentGatewayEditingBean, PaymentGateway> editor;

		public GatewayContributeModel(SectionInfo info)
		{
			this.info = info;
		}

		@Override
		public EntityEditor<PaymentGatewayEditingBean, PaymentGateway> getEditor()
		{
			final String type = getGatewayTypes().getSelectedValueAsString(info);
			if( editor == null && type != null )
			{
				editor = editorMap.get(type);
			}
			return editor;
		}

	}
}
