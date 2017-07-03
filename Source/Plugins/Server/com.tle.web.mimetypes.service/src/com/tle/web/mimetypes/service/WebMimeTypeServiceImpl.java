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

package com.tle.web.mimetypes.service;

import java.net.URL;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.URLUtils;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;

@NonNullByDefault
@Bind(WebMimeTypeService.class)
@Singleton
@SuppressWarnings("nls")
public class WebMimeTypeServiceImpl implements WebMimeTypeService
{
	private static final String DEFAULT_ICON = "icons/binary.png";
	private static final PluginResourceHelper URL_HELPER = ResourcesService
		.getResourceHelper(WebMimeTypeServiceImpl.class);

	@Inject
	private SectionsController controller;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private InstitutionService instituionService;

	@Nullable
	@Override
	public MimeEntry getEntryForFilename(String filename)
	{
		return mimeTypeService.getEntryForFilename(filename);
	}

	@Nullable
	@Override
	public MimeEntry getEntryForMimeType(String mimeType)
	{
		return mimeTypeService.getEntryForMimeType(mimeType);
	}

	@Override
	public String getMimeTypeForFilename(String filename)
	{
		return mimeTypeService.getMimeTypeForFilename(filename);
	}

	@Override
	public URL getIconForEntry(@Nullable MimeEntry entry)
	{
		return getIconForEntry(entry, true);
	}

	@Override
	public URL getIconForEntry(@Nullable MimeEntry entry, boolean allowCache)
	{
		if( entry == null )
		{
			return getDefaultIconForEntry(null);
		}

		Map<String, String> attributes = entry.getAttributes();
		if( attributes.containsKey(MimeTypeConstants.KEY_ICON_GIFBASE64) )
		{
			SectionInfo info = controller.createForward("/icon.do");
			IconServer server = info.lookupSection(IconServer.class);
			return URLUtils.newURL(server.getIconUrl(info, entry.getType(), allowCache).getHref());
		}
		return getDefaultIconForEntry(entry);
	}

	@Override
	public URL getDefaultIconForEntry(@Nullable MimeEntry entry)
	{
		String icon = null;
		if( entry != null )
		{
			Map<String, String> attributes = entry.getAttributes();
			icon = attributes.get(MimeTypeConstants.KEY_ICON_WEBAPPRELATIVE);
			if( icon != null )
			{
				return URLUtils.newURL(instituionService.institutionalise(icon));
			}
			icon = attributes.get(MimeTypeConstants.KEY_ICON_PLUGINICON);
		}
		if( icon == null )
		{
			icon = DEFAULT_ICON;
		}
		return URLUtils.newURL(instituionService.institutionalise(URL_HELPER.url(icon)));
	}

	@Override
	public boolean hasCustomIcon(MimeEntry entry)
	{
		return entry.getAttributes().containsKey(MimeTypeConstants.KEY_ICON_GIFBASE64);
	}

	@Override
	public void setIconBase64(MimeEntry entry, @Nullable String base64Icon)
	{
		Map<String, String> attr = entry.getAttributes();
		if( base64Icon != null )
		{
			attr.put(MimeTypeConstants.KEY_ICON_GIFBASE64, base64Icon);
		}
		else if( attr.containsKey(MimeTypeConstants.KEY_ICON_GIFBASE64) )
		{
			attr.remove(MimeTypeConstants.KEY_ICON_GIFBASE64);
		}
	}
}
