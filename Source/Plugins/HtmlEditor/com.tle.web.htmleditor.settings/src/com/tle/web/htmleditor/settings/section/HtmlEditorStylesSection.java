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

import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
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
public class HtmlEditorStylesSection extends AbstractPrototypeSection<HtmlEditorStylesSection.HtmlEditorStylesModel>
	implements
		HtmlRenderer,
		ModalHtmlEditorSettingsSection
{
	@PlugKey("settings.styles.front.link")
	private static Label LABEL_SETTING_LINK;
	@PlugKey("settings.styles.front.preamble")
	private static Label LABEL_SETTING_BLURB;

	@PlugKey("settings.styles.title")
	private static Label LABEL_TITLE;
	@PlugKey("settings.styles.confirm.navigateaway")
	private static Label LABEL_CONFIRM_NAVIGATE_AWAY;

	@TreeLookup
	private HtmlEditorSettingsFrontPageSection front;

	@Component(name = "css", stateful = false)
	private CodeMirror editor;
	@PlugKey("settings.styles.button.save")
	@Component(name = "s")
	private Button saveButton;
	@PlugKey("settings.styles.button.cancel")
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
		editor.setValue(context, htmlEditorService.getStylesheetContents());
		return view.createResult("setting/styles.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		editor.setEditorType(EditorType.CSS_EDITOR);
		editor.setAllowFullScreen(true);
		editor.setShowHelp(true);
		saveButton.setClickHandler(events.getNamedHandler("save"));
		saveButton.addEventStatements(JSHandler.EVENT_BEFOREUNLOAD, new ReturnStatement(LABEL_CONFIRM_NAVIGATE_AWAY));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		htmlEditorService.setStylesheetContents(editor.getValue(info));
		front.returnToFrontPage(info);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
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
		return new HtmlEditorStylesModel();
	}

	@Override
	public void startSession(SectionInfo info)
	{
		// No. But maybe later.
	}

	@Override
	@Nullable
	public SettingInfo getSettingInfo(SectionInfo info)
	{
		return new SettingInfo("styles", LABEL_SETTING_LINK, LABEL_SETTING_BLURB);
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

	public static class HtmlEditorStylesModel
	{
		// No
	}
}
