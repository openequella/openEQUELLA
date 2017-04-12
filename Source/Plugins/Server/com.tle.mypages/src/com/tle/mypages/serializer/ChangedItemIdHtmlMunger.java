package com.tle.mypages.serializer;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.CloneFileProcessingExtension;
import com.tle.mypages.parse.ConvertHtmlService;
import com.tle.mypages.parse.conversion.PrefixConversion;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ChangedItemIdHtmlMunger implements CloneFileProcessingExtension
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ConvertHtmlService convertHtmlService;

	private Reader getReader(FileHandle handle, String filename)
	{
		try
		{
			return new InputStreamReader(fileSystemService.read(handle, filename), Constants.UTF8);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void processFiles(ItemId oldId, FileHandle oldHandle, Item newItem, FileHandle newHandle)
	{
		for( Attachment attachment : newItem.getAttachments() )
		{
			if( attachment instanceof HtmlAttachment )
			{
				HtmlAttachment html = (HtmlAttachment) attachment;
				try( Reader reader = getReader(oldHandle, html.getFilename()) )
				{
					String newHtml = convertHtmlService.convert(reader, false, new PrefixConversion("file/" + oldId,
						"file/" + newItem.getUuid() + "/" + newItem.getVersion()), new PrefixConversion(
						"item/" + oldId, "item/" + newItem.getUuid() + "/" + newItem.getVersion()));
					fileSystemService.write(newHandle, html.getFilename(),
						new ByteArrayInputStream(newHtml.getBytes(Constants.UTF8)), false);
				}
				catch( Exception e )
				{
					throw Throwables.propagate(e);
				}
			}
		}
	}
}
