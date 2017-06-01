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

import javax.inject.Inject;

import com.dytech.devlib.Base64;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.stream.ByteArrayContentStream;
import com.tle.web.stream.ContentStreamWriter;

@Bind
public class IconServer extends AbstractPrototypeSection<Object>
{
	@Inject
	private MimeTypeService mimeService;
	@EventFactory
	private EventGenerator events;
	@Inject
	private ContentStreamWriter contentStreamWriter;

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	public Bookmark getIconUrl(SectionInfo info, String mimeType, boolean allowCache)
	{
		return new BookmarkAndModify(info, events.getNamedModifier("serve", mimeType, allowCache)); //$NON-NLS-1$
	}

	@EventHandlerMethod(preventXsrf = false)
	public void serve(SectionInfo info, String mimeType, boolean allowCache)
	{
		MimeEntry entry = mimeService.getEntryForMimeType(mimeType);
		String base64Icon = entry.getAttributes().get(MimeTypeConstants.KEY_ICON_GIFBASE64);
		byte[] bytes = new Base64().decode(base64Icon);
		ByteArrayContentStream stream = new ByteArrayContentStream(bytes, "icon.gif", "image/gif"); //$NON-NLS-1$ //$NON-NLS-2$
		if( !allowCache )
		{
			stream.setCacheControl("no-cache"); //$NON-NLS-1$
		}
		stream.setContentDisposition("inline"); //$NON-NLS-1$
		info.setRendered();
		contentStreamWriter.outputStream(info.getRequest(), info.getResponse(), stream);
	}
}
