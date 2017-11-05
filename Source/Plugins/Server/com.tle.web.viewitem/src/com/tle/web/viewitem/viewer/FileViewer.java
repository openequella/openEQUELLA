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

package com.tle.web.viewitem.viewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.utils.TokenModifier;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.viewurl.*;

@Bind
@Singleton
public class FileViewer extends AbstractResourceViewer
{
	@Inject
	private UserService userService;
	@Inject
	private ComponentFactory componentFactory;

	@Override
	public String getViewerId()
	{
		return MimeTypeConstants.VAL_DEFAULT_VIEWERID;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		boolean appendToken = isAppendToken(resource);
		if( appendToken || resource.isExternalResource() )
		{
			return createViewItemUrl(resource, appendToken);
		}
		return resource.createCanonicalUrl();
	}

	private boolean isAppendToken(ViewableResource resource)
	{
		ResourceViewerConfig config = getViewerConfig(resource);
		return (config != null && Boolean.TRUE.equals(config.getAttr().get(
			MimeTypeConstants.KEY_VIEWER_CONFIG_APPENDTOKEN)));
	}

	private ViewItemUrl createViewItemUrl(ViewableResource resource, boolean addToken)
	{
		ViewItemUrl viewerUrl = resource.createDefaultViewerUrl();
		viewerUrl.setViewer(getViewerId());
		if( addToken )
		{
			viewerUrl.add(new TokenModifier(userService));
		}
		return viewerUrl;
	}

	@Override
	public ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource)
	{
		return createViewItemUrl(resource, isAppendToken(resource));
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		FileViewerConfigDialog cd = componentFactory.createComponent(parentId, "fcd", tree, //$NON-NLS-1$
			FileViewerConfigDialog.class, true);
		cd.setTemplate(dialogTemplate);
		return cd;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return true;
	}

	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		IAttachment attachment = super.getAttachment(info, resource);
		if (attachment == null)
		{
			return resource.getViewableItem().getAttachmentByFilepath(resource.getFilepath());
		}
		return attachment;
	}
}
