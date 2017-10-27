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

package com.tle.core.activation.convert;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.common.io.UnicodeReader;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.entity.service.impl.EntityInitialiserCallback;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.item.convert.ItemConverter.ItemExtrasConverter;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationsConverter implements ItemExtrasConverter
{
	public static final String ACTIVATIONS_XML = "activations.xml";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ActivateRequestDao requestDao;
	@Inject
	private InitialiserService initialiserService;

	@Override
	public void exportExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder)
	{
		Item item = info.getItem();
		List<ActivateRequest> allRequests = requestDao.getAllRequests(item);
		allRequests = initialiserService.initialise(allRequests, new EntityInitialiserCallback());
		if( !allRequests.isEmpty() )
		{
			for( ActivateRequest activateRequest : allRequests )
			{
				activateRequest.setItem(null);
			}
			String xml = xstream.toXML(allRequests);
			try
			{
				fileSystemService.write(extrasFolder, ACTIVATIONS_XML, new StringReader(xml), false);
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void importExtras(ItemConverterInfo info, XStream xstream, SubTemporaryFile extrasFolder)
	{
		Item item = info.getItem();
		if( fileSystemService.fileExists(extrasFolder, ACTIVATIONS_XML) )
		{
			try( Reader reader = new UnicodeReader(fileSystemService.read(extrasFolder, ACTIVATIONS_XML), "UTF-8") )
			{
				List<ActivateRequest> requests = (List<ActivateRequest>) xstream.fromXML(reader);
				for( ActivateRequest request : requests )
				{
					request.setItem(item);
					request.setId(0);
					requestDao.saveAny(request);
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}
}
