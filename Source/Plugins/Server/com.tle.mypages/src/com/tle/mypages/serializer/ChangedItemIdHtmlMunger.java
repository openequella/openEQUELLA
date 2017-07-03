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

package com.tle.mypages.serializer;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.google.common.base.Throwables;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.CloneFileProcessingExtension;
import com.tle.core.services.FileSystemService;
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
					String newHtml = convertHtmlService.convert(reader, false,
						new PrefixConversion("file/" + oldId, "file/" + newItem.getUuid() + "/" + newItem.getVersion()),
						new PrefixConversion("item/" + oldId,
							"item/" + newItem.getUuid() + "/" + newItem.getVersion()));
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
