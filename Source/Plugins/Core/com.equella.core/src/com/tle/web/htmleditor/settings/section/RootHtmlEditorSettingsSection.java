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

import com.tle.core.guice.Bind;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.htmleditor.settings.HtmlEditorSettingsPrivilegeTreeProvider;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.layout.OneColumnLayout.OneColumnLayoutModel;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
@Bind
public class RootHtmlEditorSettingsSection extends OneColumnLayout<OneColumnLayoutModel>
{
	@PlugKey("htmledit.settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.plugins.error.noaccess")
	private static Label LABEL_NOACCESS;

	@Inject
	private HtmlEditorSettingsPrivilegeTreeProvider securityProvider;

	private boolean canView()
	{
		return securityProvider.isAuthorised();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView() )
		{
			throw new AccessDeniedException(LABEL_NOACCESS.getText());
		}

		return super.renderHtml(context);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		OneColumnLayoutModel model = getModel(info);
		SectionId modalSection = model.getModalSection();
		crumbs.add(SettingsUtils.getBreadcrumb());

		if( modalSection != null )
		{
			crumbs.add(HtmlEditorSettingsSection.getShowHtmlEditorPluginsLink(info));

			SectionId section = info.getSectionForId(modalSection);
			if( section instanceof ModalHtmlEditorSettingsSection )
			{
				((ModalHtmlEditorSettingsSection) section).addBreadcrumbsAndTitle(info, decorations, crumbs);
				return;
			}
		}
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("htmleditor");
	}

	@Override
	public Class<OneColumnLayoutModel> getModelClass()
	{
		return OneColumnLayoutModel.class;
	}
}
