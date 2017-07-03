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

package com.tle.web.viewitem.externallinkviewer;

import java.util.Collection;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Div;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.FullScreen;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author aholland
 */
@NonNullByDefault
@SuppressWarnings("nls")
@Bind
public class ExternalLinkViewerSection extends AbstractViewerSection<ExternalLinkViewerSection.ExternalLinkViewerModel>
{
	@Inject
	private ViewItemUrlFactory itemUrls;
	@Inject
	private BundleCache bundleCache;

	@Inject
	@Component
	private NavBar navBar;

	@Component
	@PlugKey("hidebar")
	private Link hideButton;
	@Component
	private Link itemName;
	@Component
	private Div resourceUrl;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		navBar.setTitle(itemName);
		resourceUrl.setStyleClass("navbar-text");
		navBar.buildLeft().content(resourceUrl);
		navBar.buildRight().divider().action(hideButton);
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return resource.getViewAuditEntry();
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final ExternalLinkViewerModel model = getModel(info);
		final Item item = (Item) resource.getViewableItem().getItem();

		final String contentUrl = resource.createCanonicalURL().getHref();
		model.setContentUrl(contentUrl);
		resourceUrl.setLabel(info, new TextLabel(contentUrl));

		ViewableResource viewRes = resource.getAttribute(ViewableResource.class);
		String title = viewRes.getDescription();
		if( Check.isEmpty(title) )
		{
			title = contentUrl;
		}
		else if( !title.equals(contentUrl) )
		{
			title += " : " + contentUrl;
		}
		Label urlLabel = new TextLabel(title);

		hideButton.addClickStatements(info,
			new ScriptStatement("document.location = " + JSUtils.escape(contentUrl, true)));

		itemName.setLabel(info, new IconLabel(Icon.BACK, new BundleLabel(item.getName(), item.getUuid(), bundleCache)));
		itemName.setBookmark(info, itemUrls.createItemUrl(info, resource.getViewableItem().getItem().getItemId()));

		Decorations decorations = Decorations.getDecorations(info);
		decorations.setFullscreen(FullScreen.YES_WITH_TOOLBAR);
		decorations.setTitle(urlLabel);
		decorations.clearAllDecorations();
		return viewFactory.createResult("frames.ftl", this);
	}

	@Override
	public Class<ExternalLinkViewerModel> getModelClass()
	{
		return ExternalLinkViewerModel.class;
	}

	public NavBar getNavBar()
	{
		return navBar;
	}

	public static class ExternalLinkViewerModel
	{
		private String contentUrl;

		public String getContentUrl()
		{
			return contentUrl;
		}

		public void setContentUrl(String contentUrl)
		{
			this.contentUrl = contentUrl;
		}
	}
}
