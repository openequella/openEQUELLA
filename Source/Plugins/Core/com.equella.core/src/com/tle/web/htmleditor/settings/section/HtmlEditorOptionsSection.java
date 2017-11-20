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

package com.tle.web.htmleditor.settings.section;

import javax.inject.Inject;

import com.tle.common.beans.exception.InvalidDataException;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorConfigurationEditingSession;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.CodeMirror;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class HtmlEditorOptionsSection extends AbstractPrototypeSection<HtmlEditorOptionsSection.HtmlEditorOptionsModel>
	implements
		HtmlRenderer,
		ModalHtmlEditorSettingsSection
{
	@PlugKey("settings.editoroptions.front.link")
	private static Label LABEL_SETTING_LINK;
	@PlugKey("settings.editoroptions.front.preamble")
	private static Label LABEL_SETTING_BLURB;

	@PlugKey("settings.editoroptions.title")
	private static Label LABEL_TITLE;
	@PlugKey("settings.editoroptions.confirm.navigateaway")
	private static Label LABEL_CONFIRM_NAVIGATE_AWAY;

	@TreeLookup
	private HtmlEditorSettingsFrontPageSection front;

	@Component(name = "opt", stateful = false)
	private CodeMirror editor;
	@PlugKey("settings.editoroptions.button.save")
	@Component(name = "s")
	private Button saveButton;
	@PlugKey("settings.editoroptions.button.cancel")
	@Component(name = "c")
	private Button cancelButton;

	@Inject
	private HtmlEditorService htmlEditorService;
	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context)
	{
		final HtmlEditorOptionsModel model = getModel(context);
		final HtmlEditorConfigurationEditingSession session = htmlEditorService.getEditorConfigEditingSession(model
			.getSessionId());
		editor.setValue(context, session.getConfig().getEditorOptions());
		return view.createResult("setting/editoroptions.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		editor.setEditorType(EditorType.JAVASCRIPT_EDITOR);
		editor.setAllowFullScreen(true);
		editor.setShowHelp(true);
		saveButton.setClickHandler(events.getNamedHandler("save"));
		saveButton.addEventStatements(JSHandler.EVENT_BEFOREUNLOAD, new ReturnStatement(LABEL_CONFIRM_NAVIGATE_AWAY));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final HtmlEditorOptionsModel model = getModel(info);

		final String sessionId = model.getSessionId();
		final HtmlEditorConfigurationEditingSession session = htmlEditorService
			.getEditorConfigEditingSession(sessionId);
		final String editorOptions = editor.getValue(info);
		session.getConfig().setEditorOptions(editorOptions);

		// validate the JSON
		try
		{
			if( editorOptions.trim().length() != 0 )
			{
				htmlEditorService.validateEditorOptions(editorOptions);
			}
		}
		catch( InvalidDataException ide )
		{
			model.setError(ide.getMessage());
			info.preventGET();
			return;
		}

		htmlEditorService.commitEditorConfigEditingSession(sessionId);
		model.setSessionId(null);
		front.returnToFrontPage(info);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		final HtmlEditorOptionsModel model = getModel(info);
		model.setSessionId(null);
		front.returnToFrontPage(info);
	}

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		crumbs.setForcedLastCrumb(LABEL_TITLE);
		decorations.setTitle(LABEL_TITLE);
		decorations.setContentBodyClass("htmleditor");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new HtmlEditorOptionsModel();
	}

	@Override
	public void startSession(SectionInfo info)
	{
		final HtmlEditorConfigurationEditingSession session = htmlEditorService.createEditorConfigEditingSession();
		getModel(info).setSessionId(session.getSessionId());
	}

	@Override
	@Nullable
	public SettingInfo getSettingInfo(SectionInfo info)
	{
		return new SettingInfo("options", LABEL_SETTING_LINK, LABEL_SETTING_BLURB);
	}

	public CodeMirror getEditor()
	{
		return editor;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public static class HtmlEditorOptionsModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		private String error;

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}
	}
}
