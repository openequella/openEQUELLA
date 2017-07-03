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

package com.tle.core.legacy.migration.v50;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.filesystem.FileSystemHelper;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveAssemblerAndActivityWizardItemsFix extends AbstractItemXmlMigrator
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private XmlService xmlService;

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean changed = xml.deleteAll("folder");
		changed |= xml.deleteAll("planMetadata");

		Iterator<PropBagEx> iter = xml.iterateAll("attachments/com.tle.beans.item.attachments.ActivityAttachment");
		String dirname = filename.substring(0, filename.indexOf('.'));
		String activitiesDir = dirname + "/_activity";
		Map<String, Integer> perType = new HashMap<String, Integer>();
		int index = 0;
		while( iter.hasNext() )
		{
			changed = true;
			PropBagEx activity = iter.next();
			String attachUuid = activity.getNode("uuid");
			String itemId = activity.getNode("value2");
			String url = activity.getNode("url");
			String newClazz;

			if( !Check.isEmpty(itemId) )
			{
				newClazz = "com.tle.beans.item.attachments.CustomAttachment";
				Map<String, Object> dataMap = new HashMap<String, Object>();
				ItemId itemIdKey = new ItemId(itemId);
				dataMap.put("uuid", itemIdKey.getUuid());
				dataMap.put("version", itemIdKey.getVersion());
				dataMap.put("type", "p");
				activity.setNode("url", activity.getNode("value3"));
				activity.setNode("value1", "resource");
				activity.deleteNode("value2");
				activity.deleteNode("value3");
				activity.deleteNode("data");
				activity.newSubtree("data").appendChildren("", new PropBagEx(xmlService.serialiseToXml(dataMap)));
			}
			else if( !Check.isEmpty(url) )
			{
				// Yes this is dodge, the data in the url is a full url to the
				// item!
				int internalItem = url.lastIndexOf("/items/");
				if( internalItem == -1 )
				{
					newClazz = "com.tle.beans.item.attachments.LinkAttachment";
					activity.setNode("value1", "false");
					activity.deleteNode("data");
				}
				else
				{
					newClazz = "com.tle.beans.item.attachments.CustomAttachment";
					url = url.substring(internalItem + 7);
					final int verindex = url.indexOf('/');
					final String uuid = url.substring(0, verindex);
					final int endindex = url.indexOf('/', verindex + 1);
					final int version = Integer.parseInt(url.substring(verindex + 1, endindex));

					String extra = url.substring(endindex + 1);
					try
					{
						extra = URLDecoder.decode(url.substring(endindex + 1), Constants.UTF8);
						if( extra.isEmpty() )
						{
							extra = "viewdefault.jsp";
						}
					}
					catch( UnsupportedEncodingException e )
					{
						throw new RuntimeException(e);
					}

					final Map<String, Object> dataMap = new HashMap<String, Object>();
					final ItemId itemIdKey = new ItemId(uuid, version);
					dataMap.put("uuid", itemIdKey.getUuid());
					dataMap.put("version", itemIdKey.getVersion());
					dataMap.put("type", "p");
					activity.setNode("url", extra);
					activity.setNode("value1", "resource");
					activity.deleteNode("value2");
					activity.deleteNode("value3");
					activity.deleteNode("data");
					activity.newSubtree("data").appendChildren("", new PropBagEx(xmlService.serialiseToXml(dataMap)));
				}
			}
			else
			{
				newClazz = "com.tle.beans.item.attachments.HtmlAttachment";
				String htmlFile = activitiesDir + '/' + FileSystemHelper.encode(activity.getNode("description"))
					+ ".html";
				String destDir = dirname + "/_mypages/" + attachUuid;
				String destFile = destDir + "/page.html";
				fileSystemService.mkdir(file, destDir);
				fileSystemService.rename(file, htmlFile, destFile);
				long length = 0;
				try
				{
					length = fileSystemService.fileLength(file, destFile);
				}
				catch( FileNotFoundException ex )
				{
					// Ignore
				}
				activity.setNode("value1", length);
				activity.deleteNode("data");
			}

			activity.setNodeName(newClazz);
			Integer perCount = perType.get(newClazz);
			if( perCount == null )
			{
				perCount = 0;
			}
			perType.put(newClazz, ++perCount);
			PropBagEx treeNodes = xml.aquireSubtree("treeNodes");

			PropBagEx newNode = treeNodes.newSubtree("com.tle.beans.item.attachments.ItemNavigationNode");
			newNode.setNode("uuid", UUID.randomUUID().toString());
			newNode.setNode("name", activity.getNode("description"));
			newNode.setNode("index", index++);
			PropBagEx tab = newNode.newSubtree("tabs").newSubtree("com.tle.beans.item.attachments.ItemNavigationTab");
			PropBagEx attach = tab.newSubtree("attachment");
			attach.setNode("@class", newClazz);
			String reference = "../../../../../attachments/" + newClazz;
			if( perCount > 1 )
			{
				reference += "[" + perCount + "]";
			}
			attach.setNode("@reference", reference);
			tab.setNode("name", "Default");
		}

		if( fileSystemService.fileExists(file, activitiesDir) )
		{
			fileSystemService.removeFile(file, activitiesDir);
		}

		return changed;
	}
}