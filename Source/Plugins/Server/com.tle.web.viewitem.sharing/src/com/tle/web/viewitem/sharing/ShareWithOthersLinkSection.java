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
import com.tle.web.sections.equella.render.HideableFromDRMModel;
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
	public Class<ShareWithOthersLinkModel> getModelClass()
	{
		return ShareWithOthersLinkModel.class;
	}

	public static class ShareWithOthersLinkModel implements HideableFromDRMModel
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
