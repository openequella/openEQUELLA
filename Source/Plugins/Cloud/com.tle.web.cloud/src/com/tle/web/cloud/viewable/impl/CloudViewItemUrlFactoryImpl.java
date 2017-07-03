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

package com.tle.web.cloud.viewable.impl;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.viewable.CloudViewItemUrl;
import com.tle.web.cloud.viewable.CloudViewItemUrlFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.registry.TreeRegistry;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(CloudViewItemUrlFactory.class)
public class CloudViewItemUrlFactoryImpl implements CloudViewItemUrlFactory
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SectionsController sectionsController;
	@Inject
	private TreeRegistry treeRegistry;

	@Override
	public CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem)
	{
		return createItemUrl(info, viewableItem, 0);
	}

	@Override
	public CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, int flags)
	{
		final String itemdir = viewableItem.getItemdir();
		final String path = viewableItem.isIntegration() ? "/summary" : "";
		final SectionInfo fwd = createViewInfo(info, PathUtils.urlPath(itemdir, path));
		return new CloudViewItemUrl(fwd, itemdir, UrlEncodedString.createFromFilePath(path), institutionService, flags);
	}

	@Override
	public CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment attachment)
	{
		return createItemUrl(info, viewableItem, attachment, 0);
	}

	@Override
	public CloudViewItemUrl createItemUrl(SectionInfo info, CloudViewableItem viewableItem, CloudAttachment attachment,
		int flags)
	{
		final String itemdir = viewableItem.getItemdir();
		final String path = "/" + PathUtils.urlPath("attachment", attachment.getUuid());
		final SectionInfo fwd = createViewInfo(info, PathUtils.urlPath(itemdir, path));
		return new CloudViewItemUrl(fwd, itemdir, UrlEncodedString.createFromFilePath(path), institutionService, flags);
	}

	private SectionInfo createViewInfo(SectionInfo existing, String itemdir)
	{
		HttpServletRequest request = existing.getRequest();
		HttpServletResponse response = existing.getResponse();
		SectionTree tree = treeRegistry.getTreeForPath("/cloud/viewitem.do");
		URI institutionUri = institutionService.getInstitutionUri();
		URI itemDirUri;
		try
		{
			itemDirUri = new URI(null, null, itemdir, null);
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException(e);
		}
		URI relativeItemDir = institutionUri.relativize(institutionUri.resolve(itemDirUri));
		return sectionsController.createInfo(tree, '/' + relativeItemDir.getPath(), request, response, existing, null,
			null);
	}
}
