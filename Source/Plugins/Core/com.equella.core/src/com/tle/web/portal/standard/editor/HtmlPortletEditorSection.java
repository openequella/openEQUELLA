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

package com.tle.web.portal.standard.editor;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.ScriptContext;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.scripting.ScriptContextFactory;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class HtmlPortletEditorSection
	extends
		AbstractPortletEditorSection<HtmlPortletEditorSection.HtmlPortletEditorModel>
{
	private static final String TYPE = "html";

	@ViewFactory
	private FreemarkerFactory thisView;

	@TreeLookup
	private HtmlEditorInterface htmlEditor;
	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private ScriptingService scriptingService;

	@Override
	protected SectionRenderable customRender(RenderEventContext context, HtmlPortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		String html = portlet.getConfig();
		if( html == null )
		{
			html = Constants.BLANK;
		}
		htmlEditor.setData(context, getHtmlProps(html), getScriptContextFactory());
		model.setEditor(renderSection(context, htmlEditor));

		return thisView.createResult("edit/edithtmlportlet.ftl", context);
	}

	private ScriptContextFactory getScriptContextFactory()
	{
		return new ScriptContextFactory()
		{
			@Override
			public ScriptContext createScriptContext()
			{
				return scriptingService.createScriptContext(new StandardScriptContextParams(null, null, true, null));
			}
		};
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		try
		{
			htmlEditor.setData(info, getHtmlProps(portlet != null ? portlet.getConfig() : Constants.BLANK),
				getScriptContextFactory());
		}
		catch( Exception e )
		{
			SectionUtils.throwRuntime(e);
		}
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		portlet.setConfig(htmlEditor.getHtml(info));
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		try
		{
			htmlEditor.setData(info, getHtmlProps(Constants.BLANK), getScriptContextFactory());
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> getHtmlProps(String html)
	{
		final Map<String, String> properties = new HashMap<String, String>();
		properties.put(HtmlEditorInterface.WIDTH, "640px");
		properties.put(HtmlEditorInterface.HEIGHT, "auto");

		properties.put(HtmlEditorInterface.HTML, html);
		// TleTinyMceAddonConstants.SCRAPBOOK_PICKER_ID,
		// TleTinyMceAddonConstants.FILE_UPLOADER_ID
		properties.put(HtmlEditorInterface.EXCLUDED_ADDONS, "tle_scrapbookpicker,tle_fileuploader");

		return properties;
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing to do here
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.registerSections(htmlEditorService.getEditor(), id);
	}

	@Override
	public Class<HtmlPortletEditorModel> getModelClass()
	{
		return HtmlPortletEditorModel.class;
	}

	public static class HtmlPortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		private SectionRenderable editor;

		public SectionRenderable getEditor()
		{
			return editor;
		}

		public void setEditor(SectionRenderable editor)
		{
			this.editor = editor;
		}
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return thisView.createResult("help/htmlportleteditorhelp.ftl", this);
	}
}
