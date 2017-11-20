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

package com.tle.core.wizard.controls.universal.migration.v50;

import java.util.Map;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.ImmutableMap;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReplaceDeletedControlsItemXmlMigration extends AbstractItemXmlMigrator
{
	private static final Map<String, String> CUSTOM_CLASS_TO_HANDLER = ImmutableMap.of("resource", "resourceHandler",
		"youtube", "youTubeHandler", "itunesu", "iTunesUHandler", "googlebook", "googleBookHandler", "flickr",
		"flickrHandler");

	// We don't handle ItemAttachments, they already have an xpath value that
	// just needs populating
	private static final Map<String, String> ATTACHMENT_TO_HANDLER = new ImmutableMap.Builder<String, String>()
		.put("com.tle.beans.item.attachments.LinkAttachment", "urlHandler")
		.put("com.tle.beans.item.attachments.HtmlAttachment", "mypagesHandler")
		.put("com.tle.beans.item.attachments.FileAttachment", "fileHandler")
		.put("com.tle.beans.item.attachments.ZipAttachment", "fileHandler")
		.put("com.tle.beans.item.attachments.ImsAttachment", "fileHandler_pkg")
		.put("com.tle.beans.item.attachments.IMSResourceAttachment", "").build();

	public static String getHandler(String className, String customType)
	{
		if( className.equals("com.tle.beans.item.attachments.CustomAttachment") )
		{
			return CUSTOM_CLASS_TO_HANDLER.get(customType);
		}
		return ATTACHMENT_TO_HANDLER.get(className);
	}

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		// NO-OP
		// Fixed by ReplaceDeletedControlsItemXmlMigrationFixer
		return false;
	}
}
