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

package com.tle.web.viewitem.sharing;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.sharing.ShareWithOthersLinkSection.ShareWithOthersLinkModel;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

@Bind
public class ShareWithOthersLinkSection extends AbstractParentViewItemSection<ShareWithOthersLinkModel>
	implements
		HideableFromDRMSection
{
	@PlugURL("css/share.css")
	private static String CSS;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private ModerationService moderationService;

	@TreeLookup
	private ItemSummaryContentSection contentSection;
	@TreeLookup
	private ShareWithOthersContentSection shareWithOthersContentSection;

	@Component
	@PlugKey("summary.sidebar.actions.share.title")
	private Button button;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		button.setClickHandler(events.getNamedHandler("execute"));
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setStyleClass("shareButton");
		button.addPrerenderables(CssInclude.include(CSS).hasRtl().make());
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		boolean hide = getModel(info).isHide();

		if( hide )
		{
			return false;
		}
		else
		{
			ItemSectionInfo itemInfo = getItemInfo(info);
			return itemInfo.getViewableItem().isItemForReal() && !moderationService.isModerating(info)
				&& shareWithOthersContentSection.canView(info);
		}
	}

	@EventHandlerMethod
	public void execute(SectionInfo info)
	{
		contentSection.setSummaryId(info, shareWithOthersContentSection);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, button);
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getModel(info).setHide(!show);
	}

	@Override
	public Class<ShareWithOthersLinkModel> getModelClass()
	{
		return ShareWithOthersLinkModel.class;
	}

	public static class ShareWithOthersLinkModel
	{
		private boolean hide;

		public boolean isHide()
		{
			return hide;
		}

		public void setHide(boolean hide)
		{
			this.hide = hide;
		}
	}
}
