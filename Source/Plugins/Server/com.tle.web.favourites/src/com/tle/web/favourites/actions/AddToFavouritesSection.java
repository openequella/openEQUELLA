package com.tle.web.favourites.actions;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.favourites.service.BookmarkService;
import com.tle.core.guice.Bind;
import com.tle.core.user.CurrentUser;
import com.tle.web.favourites.FavouritesDialog;
import com.tle.web.favourites.actions.AddToFavouritesSection.AddToFavouritesModel;
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
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

@Bind
public class AddToFavouritesSection extends AbstractParentViewItemSection<AddToFavouritesModel>
	implements
		HideableFromDRMSection
{
	@PlugKey("viewitem.receipt.add")
	private static Label LABEL_RECEIPT;
	@PlugURL("css/favourites.css")
	private static String CSS;

	@EventFactory
	protected EventGenerator events;

	@Inject
	private BookmarkService bookmarkService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private ModerationService moderationService;

	@Component
	@PlugKey("viewitem.button.add")
	private Button ourOpener;

	@Inject
	@Component(name = "fid")
	private FavouritesDialog myFavourites;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		ourOpener.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		ourOpener.setStyleClass("addToFavourites");
		ourOpener.addPrerenderables(CssInclude.include(CSS).hasRtl().make());

		myFavourites.setOkCallback(events.getSubmitValuesFunction("addBookmarkClicked"));
	}

	@EventHandlerMethod
	public void addBookmarkClicked(SectionInfo info, String tagString, boolean alwaysLatest)
	{
		Item item = getItemInfo(info).getItem();
		bookmarkService.add(item, tagString, alwaysLatest);
		receiptService.setReceipt(LABEL_RECEIPT);
	}

	@Override
	public final boolean canView(SectionInfo info)
	{
		boolean hide = getModel(info).isHide();

		if( hide )
		{
			return false;
		}
		else
		{
			final ItemSectionInfo itemInfo = getItemInfo(info);

			return !CurrentUser.isGuest() && !CurrentUser.wasAutoLoggedIn()
				&& itemInfo.getViewableItem().isItemForReal() && !moderationService.isModerating(info)
				&& bookmarkService.getByItem(itemInfo.getItemId()) == null;
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}

		ItemKey itemId = getItemInfo(context).getItemId();
		ourOpener.setClickHandler(context, new OverrideHandler(myFavourites.getOpenFunction(), itemId));
		return SectionUtils.renderSectionResult(context, ourOpener);
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
	public Class<AddToFavouritesModel> getModelClass()
	{
		return AddToFavouritesModel.class;
	}

	public static class AddToFavouritesModel implements HideableFromDRMModel
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
