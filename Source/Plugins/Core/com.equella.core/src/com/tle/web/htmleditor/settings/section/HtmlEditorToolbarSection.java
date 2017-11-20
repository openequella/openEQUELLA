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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.annotation.Nullable;
import com.tle.common.htmleditor.HtmlEditorConfiguration;
import com.tle.common.htmleditor.HtmlEditorToolbarConfig;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.HtmlEditorConfigurationEditingSession;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQuerySortable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class HtmlEditorToolbarSection extends AbstractPrototypeSection<HtmlEditorToolbarSection.HtmlEditorToolbarModel>
	implements
		HtmlRenderer,
		ModalHtmlEditorSettingsSection
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(HtmlEditorToolbarSection.class);

	private static final ExternallyDefinedFunction SETUP_TOOLBAR = new ExternallyDefinedFunction("setupToolbar",
		JQuerySortable.PRERENDER, new IncludeFile(resources.url("scripts/toolbar.js")));

	@PlugKey("settings.toolbar.front.link")
	private static Label LABEL_SETTING_LINK;
	@PlugKey("settings.toolbar.front.preamble")
	private static Label LABEL_SETTING_BLURB;

	@PlugKey("settings.toolbar.title")
	private static Label LABEL_TITLE;
	@PlugKey("settings.toolbar.confirm.navigateaway")
	private static Label LABEL_CONFIRM_NAVIGATE_AWAY;
	@PlugKey("settings.toolbar.confirm.reset")
	private static Label LABEL_CONFIRM_RESET;
	@PlugKey("settings.toolbar.confirm.clear")
	private static Label LABEL_CONFIRM_CLEAR;

	@Component(name = "ct")
	private Div containerDiv;
	@PlugKey("settings.toolbar.button.reset")
	@Component(name = "r")
	private Button resetButton;
	@PlugKey("settings.toolbar.button.clear")
	@Component(name = "cl")
	private Button clearButton;
	@PlugKey("settings.toolbar.button.save")
	@Component(name = "s")
	private Button saveButton;
	@PlugKey("settings.toolbar.button.cancel")
	@Component(name = "c")
	private Button cancelButton;

	@TreeLookup
	private HtmlEditorSettingsFrontPageSection front;

	@Inject
	private HtmlEditorService htmlEditorService;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;

	private JSCallable toolbarMovedFunction;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context)
	{
		final HtmlEditorToolbarModel model = getModel(context);

		final Map<String, HtmlEditorButtonDefinition> buttonMap = htmlEditorService.getEditor().getAllButtons(context);
		final HtmlEditorConfigurationEditingSession session = htmlEditorService.getEditorConfigEditingSession(model
			.getSessionId());
		final HtmlEditorConfiguration editingConfig = session.getConfig();
		final boolean dirty = session.isDirty();

		model.setDirty(dirty);

		final Set<String> usedButtons = Sets.newHashSet();
		final List<ToolbarRowRenderer> currentRows = Lists.newArrayList();
		for( int i = 0; i < editingConfig.getRows().size(); i++ )
		{
			final HtmlEditorToolbarConfig sourceRow = editingConfig.getRows().get(i);
			final ToolbarRowRenderer currentRow = new ToolbarRowRenderer("tbcurrent" + i);
			currentRow.addClass("currenttoolbar currenttoolbar" + i);
			final List<ToolbarButtonRenderer> currentButtons = currentRow.getButtons();
			for( String button : sourceRow.getButtons() )
			{
				final HtmlEditorButtonDefinition buttonDefinition = buttonMap.get(button);
				// handle deleted/disabled plugin buttons
				if( buttonDefinition != null )
				{
					currentButtons.add(new ToolbarButtonRenderer(buttonDefinition));
					usedButtons.add(button);
				}
			}
			currentRows.add(currentRow);
		}
		model.setCurrentRows(currentRows);

		for( String c : usedButtons )
		{
			final HtmlEditorButtonDefinition button = buttonMap.get(c);
			if( button.isSingleton() )
			{
				buttonMap.remove(c);
			}
		}
		final HtmlComponentState availableState = new HtmlComponentState();
		availableState.setId("tbavailable");
		availableState.addReadyStatements(Js.call_s(SETUP_TOOLBAR, Jq.$(availableState), Jq.$(".currenttoolbar"),
			Jq.$(containerDiv), dirty, new ObjectExpression("movedCallback", toolbarMovedFunction)));

		final ToolbarRowRenderer availableRow = new ToolbarRowRenderer(availableState);
		availableRow.addClass("availabletoolbar");
		final List<ToolbarButtonRenderer> availableButtons = availableRow.getButtons();
		availableButtons.addAll(tformButtons(buttonMap.values()));
		model.setAvailableRow(availableRow);

		return viewFactory.createResult("setting/toolbar.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		toolbarMovedFunction = ajax.getAjaxFunction("buttonMoved");
		resetButton.setClickHandler(events.getNamedHandler("resetToDefault").addValidator(
			Js.confirm(LABEL_CONFIRM_RESET)));
		clearButton.setClickHandler(events.getNamedHandler("clear").addValidator(Js.confirm(LABEL_CONFIRM_CLEAR)));

		saveButton.setClickHandler(events.getNamedHandler("save"));
		saveButton.addEventStatements(JSHandler.EVENT_BEFOREUNLOAD, new ReturnStatement(LABEL_CONFIRM_NAVIGATE_AWAY));
		cancelButton.setClickHandler(events.getNamedHandler("cancel"));
	}

	@EventHandlerMethod
	public void resetToDefault(SectionInfo info)
	{
		final HtmlEditorConfigurationEditingSession session = htmlEditorService.getEditorConfigEditingSession(getModel(
			info).getSessionId());
		session.setDirty(true);
		final List<HtmlEditorToolbarConfig> rows = session.getConfig().getRows();

		final List<List<String>> defaultButtonConfiguration = htmlEditorService.getEditor()
			.getDefaultButtonConfiguration();

		int i = 0;
		for( HtmlEditorToolbarConfig row : rows )
		{
			row.getButtons().clear();
			if( defaultButtonConfiguration.size() > i )
			{
				row.getButtons().addAll(defaultButtonConfiguration.get(i));
			}
			i++;
		}
	}

	@EventHandlerMethod
	public void clear(SectionInfo info)
	{
		final HtmlEditorConfigurationEditingSession session = htmlEditorService.getEditorConfigEditingSession(getModel(
			info).getSessionId());
		session.setDirty(true);
		final List<HtmlEditorToolbarConfig> rows = session.getConfig().getRows();
		for( HtmlEditorToolbarConfig row : rows )
		{
			row.getButtons().clear();
		}
	}

	@AjaxMethod
	public boolean buttonMoved(SectionInfo info, int toRow, int toIndex, int fromRow, int fromIndex, String buttonId)
	{
		final String sessionId = getModel(info).getSessionId();

		// remove it
		if( toRow == -1 )
		{
			// never remove from available
			if( fromRow >= 0 && fromRow <= 2 )
			{
				remove(sessionId, fromRow, fromIndex, buttonId);
				return true;
			}
		}
		// add or move it
		else if( toRow >= 0 && toRow <= 2 )
		{
			// never remove from available
			if( fromRow >= 0 && fromRow <= 2 )
			{
				remove(sessionId, fromRow, fromIndex, buttonId);
			}

			final HtmlEditorConfigurationEditingSession session = htmlEditorService
				.getEditorConfigEditingSession(sessionId);
			session.setDirty(true);
			final List<String> toList = session.getConfig().getRows().get(toRow).getButtons();
			// assertion
			if( toList.size() >= toIndex && toIndex >= 0 )
			{
				toList.add(toIndex, buttonId);
				return true;
			}
		}

		return false;
	}

	private void remove(String sessionId, int fromRow, int fromIndex, String buttonId)
	{
		final HtmlEditorConfigurationEditingSession session = htmlEditorService
			.getEditorConfigEditingSession(sessionId);
		session.setDirty(true);
		final List<String> fromList = session.getConfig().getRows().get(fromRow).getButtons();
		// assertion
		if( fromList.size() > fromIndex && fromIndex >= 0 && fromList.get(fromIndex).equals(buttonId) )
		{
			fromList.remove(fromIndex);
		}
	}

	private List<ToolbarButtonRenderer> tformButtons(Collection<HtmlEditorButtonDefinition> buttons)
	{
		return Lists.newArrayList(Collections2.transform(buttons,
			new Function<HtmlEditorButtonDefinition, ToolbarButtonRenderer>()
			{
				@Override
				public ToolbarButtonRenderer apply(HtmlEditorButtonDefinition button)
				{
					return new ToolbarButtonRenderer(button);
				}
			}));
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		final HtmlEditorToolbarModel model = getModel(info);
		htmlEditorService.cancelEditorConfigEditingSession(model.getSessionId());
		model.setSessionId(null);
		front.returnToFrontPage(info);
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final HtmlEditorToolbarModel model = getModel(info);
		htmlEditorService.commitEditorConfigEditingSession(model.getSessionId());
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
	public void startSession(SectionInfo info)
	{
		getModel(info).setSessionId(htmlEditorService.createEditorConfigEditingSession().getSessionId());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new HtmlEditorToolbarModel();
	}

	@Override
	@Nullable
	public SettingInfo getSettingInfo(SectionInfo info)
	{
		return new SettingInfo("toolbar", LABEL_SETTING_LINK, LABEL_SETTING_BLURB);
	}

	public Div getContainerDiv()
	{
		return containerDiv;
	}

	public Button getResetButton()
	{
		return resetButton;
	}

	public Button getClearButton()
	{
		return clearButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public class ToolbarRowRenderer extends DivRenderer
	{
		private final List<ToolbarButtonRenderer> buttons = Lists.newArrayList();
		private final DivRenderer dodgyProp;

		public ToolbarRowRenderer(HtmlComponentState state)
		{
			super(state);
			dodgyProp = new DivRenderer("dynamicheighthack", null);
		}

		public ToolbarRowRenderer(String id)
		{
			super(new HtmlComponentState());
			setId(id);
			dodgyProp = new DivRenderer("dynamicheighthack", null);
		}

		public List<ToolbarButtonRenderer> getButtons()
		{
			return buttons;
		}

		@Override
		public SectionRenderable getNestedRenderable()
		{
			return CombinedRenderer.combineResults(CombinedRenderer.combineMultipleResults(buttons), dodgyProp);
		}
	}

	public class ToolbarButtonRenderer extends DivRenderer
	{
		private final HtmlEditorButtonDefinition button;

		public ToolbarButtonRenderer(HtmlEditorButtonDefinition button)
		{
			super("div", "toolbarbutton" + (button.isSingleton() ? "" : " cloneable"), button);
			final TagState ts = getTagState();
			ts.setId(button.getId());
			this.button = button;
		}

		@Override
		protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
		{
			super.prepareFirstAttributes(writer, attrs);
			attrs.put("title", button.getLabel().getText());
		}
	}

	public static class HtmlEditorToolbarModel
	{
		@Bookmarked(name = "s")
		private String sessionId;
		private boolean dirty;
		private List<ToolbarRowRenderer> currentRows;
		private ToolbarRowRenderer availableRow;

		public String getSessionId()
		{
			return sessionId;
		}

		public void setSessionId(String sessionId)
		{
			this.sessionId = sessionId;
		}

		public boolean isDirty()
		{
			return dirty;
		}

		public void setDirty(boolean dirty)
		{
			this.dirty = dirty;
		}

		public List<ToolbarRowRenderer> getCurrentRows()
		{
			return currentRows;
		}

		public void setCurrentRows(List<ToolbarRowRenderer> currentRows)
		{
			this.currentRows = currentRows;
		}

		public ToolbarRowRenderer getAvailableRow()
		{
			return availableRow;
		}

		public void setAvailableRow(ToolbarRowRenderer availableRow)
		{
			this.availableRow = availableRow;
		}
	}
}
