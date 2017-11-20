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

package com.tle.web.externaltools.section;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.URLUtils;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.externaltools.service.ExternalToolsService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.service.session.ExternalToolEditingBean;
import com.tle.web.entities.section.AbstractEntityEditor;
import com.tle.web.externaltools.section.ExternalToolEditorSection.ExternalToolEditorModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.ImageRenderer;

@Bind
@SuppressWarnings("nls")
public class ExternalToolEditorSection
	extends
		AbstractEntityEditor<ExternalToolEditingBean, ExternalTool, ExternalToolEditorModel>
{
	@PlugKey("editor.container.option.default")
	private static String DEFAULT_KEY;
	@PlugKey("editor.container.option.embed")
	private static String EMBED_KEY;
	@PlugKey("editor.container.option.newwindow")
	private static String NEW_WINDOW_KEY;
	@PlugKey("editor.error.badurl")
	private static Label LABEL_BAD_URL;

	@Component
	private TextField baseUrl;
	@Component
	private TextField consumerKey;
	@Component
	private TextField sharedSecret;
	@Component
	private TextField customParams;
	@Component
	private TextField iconUrl;
	@PlugKey("editor.name")
	@Component
	private Checkbox shareName;
	@PlugKey("editor.email")
	@Component
	private Checkbox shareEmail;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	private ExternalToolsService toolService;

	@Override
	protected ExternalToolsService getEntityService()
	{
		return toolService;
	}

	@Override
	protected ExternalTool createNewEntity(SectionInfo info)
	{
		return new ExternalTool();
	}

	@Override
	protected SectionRenderable renderFields(RenderEventContext context,
		EntityEditingSession<ExternalToolEditingBean, ExternalTool> session)
	{
		return view.createResult("edittool.ftl", context);
	}

	@Override
	public void registered(String id, com.tle.web.sections.SectionTree tree)
	{
		super.registered(id, tree);
		SimpleHtmlListModel<NameValue> listModel = new SimpleHtmlListModel<NameValue>();
		listModel.add(new BundleNameValue(DEFAULT_KEY, "default"));
		listModel.add(new BundleNameValue(EMBED_KEY, "embed"));
		listModel.add(new BundleNameValue(NEW_WINDOW_KEY, "window"));
	}

	@Override
	protected void loadFromSession(SectionInfo info, EntityEditingSession<ExternalToolEditingBean, ExternalTool> session)
	{
		ExternalToolEditingBean bean = session.getBean();
		if( bean != null )
		{
			baseUrl.setValue(info, bean.getBaseURL());
			consumerKey.setValue(info, bean.getConsumerKey());
			sharedSecret.setValue(info, bean.getSharedSecret());
			if( bean.getCustomParams() != null )
			{
				customParams.setValue(info, toolService.customParamListToString(bean.getCustomParams()));
			}
			else
			{
				customParams.setValue(info, null);
			}
			String iconUrlStr = bean.getAttribute(ExternalToolConstants.ICON_URL);
			iconUrl.setValue(info, iconUrlStr);
			if( !Check.isEmpty(iconUrlStr) )
			{
				getModel(info).setThumbnailRenderer(new ImageRenderer(iconUrlStr, null));
			}
			else
			{
				getModel(info).setThumbnailRenderer(null);
			}
			shareName.setChecked(info, bean.isShareName());
			shareEmail.setChecked(info, bean.isShareEmail());
		}
	}

	@Override
	protected void saveToSession(SectionInfo info, EntityEditingSession<ExternalToolEditingBean, ExternalTool> session,
		boolean validate)
	{
		final ExternalToolEditingBean bean = session.getBean();
		String baseUrlStr = baseUrl.getValue(info);
		bean.setBaseURL(baseUrlStr);
		bean.setConsumerKey(consumerKey.getValue(info));
		bean.setSharedSecret(sharedSecret.getValue(info));
		bean.setCustomParams(toolService.parseCustomParamsString(customParams.getValue(info)));
		String iconUrlStr = iconUrl.getValue(info);
		bean.setAttribute(ExternalToolConstants.ICON_URL, iconUrlStr);
		bean.setShareName(shareName.isChecked(info));
		bean.setShareEmail(shareEmail.isChecked(info));
	}

	/**
	 * In addition to validating the mandatory fields, we also ensure any URLs
	 * actually entered are prima facie valid. Errors are duplicated in the
	 * model to facilitate rendering in the ftl
	 */
	@Override
	protected void validate(SectionInfo info, EntityEditingSession<ExternalToolEditingBean, ExternalTool> session)
	{
		super.validate(info, session);
		final Map<String, Object> modelErrors = getModel(info).getErrors();
		modelErrors.remove("errors.baseurl");
		modelErrors.remove("badiconurl");
		String baseUrlStr = baseUrl.getValue(info);
		if( !Check.isEmpty(baseUrlStr) && !URLUtils.isAbsoluteUrl(baseUrlStr) )
		{
			modelErrors.put("errors.baseurl", LABEL_BAD_URL);
		}
		// Validate the icon URL if it's not empty
		String iconUrlStr = iconUrl.getValue(info);
		if( !Check.isEmpty(iconUrlStr) && !URLUtils.isAbsoluteUrl(iconUrlStr) )
		{
			modelErrors.put("badiconurl", LABEL_BAD_URL);
		}
		session.getValidationErrors().putAll(getModel(info).getErrors());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ExternalToolEditorModel();
	}

	@Override
	public Class<ExternalToolEditorModel> getModelClass()
	{
		return ExternalToolEditorModel.class;
	}

	public class ExternalToolEditorModel
		extends
			AbstractEntityEditor<ExternalToolEditingBean, ExternalTool, ExternalToolEditorModel>.AbstractEntityEditorModel
	{
		private SectionRenderable thumbnailRenderer;

		public SectionRenderable getThumbnailRenderer()
		{
			return thumbnailRenderer;
		}

		public void setThumbnailRenderer(SectionRenderable thumbnailRenderer)
		{
			this.thumbnailRenderer = thumbnailRenderer;
		}
	}

	public TextField getBaseUrl()
	{
		return baseUrl;
	}

	public TextField getConsumerKey()
	{
		return consumerKey;
	}

	public TextField getSharedSecret()
	{
		return sharedSecret;
	}

	public TextField getCustomParams()
	{
		return customParams;
	}

	public TextField getIconUrl()
	{
		return iconUrl;
	}

	public Checkbox getShareName()
	{
		return shareName;
	}

	public Checkbox getShareEmail()
	{
		return shareEmail;
	}

	public ExternalToolsService getToolService()
	{
		return toolService;
	}
}
