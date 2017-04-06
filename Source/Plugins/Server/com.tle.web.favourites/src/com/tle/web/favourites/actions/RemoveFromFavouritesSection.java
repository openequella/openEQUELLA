package com.tle.web.favourites.actions;

import javax.inject.Inject;

import com.tle.beans.item.Bookmark;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
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
import com.tle.web.sections.equella.render.HideableFromDRMModel;
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
	public void hideSection(SectionInfo info)
	{
		getModel(info).setHide(true);
	}

	@Override
	public void unhideSection(SectionInfo info)
	{
		getModel(info).setHide(false);
	}

	@Override
	public Class<RemoveFromFavouritesModel> getModelClass()
	{
		return RemoveFromFavouritesModel.class;
	}

	public static class RemoveFromFavouritesModel implements HideableFromDRMModel
	{
		private boolean hide;

		@Override
		public boolean isHide()
		{
			return hide;
		}

		@Override
		public void setHide(boolean hide)
		{
			this.hide = hide;
		}
	}
}
