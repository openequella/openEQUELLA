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

package com.tle.web.sections.equella.viewers;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryFancyBox;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.template.DialogTemplate;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

@NonNullByDefault
public abstract class AbstractResourceViewer implements ResourceViewer
{
	@Inject
	private MimeTypeService mimeTypeService;

	@Inject
	protected DialogTemplate dialogTemplate;

	public abstract String getViewerId();

	@Nullable
	public abstract Class<? extends SectionId> getViewerSectionClass();

	@Override
	public LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource, Bookmark viewUrl)
	{
		HtmlLinkState state = new HtmlLinkState(viewUrl);
		LinkTagRenderer renderer = null;
		ResourceViewerConfig config = getViewerConfig(resource);
		if( config != null )
		{
			renderer = createLinkFromConfig(info, resource, config, state);
		}
		if( renderer == null )
		{
			renderer = new LinkRenderer(state);
		}
		renderer.setDisabled(resource.isDisabled());
		return renderer;
	}

	@Override
	public LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource)
	{
		return createLinkRenderer(info, resource, createStreamUrl(info, resource));
	}

	@Nullable
	protected ResourceViewerConfig getViewerConfig(ViewableResource resource)
	{
		MimeEntry entry = mimeTypeService.getEntryForMimeType(resource.getMimeType());
		if( entry == null )
		{
			return null;
		}
		ResourceViewerConfig config = mimeTypeService.getBeanFromAttribute(entry,
			MimeTypeConstants.KEY_VIEWER_CONFIG_PREFIX + getViewerId(), ResourceViewerConfig.class);
		return config;
	}

	@SuppressWarnings("nls")
	@Nullable
	protected LinkTagRenderer createLinkFromConfig(SectionInfo info, ViewableResource resource,
		ResourceViewerConfig config, HtmlLinkState state)
	{
		if( !config.isOpenInNewWindow() )
		{
			return null;
		}

		if( config.isThickbox() )
		{

			String height = config.getHeight();
			String width = config.getWidth();

			ObjectExpression exp = new ObjectExpression();

			if( !Check.isEmpty(height) )
			{
				exp.put("height", height.contains("%") ? height : Integer.valueOf(height));
			}

			if( !Check.isEmpty(width) )
			{
				exp.put("width", width.contains("%") ? width : Integer.valueOf(width));
			}

			state.setClickHandler(new OverrideHandler());
			state.addClass("iframe");
			state.addReadyStatements(Js.handler(JQuerySelector.methodCallExpression(state,
				new ExternallyDefinedFunction("fancybox", JQueryFancyBox.PRERENDER), exp)));
			LinkRenderer linkRenderer = new LinkRenderer(state);

			return linkRenderer;
		}
		PopupLinkRenderer popup = new PopupLinkRenderer(state);
		popup.setWidth(config.getWidth());
		popup.setHeight(config.getHeight());
		return popup;
	}

	@Override
	public ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource)
	{
		ViewItemUrl viewerUrl = resource.createDefaultViewerUrl();
		viewerUrl.setViewer(getViewerId());
		return viewerUrl;
	}

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		return createViewItemUrl(info, resource);
	}

	@Nullable
	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		Class<? extends SectionId> viewerSectionClass = getViewerSectionClass();
		if( viewerSectionClass != null )
		{
			return (ViewItemViewer) info.lookupSection(viewerSectionClass);
		}
		return null;
	}

	@Nullable
	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		return defaultDialog;
	}

	// Implements part of the ViewItemViewer interface
	@Nullable
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableResource viewableResource = resource.getAttribute(ViewableResource.class);
		if( viewableResource != null )
		{
			return viewableResource.getAttachment();
		}
		return null;
	}
}
