package com.tle.web.navigation;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewurl.ViewItemUrlFactory;

/**
 * I'm not sure com.tle.web.sections.equella should depend on
 * com.tle.web.viewurl... otherwise I would have put this class in that plugin.
 * 
 * @author aholland
 */
@Bind
@Singleton
public class ViewItemBreadcrumbProvider implements BreadcrumbProvider
{
	@PlugKey("breadcrumb.title")
	private static Label BREADCRUMB_TITLE;
	@PlugKey("breadcrumb.item.untitled")
	private static Label BREADCRUMB_UNTITLED;

	@Inject
	private ViewItemUrlFactory viewItemUrl;
	@Inject
	private BundleCache bundleCache;

	@Override
	@SuppressWarnings("nls")
	public TagState getBreadcrumb(SectionInfo info, Map<String, ?> params)
	{
		final Item item = (Item) params.get("item");

		HtmlLinkState itemLink = new HtmlLinkState(viewItemUrl.createItemUrl(info, item.getItemId()));
		itemLink
			.setLabel(new WrappedLabel(new BundleLabel(item.getName(), BREADCRUMB_UNTITLED, bundleCache), 35, true));
		itemLink.setRel("parent");
		itemLink.setTitle(BREADCRUMB_TITLE);
		return itemLink;
	}
}
