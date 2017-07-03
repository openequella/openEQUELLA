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

package com.tle.web.viewitem.summary.content;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.core.i18n.BundleCache;
import com.tle.web.navigation.BreadcrumbService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.section.ItemDetailsAndActionsSummarySection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
public abstract class AbstractContentSection<M> extends AbstractPrototypeSection<M> implements HtmlRenderer
{
	@EventFactory
	protected EventGenerator events;

	@TreeLookup(mandatory = false)
	protected ItemSummaryContentSection itemSummaryContentSection;

	@Inject
	protected BundleCache bundleCache;
	@Inject
	protected BreadcrumbService breadcrumbService;
	@Inject
	private SelectionService selectionService;

	protected JSHandler mainContentBreadcrumbClickedHandler;

	@TreeLookup(mandatory = false)
	private ItemDetailsAndActionsSummarySection itemDetailsAndActionsSummarySection;

	@PlugKey("summary.content.back")
	@Component
	private Button backButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		mainContentBreadcrumbClickedHandler = events.getNamedHandler("mainContentBreadcrumbClicked");
		backButton.setClickHandler(events.getNamedHandler("backButtonCliecked"));
	}

	protected void addDefaultBreadcrumbs(SectionInfo info, ItemSectionInfo itemInfo, Label overrideLastCrumb)
	{
		TagState col = breadcrumbService.getSearchCollectionCrumb(info, itemInfo.getItemdef().getUuid());

		if( col instanceof HtmlLinkState
			&& (ParentViewItemSectionUtils.isForPreview(info) || ParentViewItemSectionUtils.isInIntegration(info)) )
		{
			((HtmlLinkState) col).setDisabled(true);
		}

		Breadcrumbs crumbs = Breadcrumbs.get(info);

		crumbs.add(col);

		Item item = itemInfo.getItem();
		WrappedLabel truncLabel = new WrappedLabel(new BundleLabel(item.getName(), item.getUuid(), bundleCache), 35);

		// Page title is always going to be "Resource", so the last crumb should
		// either be the item name, or the item name and the sub-page title.
		if( overrideLastCrumb == null )
		{
			crumbs.setForcedLastCrumb(truncLabel.setShowAltText(true));
		}
		else
		{
			crumbs.add(new HtmlLinkState(truncLabel, mainContentBreadcrumbClickedHandler));
			crumbs.setForcedLastCrumb(overrideLastCrumb);
		}
	}

	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}

	public boolean isCourseSelectionSession(SectionInfo info)
	{
		SelectionSession ss = selectionService.getCurrentSession(info);
		if( (ss != null && ss.getLayout() == Layout.COURSE) )
		{
			return true;
		}
		return false;
	}

	@EventHandlerMethod
	public void mainContentBreadcrumbClicked(SectionInfo info)
	{
		itemSummaryContentSection.setSummaryId(info, null);
	}

	@EventHandlerMethod
	public void backButtonCliecked(SectionInfo info)
	{
		itemSummaryContentSection.setSummaryId(info, itemDetailsAndActionsSummarySection);
	}

	protected void displayBackButton(SectionInfo info)
	{
		SelectionSession ss = selectionService.getCurrentSession(info);
		backButton.setDisplayed(info, (ss != null && ss.getLayout() == Layout.COURSE));
	}

	public Button getBackButton()
	{
		return backButton;
	}
}