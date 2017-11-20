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

package com.tle.web.galleryviewer;

import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewableResource;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class FancyBoxViewer extends AbstractResourceViewer
{
	@Override
	public String getViewerId()
	{
		return "fancy";
	}

	@Nullable
	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource, Bookmark viewUrl)
	{
		final HtmlLinkState state = new HtmlLinkState(viewUrl);
		final ItemKey itemId = resource.getViewableItem().getItemId();
		state.setRel(itemId.toString());
		return new FancyBoxLinkRenderer(state);
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		// Does not (currently) support external resources because of a XSS
		// limitation (the dialog doesn't like the redirect to an external site)
		if( resource.isExternalResource() )
		{
			return false;
		}
		String mimeType = resource.getMimeType();
		return (mimeType.startsWith("image")) && !resource.getBooleanAttribute(ViewableResource.KEY_TARGETS_FRAME);
	}

	@Nullable
	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		return null;
	}
}
