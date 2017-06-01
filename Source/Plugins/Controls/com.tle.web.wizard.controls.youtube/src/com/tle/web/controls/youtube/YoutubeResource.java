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

package com.tle.web.controls.youtube;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.RegisterMimeTypeExtension;
import com.tle.freetext.SupportedVideoMimeTypeExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceExtension;

@NonNullByDefault
@Bind
@Singleton
public class YoutubeResource
	implements
		AttachmentResourceExtension<CustomAttachment>,
		RegisterMimeTypeExtension<CustomAttachment>,
		SupportedVideoMimeTypeExtension
{
	@Inject
	private SelectionService selection;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@Override
	public ViewableResource process(SectionInfo info, ViewableResource resource, CustomAttachment attachment)
	{
		return new YoutubeViewableResource(resource, attachment, selection, info, dateRendererFactory);
	}

	@Override
	public String getMimeType(CustomAttachment attachment)
	{
		return YoutubeUtils.MIME_TYPE;
	}

	@Override
	public boolean isSupportedMimeType(@Nullable String mimeType)
	{
		if( mimeType != null && mimeType.contains(YoutubeUtils.MIME_TYPE) )
		{
			return true;
		}
		return false;
	}
}
