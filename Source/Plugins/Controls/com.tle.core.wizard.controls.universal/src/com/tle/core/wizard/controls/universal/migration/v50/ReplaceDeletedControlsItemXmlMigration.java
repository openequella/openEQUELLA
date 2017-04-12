package com.tle.core.wizard.controls.universal.migration.v50;

import java.util.Map;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.ImmutableMap;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.migration.AbstractItemXmlMigrator;

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
