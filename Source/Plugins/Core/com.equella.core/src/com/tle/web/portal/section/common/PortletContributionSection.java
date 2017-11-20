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

package com.tle.web.portal.section.common;

import java.util.Map;

import javax.inject.Inject;

import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.portal.editor.PortletEditor;
import com.tle.web.portal.section.enduser.ModalPortletSection;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author aholland
 */
public class PortletContributionSection
	extends
		AbstractPrototypeSection<PortletContributionSection.PortletContributeModel>
	implements
		HtmlRenderer,
		ModalPortletSection
{
	private Map<String, PortletEditor> editorMap;

	@Inject
	private PortletWebService portletWebService;

	@EventFactory
	protected EventGenerator events;

	@TreeLookup
	private OneColumnLayout<?> rootSection;

	@Override
	public void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		ContentLayout.setLayout(info, ContentLayout.ONE_COLUMN);
		getModel(info).getEditor().addBreadcrumbsAndTitle(info, decorations, crumbs);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final PortletContributeModel model = getModel(context);
		PortletEditor ed = model.getEditor();
		GenericTemplateResult templateResult = new GenericTemplateResult();
		// System account user presumably doesn't need help, and screen-options
		// refers to dashboard
		// customisation which system user doesn't see, instead seeing
		// sysnoportlets.ftl
		if( !CurrentUser.getUserState().isSystem() )
		{
			HelpAndScreenOptionsSection.addHelp(context, ed.renderHelp(context));
		}
		templateResult.addNamedResult("body", ed.render(context)); //$NON-NLS-1$
		return templateResult;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_MODAL_LOGIC)
	public void checkModal(SectionInfo info)
	{
		final PortletContributeModel model = getModel(info);
		final String type = model.getType();
		boolean fromSettings = model.isFromSettings();
		if( type != null && !fromSettings )
		{
			rootSection.setModalSection(info, this);
		}
		model.setFromSettings(false);
	}

	public void setFromSettings(SectionInfo info, boolean fromSettings)
	{
		getModel(info).setFromSettings(fromSettings);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		editorMap = portletWebService.registerEditors(tree, id);
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void includeHandler(SectionInfo info)
	{
		final PortletContributeModel model = getModel(info);
		final String type = model.getType();
		if( type != null )
		{
			PortletEditor ed = model.getEditor();

			if( model.isRendered() )
			{
				ed.saveToSession(info);
			}
			model.setEditor(ed);
		}
	}

	@DirectEvent
	public void loadFromSession(SectionInfo info)
	{
		final PortletContributeModel model = getModel(info);
		model.setRendered(true);
		PortletEditor editor = model.getEditor();
		if( editor != null )
		{
			editor.loadFromSession(info);
		}
	}

	/**
	 * Invoked by PortletWebService ONLY. Use PortletWebService.newPortlet
	 * instead
	 * 
	 * @param info
	 * @param type
	 */
	public void createNew(SectionInfo info, String type, boolean admin)
	{
		final PortletContributeModel model = getModel(info);
		PortletEditor ed = editorMap.get(type);
		model.setType(type);
		model.setEditor(ed);
		ed.create(info, type, admin);
	}

	/**
	 * Invoked by PortletWebService ONLY. Use PortletWebService.editPortlet
	 * instead
	 * 
	 * @param info
	 * @param portletUuid
	 * @param type
	 */
	public void startEdit(SectionInfo info, String portletUuid, String type, boolean admin)
	{
		final PortletContributeModel model = getModel(info);
		PortletEditor ed = editorMap.get(type);
		model.setEditor(ed);
		ed.edit(info, portletUuid, admin);
		model.setType(type);
	}

	/**
	 * Invoked by PortletWebService ONLY. Use PortletWebService.returnFromEdit
	 * instead
	 * 
	 * @param info
	 */
	public void editingFinished(SectionInfo info)
	{
		final PortletContributeModel model = getModel(info);
		model.setType(null);
		model.setEditor(null);
	}

	@Override
	public Class<PortletContributeModel> getModelClass()
	{
		return PortletContributeModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pcn"; //$NON-NLS-1$
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new PortletContributeModel(info);
	}

	public class PortletContributeModel
	{
		private final SectionInfo info;
		@Bookmarked(name = "t")
		private String type;
		@Bookmarked(stateful = false)
		private boolean rendered;
		@Bookmarked(name = "s", contexts = BookmarkEvent.CONTEXT_BROWSERURL)
		private boolean fromSettings;
		private PortletEditor editor;

		public PortletContributeModel(SectionInfo info)
		{
			this.info = info;
		}

		public String getType()
		{
			return type;
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public boolean isRendered()
		{
			return rendered;
		}

		public void setRendered(boolean rendered)
		{
			this.rendered = rendered;
		}

		public boolean isFromSettings()
		{
			return fromSettings;
		}

		public void setFromSettings(boolean fromSettings)
		{
			this.fromSettings = fromSettings;
		}

		public PortletEditor getEditor()
		{
			if( editor == null && type != null )
			{
				editor = editorMap.get(type);
				editor.restore(info);
			}
			return editor;
		}

		public void setEditor(PortletEditor editor)
		{
			this.editor = editor;
		}
	}
}
