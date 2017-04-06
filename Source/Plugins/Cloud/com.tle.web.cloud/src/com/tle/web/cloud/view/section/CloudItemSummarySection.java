package com.tle.web.cloud.view.section;

import java.util.Collection;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.CloudWebConstants;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.MenuMode;
import com.tle.web.viewitem.summary.section.AbstractItemSummarySection;
import com.tle.web.viewurl.ViewItemResource;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CloudItemSummarySection extends AbstractItemSummarySection<CloudItem>
{
	@PlugKey("viewitem.title")
	private static Label LABEL_TITLE;
	@PlugKey("viewitem.breadcrumb.title")
	private static Label LABEL_BREADCRUMB_TITLE;
	@PlugKey("search.breadcrumb.title")
	private static Label LABEL_BREADCRUMB_SEARCH_TITLE;

	@PlugURL("css/item/summary.css")
	private static String CSS_URL;

	@TreeLookup
	private RootCloudViewItemSection rootSection;

	@Nullable
	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return null;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final CloudViewableItem vitem = (CloudViewableItem) resource.getViewableItem();
		if( vitem.isIntegration() )
		{
			final Decorations decorations = Decorations.getDecorations(info);
			decorations.setBanner(false);
			decorations.setMenuMode(MenuMode.HIDDEN);
		}

		return super.view(info, resource);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		super.addBreadcrumbsAndTitle(info, decorations, crumbs);

		final HtmlLinkState cloudBreadcrumb = new HtmlLinkState(LABEL_BREADCRUMB_SEARCH_TITLE, new InfoBookmark(
			info.createForward(CloudWebConstants.URL_CLOUD_SEARCH)));
		if( CloudItemSectionInfo.getItemInfo(info).getViewableItem().isIntegration() )
		{
			cloudBreadcrumb.setDisabled(true);
		}
		crumbs.add(cloudBreadcrumb);

		final CloudItem item = getItem(info);
		crumbs.setForcedLastCrumb(new WrappedLabel(new BundleLabel(item.getName(), item.getUuid(), bundleCache), 60,
			true));
	}

	@Override
	protected String getContentBodyClass(SectionInfo info)
	{
		return "cloud-layout itemsummary-layout";
	}

	@Override
	protected CloudItem getItem(SectionInfo info)
	{
		return CloudItemSectionInfo.getItemInfo(info).getViewableItem().getItem();
	}

	@Override
	protected Label getPageTitle(SectionInfo info)
	{
		return LABEL_TITLE;
	}

	@Override
	protected List<String> getCssUrls(SectionInfo info)
	{
		final List<String> cssUrls = super.getCssUrls(info);
		cssUrls.add(CSS_URL);
		return cssUrls;
	}

	@Override
	protected boolean isPreview(SectionInfo info)
	{
		return false;
	}
}
