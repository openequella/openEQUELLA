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

package com.tle.web.viewitem;

import com.tle.common.Check;
import com.tle.web.sections.Bookmark;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.WrappedViewItemResource;

public abstract class AbstractAttachmentViewItemResource extends WrappedViewItemResource
{
	private final ViewableResource viewableResource;
	protected final boolean forcedStream;

	public AbstractAttachmentViewItemResource(ViewItemResource inner, ViewableResource viewableResource,
		boolean forcedStream)
	{
		super(inner);
		setAttribute(ViewableResource.class, viewableResource);
		this.viewableResource = viewableResource;
		this.forcedStream = forcedStream;
	}

	@Override
	public String getFilepath()
	{
		return viewableResource.getFilepath();
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return viewableResource.getViewAuditEntry();
	}

	@Override
	public Bookmark createCanonicalURL()
	{
		return viewableResource.createCanonicalUrl();
	}

	@Override
	public ContentStream getContentStream()
	{
		return viewableResource.getContentStream();
	}

	@Override
	public String getMimeType()
	{
		return viewableResource.getMimeType();
	}

	@Override
	public String getDefaultViewerId()
	{
		String viewerId = inner.getDefaultViewerId();
		if( viewerId == null )
		{
			viewerId = viewableResource.getAttachment().getViewer();
		}
		return Check.isEmpty(viewerId) ? null : viewerId;
	}

	@Override
	public boolean isPathMapped()
	{
		return false;
	}

	@Override
	public boolean isRestrictedResource()
	{
		return viewableResource.getAttachment().isRestricted();
	}
}
