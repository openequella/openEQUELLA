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

package com.tle.web.viewitem.treeviewer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ItemUrlExtender;
import com.tle.web.viewurl.ResourceViewerConfigDialog;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class DownloadPackageViewer extends AbstractResourceViewer
{
	protected static final String DOWNLOADIMS = "downloadIms";

	private PluginTracker<ItemUrlExtender> downloadLink;

	@Inject
	private ViewItemUrlFactory urlFactory;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		downloadLink = new PluginTracker<ItemUrlExtender>(pluginService,
			pluginService.getPluginIdForObject(getClass()), "downloadLink", "id");
		downloadLink.setBeanKey("class");
	}

	@Override
	public String getViewerId()
	{
		return DOWNLOADIMS;
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return null;
	}

	@Override
	public ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog)
	{
		return null;
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		String mimeType = resource.getMimeType();
		return (mimeType.equals(MimeTypeConstants.MIME_IMS) || mimeType.equals(MimeTypeConstants.MIME_SCORM));
	}

	@Override
	public Bookmark createStreamUrl(SectionInfo info, ViewableResource resource)
	{
		return createViewItemUrl(info, resource);
	}

	@Override
	public ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource)
	{
		UrlEncodedString urlFilepath = !Check.isEmpty(resource.getFilepath()) ? UrlEncodedString
			.createFromValue(resource.getFilepath()) : null;
		ViewItemUrl vurl = urlFactory.createItemUrl(resource.getInfo(), resource.getViewableItem(), urlFilepath, 0);
		// If the factory hasn't identified our filepath placeholder (eg
		// "viewims.jsp", or "viewscorm.jsp" etc)
		if( vurl.getFilepath() == null || Check.isEmpty(vurl.getFilepath().getUnencodedString()) )
		{
			vurl.setFilepath(urlFilepath);
		}
		vurl.addFlag(ViewItemUrl.FLAG_NO_SELECTION);
		vurl.setViewer(getViewerId());
		String mimeType = resource.getMimeType();
		vurl.add(getDownloadFor(mimeType));
		return vurl;
	}

	public ItemUrlExtender getDownloadFor(String mimeType)
	{
		return downloadLink.getBeanByExtension(downloadLink.getExtension(mimeType));
	}
}
