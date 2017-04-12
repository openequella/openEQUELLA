package com.tle.web.pss.settings;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.system.PearsonScormServicesSettings;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.pss.service.PearsonScormServicesService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.pss.privileges.PearsonScormServicesSettingsPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@Bind
@SuppressWarnings("nls")
public class PearsonScormServicesSettingsEditorSection
	extends
		OneColumnLayout<PearsonScormServicesSettingsEditorSection.PearsonScormServicesSettingsModel>
{
	private static final CssInclude CSS = CssInclude.include(
		ResourcesService.getResourceHelper(PearsonScormServicesSettingsEditorSection.class).url("css/pss-editor.css"))
		.make();

	private static final String AJAX_ALL = "overallajaxdiv";

	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("editor.validation.")
	private static String KEYPFX_VALIDATE;
	@PlugKey("editor.test.failure")
	private static Label TEST_CONNECTION_FAILURE;
	@PlugKey("editor.test.nottested")
	private static Label TEST_CONNECTION_NOTTESTED;

	@Component(name = "scenb", parameter = "scenbk", supported = true)
	protected Checkbox enable;

	@Component
	private TextField baseUrl;
	@Component
	private TextField accountNamespace;
	@Component
	private TextField consumerKey;
	@Component
	private TextField consumerSecret;

	@Component
	@PlugKey("settings.test.button")
	private Button testButton;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@Inject
	private ConfigurationService configService;
	@Inject
	private PearsonScormServicesSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private PearsonScormServicesService pssService;

	@EventFactory
	private EventGenerator events;
	@Inject
	private ReceiptService receiptService;
	@AjaxFactory
	protected AjaxGenerator ajax;

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		UpdateDomFunction ajaxUpdate = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("toggleEnabled"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), AJAX_ALL);
		enable.setClickHandler(ajaxUpdate);
		testButton.setClickHandler(new StatementHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("testConnection"), ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING),
			"connectionstatus", "controls", "testbutton")));
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		securityProvider.checkAuthorised();
		PearsonScormServicesSettingsModel model = getModel(context);
		PearsonScormServicesSettings settings = configService.getProperties(new PearsonScormServicesSettings());

		if( !model.isLoaded() )
		{
			enable.setChecked(context, settings.isEnable());
			model.setShowControls(settings.isEnable());
			baseUrl.setValue(context, settings.getBaseUrl());
			accountNamespace.setValue(context, settings.getAccountNamespace());
			consumerKey.setValue(context, settings.getConsumerKey());
			consumerSecret.setValue(context, settings.getConsumerSecret());
			model.setLoaded(true);
		}
		else
		{
			model.setShowControls(enable.isChecked(context));
		}

		GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(BODY, new CombinedRenderer(viewFactory.createResult("pss-settings.ftl", this),
			CSS));
		return templateResult;
	}

	@EventHandlerMethod
	public void testConnection(SectionInfo info)
	{
		getModel(info).setSuccessful(saveSystemConstants(info, true));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		PearsonScormServicesSettingsModel model = getModel(info);
		if( (!enable.isChecked(info) || model.isSuccessful()) && saveSystemConstants(info, false) )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
			model.setLoaded(false);
		}
		else
		{
			model.addError("nottested", TEST_CONNECTION_NOTTESTED);
			info.preventGET();
		}
	}

	private boolean testPSSConnection(SectionInfo info, PearsonScormServicesSettings settings)
	{
		boolean success = false;
		success = pssService.pingConnection(settings);
		if( !success )
		{
			getModel(info).addError("connectiontest", TEST_CONNECTION_FAILURE);
		}
		return success;
	}

	private PearsonScormServicesSettings getDetailsFromForm(SectionInfo info)
	{
		PearsonScormServicesSettings settings = new PearsonScormServicesSettings();
		settings.setEnable(enable.isChecked(info));
		String url = baseUrl.getValue(info);
		if( !Check.isEmpty(url) )
		{
			url = url.endsWith("/") ? url : url + "/";
		}
		settings.setBaseUrl(url);
		settings.setConsumerKey(consumerKey.getValue(info));
		settings.setConsumerSecret(consumerSecret.getValue(info));
		settings.setAccountNamespace(accountNamespace.getValue(info));
		return settings;
	}

	private boolean saveSystemConstants(SectionInfo info, boolean test)
	{
		PearsonScormServicesSettingsModel model = getModel(info);

		try
		{
			PearsonScormServicesSettings settings = getDetailsFromForm(info);

			if( !enable.isChecked(info) )
			{
				settings.setEnable(false);
				configService.setProperties(settings);
				return true;
			}

			if( !test )
			{
				// Save
				doValidation(settings);
				testPSSConnection(info, settings);
				if( Check.isEmpty(model.getErrors()) )
				{
					configService.setProperties(settings);
					return true;
				}
				return false;
			}

			doValidation(settings);
			return testPSSConnection(info, settings);
		}
		catch( InvalidDataException ide )
		{
			List<ValidationError> errors = ide.getErrors();
			for( ValidationError error : errors )
			{
				model.addError(error.getField(),
					new KeyLabel(KEYPFX_VALIDATE + error.getField() + '.' + error.getMessage()));
			}
		}

		return false;
	}

	private void doValidation(PearsonScormServicesSettings settings) throws InvalidDataException
	{
		List<ValidationError> errors = Lists.newArrayList();
		String url = settings.getBaseUrl();
		addIfInvalid(errors, !URLUtils.isAbsoluteUrl(url), "baseurl");
		addIfEmpty(errors, Check.isEmpty(url), "baseurl");
		addIfEmpty(errors, Check.isEmpty(settings.getConsumerKey()), "key");
		addIfEmpty(errors, Check.isEmpty(settings.getConsumerSecret()), "secret");
		addIfEmpty(errors, Check.isEmpty(settings.getAccountNamespace()), "namespace");

		if( !errors.isEmpty() )
		{
			throw new InvalidDataException(errors);
		}
	}

	private void addIfEmpty(List<ValidationError> errors, boolean empty, String field)
	{
		if( empty )
		{
			errors.add(new ValidationError(field, "mandatory"));
		}
	}

	private void addIfInvalid(List<ValidationError> errors, boolean invalid, String field)
	{
		if( invalid )
		{
			errors.add(new ValidationError(field, "invalid"));
		}
	}

	public Checkbox getEnable()
	{
		return enable;
	}

	public TextField getBaseUrl()
	{
		return baseUrl;
	}

	public TextField getAccountNamespace()
	{
		return accountNamespace;
	}

	public TextField getConsumerKey()
	{
		return consumerKey;
	}

	public TextField getConsumerSecret()
	{
		return consumerSecret;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info)
	{
		boolean show = enable.isChecked(info);
		getModel(info).setShowControls(show);
	}

	@Override
	public Class<PearsonScormServicesSettingsModel> getModelClass()
	{
		return PearsonScormServicesSettingsModel.class;
	}

	public static class PearsonScormServicesSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked
		private boolean loaded;
		@Bookmarked
		private boolean successful;

		private final Map<String, Label> errors = Maps.newHashMap();
		private boolean showControls;

		public void addError(String key, Label value)
		{
			this.errors.put(key, value);
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}

		public boolean isShowControls()
		{
			return showControls;
		}

		public void setShowControls(boolean showControls)
		{
			this.showControls = showControls;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public boolean isSuccessful()
		{
			return successful;
		}

		public void setSuccessful(boolean successful)
		{
			this.successful = successful;
		}
	}

	public Button getTestButton()
	{
		return testButton;
	}

	public void setTestButton(Button testButton)
	{
		this.testButton = testButton;
	}
}
