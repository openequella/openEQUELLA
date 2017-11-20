/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.echo.section;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.Maps;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.echo.service.EchoService;
import com.tle.core.guice.Bind;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class EchoServerEditorSection extends AbstractPrototypeSection<EchoServerEditorSection.EchoServerEditorModel>
	implements
		HtmlRenderer,
		ModalEchoServerSection
{
	// Success - All good
	// Fail - Could not reach URLs
	// Invalid - Form data missing
	// Not tested - Has not been tried
	public enum TestStatus
	{
		SUCCESS, FAIL, INVALID, NOTTESTED;

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}

	private static final CssInclude CSS = CssInclude.include(
		ResourcesService.getResourceHelper(EchoServerEditorSection.class).url("css/echoeditor.css")).make();

	@PlugKey("editor.label.page.title.new")
	private static Label LABEL_CREATE_TITLE;
	@PlugKey("editor.label.page.title.edit")
	private static Label LABEL_EDIT_TITLE;
	@PlugKey("editor.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	@PlugKey("editor.validation.")
	private static String KEYPFX_VALIDATE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private ReceiptService receiptService;
	@Inject
	private EchoService echoService;

	@Inject
	private URLCheckerService urlService;

	@TreeLookup
	private OneColumnLayout<?> rootSection;

	@Inject
	@Component(name = "t")
	private MultiEditBox title;
	@Inject
	@Component(name = "d")
	private MultiEditBox description;

	@Component(name = "aurl")
	private TextField applicationUrl;
	@Component(name = "curl")
	private TextField contentUrl;
	@Component(name = "ck")
	private TextField consumerKey;
	@Component(name = "cs")
	private TextField consumerSecret;
	@Component(name = "sn")
	private TextField echoSystemID;

	@Component
	@PlugKey("editor.button.save")
	private Button saveButton;
	@Component
	@PlugKey("echo.editor.button.test")
	private Button testUrlButton;
	@Component
	@PlugKey("editor.button.cancel")
	private Button cancelButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		description.setSize(3);

		testUrlButton.setClickHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("testUrl"),
			"connectionstatus", "controls"));
		saveButton.setClickHandler(events.getNamedHandler("save"));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setPageTitle(getPageTitle(context));

		return new CombinedRenderer(viewFactory.createResult("echoservereditor.ftl", this), CSS);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final EchoServerEditorModel model = getModel(info);
		if( model.isEditing() )
		{
			rootSection.setModalSection(info, this);
		}
	}

	public void createNew(SectionInfo info)
	{
		getModel(info).setEditing(true);
	}

	public void startEdit(SectionInfo info, String echoServerUUID)
	{
		EchoServerEditorModel model = getModel(info);
		model.setEditUuid(echoServerUUID);
		model.setEditing(true);
		model.setTestStatus(TestStatus.NOTTESTED.toString());
		loadDetailsIntoForm(info, echoService.getByUuid(echoServerUUID));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		if( saveEchoSettings(info, false) )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
			SectionUtils.clearModel(info, this);
		}
		else
		{
			info.preventGET();
		}
	}

	private boolean saveEchoSettings(SectionInfo info, boolean testing)
	{
		EchoServerEditorModel model = getModel(info);
		EchoServer es = loadDetailsFromForm(info, new EchoServer());

		try
		{
			validate(info, es);
			boolean test = testEchoUrls(info);
			model.setTestStatus(test ? TestStatus.SUCCESS.toString() : TestStatus.FAIL.toString());

			if( test && !testing )
			{
				String editUuid = model.getEditUuid();
				if( Check.isEmpty(editUuid) )
				{
					es.setUuid(UUID.randomUUID().toString());
					return !Check.isEmpty(echoService.addEchoServer(es));
				}

				echoService.editEchoServer(editUuid, es);
				return true;
			}

			return test;

		}
		catch( InvalidDataException ide )
		{
			// Add errors to model
			List<ValidationError> errors = ide.getErrors();
			for( ValidationError error : errors )
			{
				model.addError(error.getField(),
					new KeyLabel(KEYPFX_VALIDATE + error.getField() + '.' + error.getMessage()));
			}

			if( model.getTestStatus().equals(TestStatus.INVALID.toString()) )
			{
				model.addError("testurls", new KeyLabel(KEYPFX_VALIDATE + "testurls.invalid"));
			}
		}

		return false;
	}

	private void validate(SectionInfo info, EchoServer es)
	{
		try
		{
			echoService.validate(null, es);
		}
		catch( InvalidDataException e )
		{
			getModel(info).setTestStatus(TestStatus.INVALID.toString());
			throw e;
		}
	}

	@EventHandlerMethod
	public void testUrl(SectionInfo info)
	{
		saveEchoSettings(info, true);
	}

	private boolean testEchoUrls(SectionInfo info)
	{
		// Get the URLs
		Map<String, String> urls = Maps.newHashMap();
		urls.put("applicationurl", applicationUrl.getValue(info));
		urls.put("contenturl", contentUrl.getValue(info));

		EchoServerEditorModel model = getModel(info);

		for( Entry<String, String> url : urls.entrySet() )
		{
			ReferencedURL urlStatus = urlService
				.getUrlStatus(url.getValue(), URLCheckMode.RECORDS_FIRST_NON_ITERACTIVE);
			if( !urlStatus.isSuccess() )
			{
				addUrlFailureError(model, urlStatus);
				return false;
			}
		}

		// All ok
		return true;
	}

	private void addUrlFailureError(EchoServerEditorModel model, ReferencedURL urlStatus)
	{
		model.addError("testurls",
			new KeyLabel(KEYPFX_VALIDATE + "testurls.fail", urlStatus.getUrl(), urlStatus.getStatus()));
	}

	private EchoServer loadDetailsFromForm(SectionInfo info, EchoServer es)
	{
		es.setName(LangUtils.convertBeanToBundle(title.getLanguageBundle(info)));
		es.setDescription(LangUtils.convertBeanToBundle(description.getLanguageBundle(info)));
		String appUrl = checkURL(applicationUrl.getValue(info));
		applicationUrl.setValue(info, appUrl);
		es.setApplicationUrl(appUrl);
		String contUrl = checkURL(contentUrl.getValue(info));
		contentUrl.setValue(info, contUrl);
		es.setContentUrl(contUrl);
		es.setConsumerKey(consumerKey.getValue(info));
		es.setConsumerSecret(consumerSecret.getValue(info));
		es.setEchoSystemID(echoSystemID.getValue(info));
		return es;
	}

	// Append trailing slash if missing
	private String checkURL(String url)
	{
		if( !Check.isEmpty(url) )
		{
			return url.endsWith("/") ? url : url + "/";
		}
		return url;
	}

	private void loadDetailsIntoForm(SectionInfo info, EchoServer es)
	{
		final LanguageBundle name = es.getName();
		if( name != null )
		{
			title.setLanguageBundle(info, LangUtils.convertBundleToBean(name));
		}

		final LanguageBundle desc = es.getDescription();
		if( desc != null )
		{
			description.setLanguageBundle(info, LangUtils.convertBundleToBean(desc));
		}

		applicationUrl.setValue(info, es.getApplicationUrl());
		contentUrl.setValue(info, es.getContentUrl());
		consumerKey.setValue(info, es.getConsumerKey());
		consumerSecret.setValue(info, es.getConsumerSecret());
		echoSystemID.setValue(info, es.getEchoSystemID());
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		SectionUtils.clearModel(info, this);
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(getPageTitle(info));
	}

	private Label getPageTitle(SectionInfo info)
	{
		final EchoServerEditorModel model = getModel(info);
		return (!Check.isEmpty(model.getEditUuid()) ? LABEL_EDIT_TITLE : LABEL_CREATE_TITLE);
	}

	@Override
	public Class<EchoServerEditorModel> getModelClass()
	{
		return EchoServerEditorModel.class;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new EchoServerEditorModel();
	}

	public class EchoServerEditorModel
	{
		@Bookmarked(name = "ed")
		private boolean editing;
		@Bookmarked
		private String editUuid;

		private String testStatus;

		private Label pageTitle;

		private final Map<String, Label> errors = new HashMap<String, Label>();

		public void addError(String key, Label value)
		{
			errors.put(key, value);
		}

		public boolean isEditing()
		{
			return editing;
		}

		public void setEditing(boolean editing)
		{
			this.editing = editing;
		}

		public String getEditUuid()
		{
			return editUuid;
		}

		public void setEditUuid(String editUuid)
		{
			this.editUuid = editUuid;
		}

		public Label getPageTitle()
		{
			return pageTitle;
		}

		public void setPageTitle(Label pageTitle)
		{
			this.pageTitle = pageTitle;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
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

	public MultiEditBox getTitle()
	{
		return title;
	}

	public MultiEditBox getDescription()
	{
		return description;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public TextField getApplicationUrl()
	{
		return applicationUrl;
	}

	public TextField getContentUrl()
	{
		return contentUrl;
	}

	public TextField getConsumerKey()
	{
		return consumerKey;
	}

	public TextField getConsumerSecret()
	{
		return consumerSecret;
	}

	public TextField getEchoSystemID()
	{
		return echoSystemID;
	}

	public Button getTestUrlButton()
	{
		return testUrlButton;
	}
}
