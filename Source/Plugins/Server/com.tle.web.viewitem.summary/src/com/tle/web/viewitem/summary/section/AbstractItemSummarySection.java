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

package com.tle.web.viewitem.summary.section;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.core.i18n.BundleCache;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.equella.layout.TwoColumnLayout.TwoColumnModel;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.MenuSection;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.viewitem.summary.ItemSummaryContent;
import com.tle.web.viewitem.summary.ItemSummarySidebar;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractItemSummarySection<I extends IItem<?>> extends TwoColumnLayout<TwoColumnModel>
	implements
		ViewItemViewer
{
	@PlugURL("css/itemsummary.css")
	private static String LAYOUT_CSS;

	@TreeLookup
	private ItemSummaryContent summarySection;
	@TreeLookup
	private ItemSummarySidebar sidebarSection;
	@Inject
	protected SelectionService selectionService;
	@Inject
	protected BundleCache bundleCache;

	protected abstract String getContentBodyClass(SectionInfo info);

	protected abstract I getItem(SectionInfo info);

	protected abstract Label getPageTitle(SectionInfo info);

	protected abstract boolean isPreview(SectionInfo info);

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		for( String css : getCssUrls(info) )
		{
			info.getPreRenderContext().addCss(CssInclude.include(css).hasRtl().make());
		}

		if( isPreview(info)
			|| (selectionService.getCurrentSession(info) == null && Decorations.getDecorations(info).isMenuHidden()) )
		{
			info.getPreRenderContext().addCss(MenuSection.getHiddenMenuCSS());
		}

		return renderHtml((RenderEventContext) info);
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	protected List<String> getCssUrls(SectionInfo info)
	{
		return Lists.newArrayList(LAYOUT_CSS);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		if( Check.isEmpty(decorations.getContentBodyClasses()) )
		{
			decorations.setContentBodyClass(getContentBodyClass(info));
		}
		if( decorations.getTitle() == null )
		{
			I item = getItem(info);
			decorations.setTitle(new BundleLabel(item.getName(), item.getUuid(), bundleCache));
		}
		decorations.setBannerTitle(getPageTitle(info));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		GenericTemplateResult template = new GenericTemplateResult();
		template.addNamedResult(LEFT, renderSection(info, summarySection));

		// This is really dirty (but not new)
		SelectionSession ss = selectionService.getCurrentSession(info);
		if( ss == null || ss.getLayout() != Layout.SKINNY && ss.getLayout() != Layout.COURSE )
		{
			template.addNamedResult(RIGHT, renderSection(info, sidebarSection));
		}

		info.processEvent(new BlueBarEvent(info));
		return template;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return new ViewAuditEntry(true);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "summ";
	}

	@Override
	public Class<TwoColumnModel> getModelClass()
	{
		return TwoColumnModel.class;
	}
}
