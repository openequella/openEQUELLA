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

package com.tle.web.favourites.actions;

import javax.inject.Inject;

import com.tle.beans.item.Bookmark;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.favourites.actions.RemoveFromFavouritesSection.RemoveFromFavouritesModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;

@Bind
@SuppressWarnings("nls")
public class RemoveFromFavouritesSection extends AbstractParentViewItemSection<RemoveFromFavouritesModel>
	implements
		HideableFromDRMSection
{
	@PlugKey("viewitem.receipt.remove")
	private static Label RECEIPT_LABEL;
	@PlugURL("css/favourites.css")
	private static String CSS;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private BookmarkService bookmarkService;
	@Inject
	private ReceiptService receiptService;

	@Component
	@PlugKey("viewitem.button.remove")
	private Button button;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		button.setClickHandler(events.getNamedHandler("remove"));
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setStyleClass("removeFromFavourites");
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
			return !CurrentUser.isGuest() && !CurrentUser.wasAutoLoggedIn()
				&& bookmarkService.getByItem(getItemInfo(info).getItemId()) != null;
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, button);
	}

	@EventHandlerMethod
	public void remove(SectionInfo info)
	{
		Bookmark fav = bookmarkService.getByItem(getItemInfo(info).getItemId());
		bookmarkService.delete(fav.getId());
		receiptService.setReceipt(RECEIPT_LABEL);
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getModel(info).setHide(!show);
	}

	@Override
	public Class<RemoveFromFavouritesModel> getModelClass()
	{
		return RemoveFromFavouritesModel.class;
	}

	public static class RemoveFromFavouritesModel
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
