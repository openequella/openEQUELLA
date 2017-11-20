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

package com.tle.cal.migration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.activation.convert.ActivationsConverter;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.impl.BaseEntityXmlConverter;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
public class CalItemXmlConvert extends AbstractItemXmlMigrator
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private EntityRegistry registry;
	@Inject
	private XmlService xmlService;

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		String id = filename.substring(0, filename.indexOf('.'));
		Map<String, String> attachmentMap = getAttachmentMap(xml);
		final int attachmentsCount = attachmentMap.size();

		boolean modified = false;

		processActivateRequests(xml, attachmentMap, file, id);
		processMetadata(attachmentMap, file, id, xml);

		modified |= removeDeprecatedItemPath(xml);
		modified |= attachmentsCount != attachmentMap.size();

		return modified;
	}

	private Date getDateField(XStream xstream, PropBagEx subtree)
	{
		return (Date) xstream.fromXML(subtree.toString());
	}

	private Map<String, String> getAttachmentMap(PropBagEx xml)
	{
		Map<String, String> map = new HashMap<String, String>();
		PropBagThoroughIterator iter = xml.iterateAll("attachments/*"); //$NON-NLS-1$
		while( iter.hasNext() )
		{
			PropBagEx attach = iter.next();
			map.put(attach.getNode("url").trim(), attach.getNode("uuid").trim()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return map;
	}

	private void processActivateRequests(PropBagEx xml, Map<String, String> attachmentMap, SubTemporaryFile file,
		String id) throws IOException
	{
		XStream xstream = getXStream();

		xstream.alias("com.tle.beans.cal.ActivateRequest", ActivateRequest.class); //$NON-NLS-1$
		List<ActivateRequest> actRequests = new ArrayList<ActivateRequest>();
		PropBagThoroughIterator iter = xml.iterateAll("activateRequests/*"); //$NON-NLS-1$
		while( iter.hasNext() )
		{
			PropBagEx actXml = iter.next();
			ActivateRequest request = new ActivateRequest();
			request.setUuid(UUID.randomUUID().toString());
			request.setUser(actXml.getNode("user")); //$NON-NLS-1$
			request.setStatus(actXml.getIntNode("status")); //$NON-NLS-1$
			CourseInfo course = new CourseInfo();
			course.setUuid(actXml.getNode("course/@uuid")); //$NON-NLS-1$
			request.setCourse(course);
			if( actXml.nodeExists("citation") ) //$NON-NLS-1$
			{
				request.setCitation(actXml.getNode("citation")); //$NON-NLS-1$
			}
			if( actXml.nodeExists("description") ) //$NON-NLS-1$
			{
				request.setDescription(actXml.getNode("description")); //$NON-NLS-1$
			}
			if( actXml.nodeExists("locationId") ) //$NON-NLS-1$
			{
				request.setLocationId(actXml.getNode("locationId")); //$NON-NLS-1$
			}
			if( actXml.nodeExists("locationName") ) //$NON-NLS-1$
			{
				request.setLocationName(actXml.getNode("locationName")); //$NON-NLS-1$
			}
			request.setFrom(getDateField(xstream, actXml.getSubtree("from"))); //$NON-NLS-1$
			request.setTime(getDateField(xstream, actXml.getSubtree("time"))); //$NON-NLS-1$
			request.setUntil(getDateField(xstream, actXml.getSubtree("until"))); //$NON-NLS-1$
			request.setAttachment(convertAttachment(attachmentMap, xml, actXml.getNode("attachment"))); //$NON-NLS-1$
			actRequests.add(request);
		}

		String newActxml = xstream.toXML(actRequests);
		ByteArrayInputStream inp = new ByteArrayInputStream(newActxml.getBytes("UTF-8")); //$NON-NLS-1$
		fileSystemService.write(file, id + "-extra/" + ActivationsConverter.ACTIVATIONS_XML, inp, //$NON-NLS-1$
			false);
	}

	private void processMetadata(Map<String, String> attachmentMap, SubTemporaryFile file, String id, PropBagEx dbXml)
		throws IOException
	{
		final String metadataPath = id + "/_ITEM/item.xml"; //$NON-NLS-1$

		try( InputStream in = fileSystemService.read(file, metadataPath) )
		{
			PropBagEx metadata = new PropBagEx(in);

			boolean changesMade = false;
			Iterator<PropBagEx> iter = metadata
				.iterateAll("item/copyright/portions/portion/sections/section/attachment"); //$NON-NLS-1$
			while( iter.hasNext() )
			{
				PropBagEx attachXml = iter.next();
				attachXml.setNode("", convertAttachment(attachmentMap, dbXml, attachXml.getNode() //$NON-NLS-1$
					.trim()));
				changesMade = true;
			}

			if( changesMade )
			{
				try( OutputStreamWriter outStream = new OutputStreamWriter(
					fileSystemService.getOutputStream(file, metadataPath, false)) )
				{
					outStream.write(metadata.toString());
				}
			}
		}
	}

	private String convertAttachment(Map<String, String> attachmentMap, PropBagEx dbXml, String filename)
	{
		String uuid = attachmentMap.get(filename);
		if( uuid == null )
		{
			uuid = UUID.randomUUID().toString();

			// Create a new link attachment
			PropBagEx na = dbXml.newSubtree("attachments/" //$NON-NLS-1$
				+ "com.tle.beans.item.attachments.LinkAttachment"); //$NON-NLS-1$
			na.setNode("id", 0); //$NON-NLS-1$
			na.setNode("uuid", uuid); //$NON-NLS-1$
			na.setNode("url", filename); //$NON-NLS-1$
			na.setNode("description", filename); //$NON-NLS-1$
			na.setNode("value1", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			na.setNode("value2", ""); //$NON-NLS-1$ //$NON-NLS-2$

			attachmentMap.put(filename, uuid);
		}
		return uuid;
	}

	public static boolean removeDeprecatedItemPath(PropBagEx xml)
	{
		boolean modified = false;
		modified |= xml.deleteAll("calItem"); //$NON-NLS-1$
		modified |= xml.deleteAll("activateRequests"); //$NON-NLS-1$
		return modified;
	}

	public XStream getXStream()
	{
		XStream xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.registerConverter(new BaseEntityXmlConverter(registry));
		return xstream;
	}
}
