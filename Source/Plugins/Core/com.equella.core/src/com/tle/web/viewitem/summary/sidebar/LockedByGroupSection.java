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

package com.tle.web.viewitem.summary.sidebar;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.item.service.ItemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public class LockedByGroupSection extends AbstractParentViewItemSection<LockedByGroupSection.LockedByGroupModel>
{
	@PlugKey("summary.sidebar.lockedbygroup.unlock.confirm")
	private static Label UNLOCK_CONFIRM_LABEL;

	@Inject
	private ItemService itemService;

	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	@EventFactory
	private EventGenerator events;

	@Component
	@PlugKey("summary.sidebar.lockedbygroup.unlock.button")
	private Button unlock;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		userLinkSection = userLinkService.register(tree, id);

		JSHandler unlockHandler = events.getNamedHandler("unlockItem");
		unlockHandler.addValidator(new Confirm(UNLOCK_CONFIRM_LABEL));

		unlock.setClickHandler(unlockHandler);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return getItemInfo(info).getWorkflowStatus().isLocked();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final ItemSectionInfo itemInfo = getItemInfo(context);
		final WorkflowStatus status = itemInfo.getWorkflowStatus();

		if( itemInfo.isEditing() || !status.isLocked() )
		{
			return null;
		}

		final LockedByGroupModel model = getModel(context);

		model.setAllowUnlock(!status.getSecurityStatus().getLock().getUserSession().equals(CurrentUser.getSessionID())
			&& (itemInfo.hasPrivilege("EDIT_ITEM") || itemInfo.hasPrivilege("REDRAFT_ITEM")));

		model.setLockedByUser(userLinkSection.createLink(context, status.getSecurityStatus().getLockedBy()));

		model.setNotPreview(itemInfo.getViewableItem().isItemForReal());

		List<SectionRenderable> sections = renderChildren(context, new ResultListCollector()).getResultList();
		model.setSections(sections);

		return viewFactory.createResult("viewitem/summary/sidebar/lockedbygroup.ftl", context);
	}

	@EventHandlerMethod
	public void unlockItem(SectionInfo info)
	{
		ItemSectionInfo itemInfo = getItemInfo(info);
		itemService.forceUnlock(itemInfo.getItem());
		itemInfo.refreshItem(true);
	}

	public Button getUnlock()
	{
		return unlock;
	}

	@Override
	public Class<LockedByGroupModel> getModelClass()
	{
		return LockedByGroupModel.class;
	}

	public static class LockedByGroupModel
	{
		private boolean allowUnlock;
		private boolean notPreview;
		private HtmlLinkState lockedByUser;
		private List<SectionRenderable> sections;

		public boolean isAllowUnlock()
		{
			return allowUnlock;
		}

		public void setAllowUnlock(boolean allowUnlock)
		{
			this.allowUnlock = allowUnlock;
		}

		public boolean isNotPreview()
		{
			return notPreview;
		}

		public void setNotPreview(boolean notPreview)
		{
			this.notPreview = notPreview;
		}

		public HtmlLinkState getLockedByUser()
		{
			return lockedByUser;
		}

		public void setLockedByUser(HtmlLinkState lockedByUser)
		{
			this.lockedByUser = lockedByUser;
		}

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}
	}
}
