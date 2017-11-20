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

package com.tle.web.cloud.search;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.renderers.ImageRenderer;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class CloudSearchListEntryFactory
{
	@Inject
	private Provider<CloudSearchListEntry> entryProvider;

	public CloudSearchListEntry createListEntry(SectionInfo info, CloudItem result)
	{
		final CloudSearchListEntry listItem = entryProvider.get();
		listItem.setInfo(info);
		listItem.setItem(result);

		final List<CloudAttachment> attachments = result.getAttachments();
		if( attachments != null )
		{
			for( CloudAttachment attachment : attachments )
			{
				final String thumbUrl = attachment.getThumbnail();
				if( thumbUrl != null )
				{
					listItem.addThumbnail(new ImageRenderer(thumbUrl, new TextLabel(attachment.getDescription())));
				}
			}
		}
		return listItem;
	}
}
