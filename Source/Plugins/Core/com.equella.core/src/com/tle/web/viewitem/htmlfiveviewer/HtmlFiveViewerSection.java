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

package com.tle.web.viewitem.htmlfiveviewer;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.htmlfiveviewer.HtmlFiveViewerSection.HtmlFiveViewerModel;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ViewItemResource;

@Bind
@SuppressWarnings("nls")
public class HtmlFiveViewerSection extends AbstractViewerSection<HtmlFiveViewerModel>
{
	@PlugURL("scripts/video.js")
	private static String VIDEO_JS;
	@PlugKey("fallback.key")
	private static String FALLBACK_KEY;
	@PlugKey("fallback.ie.key")
	private static String FALLBACK_IE_KEY;
	@PlugURL("scripts/resize.js")
	private static String RESIZE_JS_URL;

	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public Class<HtmlFiveViewerModel> getModelClass()
	{
		return HtmlFiveViewerModel.class;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource) throws IOException
	{
		Decorations.getDecorations(info).clearAllDecorations();
		HtmlFiveViewerModel model = getModel(info);
		String videoUrl = resource.createCanonicalURL().getHref();
		String mimeType = resource.getMimeType();
		model.setVideoUrl(videoUrl);
		model.setVideoType(mimeType);
		model.setFallback(new KeyLabel(FALLBACK_KEY, mimeType, videoUrl));
		model.setFallbackIE(new KeyLabel(FALLBACK_IE_KEY, videoUrl));
		String height = null;
		String width = null;

		MimeEntry entry = mimeTypeService.getEntryForMimeType(resource.getMimeType());
		if( entry != null )
		{
			ResourceViewerConfig config = getResourceViewerConfig(mimeTypeService, resource, "htmlFiveViewer");

			if( config != null )
			{
				Map<String, Object> attr = config.getAttr();
				height = (String) attr.get("html5Height");
				width = (String) attr.get("html5Width");
			}
		}

		if( Check.isEmpty(height) || Objects.equals(height, "undefined") )
		{
			height = "264";
		}

		if( Check.isEmpty(width) || Objects.equals(height, "undefined") )
		{
			width = "640";
		}
		model.setHeight(height);
		model.setWidth(width);

		info.getBody().addPreRenderable(new IncludeFile(VIDEO_JS));
		info.getBody().addReadyStatements(
			Js.statement(Js.call(new ExternallyDefinedFunction("resizeListeners", new IncludeFile(RESIZE_JS_URL)),
				width, height)));
		return viewFactory.createTemplateResult("fiveviewer.ftl", this);
	}

	public static class HtmlFiveViewerModel
	{
		private String videoUrl;
		private String videoType;
		private String width;
		private String height;
		private Label fallback;
		private Label fallbackIE;

		public Label getFallbackIE()
		{
			return fallbackIE;
		}

		public void setFallbackIE(Label fallbackIE)
		{
			this.fallbackIE = fallbackIE;
		}

		public String getVideoUrl()
		{
			return videoUrl;
		}

		public void setVideoUrl(String videoUrl)
		{
			this.videoUrl = videoUrl;
		}

		public String getVideoType()
		{
			return videoType;
		}

		public void setVideoType(String videoType)
		{
			this.videoType = videoType;
		}

		public String getWidth()
		{
			return width;
		}

		public void setWidth(String width)
		{
			this.width = width;
		}

		public String getHeight()
		{
			return height;
		}

		public void setHeight(String height)
		{
			this.height = height;
		}

		public Label getFallback()
		{
			return fallback;
		}

		public void setFallback(Label fallback)
		{
			this.fallback = fallback;
		}

	}

}
