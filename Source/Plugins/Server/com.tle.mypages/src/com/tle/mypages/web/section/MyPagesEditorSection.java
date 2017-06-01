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

package com.tle.mypages.web.section;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.ScriptContext;
import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.scripting.ScriptContextFactory;
import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.event.SavePageEvent;
import com.tle.mypages.web.event.SavePageEventListener;
import com.tle.mypages.web.model.MyPagesContributeModel;
import com.tle.mypages.web.model.MyPagesEditorModel;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SingleResultCollector;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.WizardStateInterface;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class MyPagesEditorSection extends AbstractMyPagesSection<MyPagesEditorModel>
	implements
		HtmlRenderer,
		SavePageEventListener
{
	@TreeLookup
	private MyPagesContributeSection contribSection;
	private HtmlEditorInterface htmlEditor;

	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private MyPagesService myPagesService;

	@Component
	private TextField pageNameField;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		MyPagesContributeModel model = contribSection.getModel(context);
		String pageUuid = model.getPageUuid();

		MyPagesEditorModel edModel = getModel(context);
		if( !Check.isEmpty(pageUuid) )
		{
			HtmlAttachment page = myPagesService.getPageAttachment(context, model.getSession(), model.getItemId(),
				model.getPageUuid());
			String description;
			if( page != null )
			{
				description = page.getDescription();
			}
			else
			{
				description = CurrentLocale.get(RESOURCES.key("pagetitle.untitled"));
			}
			pageNameField.setValue(context, description);

			Map<String, String> properties = new HashMap<String, String>();

			if( model.isModal() )
			{
				// hack for being in a thickbox...
				properties.put("thickbox", "true");
			}
			String sessionId = model.getSession();
			WizardStateInterface state = myPagesService.getState(context, sessionId);
			properties.put("sessionId", sessionId);
			properties.put("pageId", model.getPageUuid());
			properties.put("html",
				page == null ? "" : myPagesService.getDraftHtml(state, context, page, state.getItemId()));
			properties.put("rows", "10");
			properties.put("width", "100%");
			properties.put("height", "300px");
			htmlEditor.setData(context, properties, new MyPagesScriptContextFactory(state));

			edModel.setEditorRenderable(renderSection(context, htmlEditor));
			edModel.setShowEditor(true);

			List<SectionId> childIds = Lists.newArrayList(context.getChildIds(this));
			childIds.remove(htmlEditor);
			edModel.setExtraRenderable((SectionRenderable) SectionUtils.renderSectionIds(context, childIds,
				new SingleResultCollector()).getResult());
		}

		return viewFactory.createResult("mypageseditor.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		htmlEditor = htmlEditorService.getEditor();
		tree.registerSections(htmlEditor, id);
	}

	@Override
	public void doSavePageEvent(SectionInfo info, SavePageEvent event)
	{
		String pageTitle = pageNameField.getValue(info);
		if( Check.isEmpty(pageTitle) )
		{
			pageTitle = CurrentLocale.get(RESOURCES.key("description.untitled"));
		}
		event.getPage().setDescription(pageTitle);
		String sessionId = event.getSessionId();
		String stagingHtml = htmlEditor.getHtml(info);
		myPagesService.setHtml(info, sessionId, event.getPage(), stagingHtml);
	}

	@Override
	public Class<MyPagesEditorModel> getModelClass()
	{
		return MyPagesEditorModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return MyPagesConstants.SECTION_EDITOR;
	}

	public TextField getPageNameField()
	{
		return pageNameField;
	}

	public class MyPagesScriptContextFactory implements ScriptContextFactory
	{
		private final WizardStateInterface state;

		protected MyPagesScriptContextFactory(WizardStateInterface state)
		{
			this.state = state;
		}

		@Override
		public ScriptContext createScriptContext()
		{
			return myPagesService.createScriptContext(state);
		}
	}
}
