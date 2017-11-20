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

package com.tle.web.viewitem.flvviewer;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.template.Decorations;
import com.tle.web.viewitem.viewer.AbstractViewerSection;
import com.tle.web.viewurl.ResourceViewerConfig;
import com.tle.web.viewurl.ViewItemResource;

@Bind
public class FLVViewerSection extends AbstractViewerSection<FLVViewerSection.FLVViewerModel>
{
	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public String getDefaultPropertyName()
	{
		return "flv"; //$NON-NLS-1$
	}

	@Override
	public Class<FLVViewerModel> getModelClass()
	{
		return FLVViewerModel.class;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@SuppressWarnings("null")
	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		Decorations.getDecorations(info).clearAllDecorations();
		FLVViewerModel model = getModel(info);
		model.setFlvUrl(resource.createCanonicalURL().getHref());

		String height = null;
		String width = null;

		MimeEntry entry = mimeTypeService.getEntryForMimeType(resource.getMimeType());
		if( entry != null )
		{
			ResourceViewerConfig config = getResourceViewerConfig(mimeTypeService, resource, "flvViewer");

			if( config != null )
			{
				Map<String, Object> attr = config.getAttr();
				height = (String) attr.get("flvHeight"); //$NON-NLS-1$
				width = (String) attr.get("flvWidth"); //$NON-NLS-1$

			}

		}

		if( Check.isEmpty(height) || height.equals("undefined") ) //$NON-NLS-1$
		{
			height = "480"; //$NON-NLS-1$
		}

		if( Check.isEmpty(width) || width.equals("undefined") ) //$NON-NLS-1$
		{
			width = "640"; //$NON-NLS-1$
		}
		model.setHeight(height);
		model.setWidth(width);
		return viewFactory.createTemplateResult("viewer.ftl", this); //$NON-NLS-1$
	}

	public static class FLVViewerModel
	{
		private String flvUrl;
		private String width;
		private String height;

		public String getFlvUrl()
		{
			return flvUrl;
		}

		public void setFlvUrl(String flvUrl)
		{
			this.flvUrl = flvUrl;
		}

		public void setHeight(String height)
		{
			this.height = height;
		}

		public String getHeight()
		{
			return height;
		}

		public void setWidth(String width)
		{
			this.width = width;
		}

		public String getWidth()
		{
			return width;
		}

	}
}
