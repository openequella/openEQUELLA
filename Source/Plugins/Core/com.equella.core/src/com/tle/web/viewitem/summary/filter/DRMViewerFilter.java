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

package com.tle.web.viewitem.summary.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.DrmService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.sections.standard.renderers.fancybox.FancyBoxDialogRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.DRMFilter;
import com.tle.web.viewitem.summary.filter.DRMLicenseDialog.DRMDialogModel;
import com.tle.web.viewitem.summary.filter.DRMLicenseDialog.LicenseOnlyUrl;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ResourceViewerFilter;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class DRMViewerFilter implements ResourceViewerFilter
{
	@Inject
	private DrmService drmService;

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(DRMViewerFilter.class);

	@SuppressWarnings("nls")
	@Override
	public LinkTagRenderer filterLink(SectionInfo info, LinkTagRenderer viewerTag, ResourceViewer viewer,
		ViewableResource resource)
	{
		ViewableItem<Item> vitem = resource.getViewableItem();
		DRMFilter filterSection = info.lookupSection(DRMFilter.class);
		if( filterSection != null && drmService.havePreviewedThisSession(vitem.getItemId()) )
		{
			return viewerTag;
		}

		// Note that we call "requiresAcceptanceCheck()" before
		// "hasAcceptedOrRequiresNoAcceptance()"
		// to avoid getting the item unless required for checking DRM.

		if( vitem.isDRMApplicable() && drmService.requiresAcceptanceCheck(vitem.getItemId(), false, false)
			&& !drmService.hasAcceptedOrRequiresNoAcceptance(vitem.getItem(), false, false) )
		{
			ViewItemUrl previewUrl = viewer.createViewItemUrl(info, resource);
			previewUrl.setSkipDrm(true);
			LinkTagRenderer previewRenderer = viewer.createLinkRenderer(info, resource, previewUrl);
			viewerTag.addClass("drmlink_viewer");
			previewRenderer.addClass("drmlink_preview");

			ViewItemUrl vurl = resource.createDefaultViewerUrl();
			vurl.addFlag(ViewItemUrl.FLAG_FULL_URL);
			vurl.add((BookmarkModifier) info.getAttribute(EventAuthoriser.class));
			vurl.setFilepath(UrlEncodedString.BLANK);
			vurl.add(new LicenseOnlyUrl(viewerTag.getElementId(info)));
			SectionInfo drmDialogInfo = vurl.getSectionInfo();
			DRMLicenseDialog licenseDialog = drmDialogInfo.lookupSection(DRMLicenseDialog.class);
			HtmlLinkState licenseLink = new HtmlLinkState(vurl);
			DRMDialogModel state = licenseDialog.getState(drmDialogInfo);
			state.setContentsUrl(vurl);
			FancyBoxDialogRenderer fancyBox = new FancyBoxDialogRenderer(state);
			licenseLink.setClickHandler(new OverrideHandler(fancyBox.createOpenFunction()));
			DRMLinkRenderer drmLink = new DRMLinkRenderer(licenseLink, previewRenderer, viewerTag);
			drmLink.setElementId(new AppendedElementId(viewerTag, "drmLink"));
			previewRenderer.setElementId(new AppendedElementId(viewerTag, "previewLink"));
			previewRenderer.setRel("preview");

			previewRenderer.setNestedRenderable(new SimpleSectionResult("PREVIEW"));
			drmLink.setNestedRenderable(viewerTag.getNestedRenderable());
			drmLink.addClass("drmlink_license");
			return drmLink;
		}
		return viewerTag;
	}

	public static class DRMLinkRenderer extends LinkRenderer
	{
		private final LinkTagRenderer previewTag;
		private final LinkTagRenderer viewerTag;

		public DRMLinkRenderer(HtmlLinkState state, LinkTagRenderer previewRenderer, LinkTagRenderer viewerTag)
		{
			super(state);
			this.previewTag = previewRenderer;
			this.viewerTag = viewerTag;
		}

		@Override
		public TagRenderer setNestedRenderable(SectionRenderable nested)
		{
			super.setNestedRenderable(nested);
			viewerTag.setNestedRenderable(nested);
			return this;
		}

		@Override
		protected void writeEnd(SectionWriter writer) throws IOException
		{
			super.writeEnd(writer);
			previewTag.realRender(writer);
			viewerTag.realRender(writer);
		}

		@SuppressWarnings("nls")
		@Override
		public void preRender(PreRenderContext info)
		{
			super.preRender(info);
			viewerTag.registerUse();
			previewTag.registerUse();
			viewerTag.ensureClickable();
			previewTag.ensureClickable();
			info.preRender(previewTag, viewerTag, new IncludeFile(resources.url("scripts/drmlicense.js")),
				new CssInclude(resources.url("css/drmlicense.css")));
		}

		// Really, there should be overridden properties for everything. This is
		// just the
		// one I actually needed.
		@Override
		public void setTitle(Label title)
		{
			super.setTitle(title);
			viewerTag.setTitle(title);
			previewTag.setTitle(title);
		}
	}
}
