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

package com.tle.web.viewitem.conversion;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.office2html.service.Office2HtmlConversionService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class ConvertToHtmlViewer extends AbstractResourceViewer implements ViewItemViewer
{
	@SuppressWarnings("nls")
	private static Set<String> SUPPORTED = new HashSet<String>(
		Arrays.asList("application/msword", "application/vnd.ms-excel", "application/powerpoint"));

	@Inject
	private Office2HtmlConversionService conversionService;

	@Override
	public String getViewerId()
	{
		return "tohtml"; //$NON-NLS-1$
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource)
	{
		return this;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		ViewableItem viewableItem = resource.getViewableItem();
		String convertedFile;
		try
		{
			convertedFile = conversionService.convert(viewableItem.getFileHandle(), resource.getFilepath(), "html"); //$NON-NLS-1$
		}
		catch( Exception e )
		{
			throw new SectionsRuntimeException(e);
		}
		info.forwardToUrl(viewableItem.createStableResourceUrl(convertedFile).getHref(), 302);
		return null;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		String mimeType = resource.getMimeType();
		if( SUPPORTED.contains(mimeType) )
		{
			IAttachment attach = resource.getAttachment();
			if( attach != null )
			{
				if( attach instanceof FileAttachment )
				{
					return ((FileAttachment) attach).isConversion();
				}
			}
			return true;
		}
		return false;
	}
}
