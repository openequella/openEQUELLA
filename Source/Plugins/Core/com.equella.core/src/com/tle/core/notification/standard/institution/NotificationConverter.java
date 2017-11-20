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

package com.tle.core.notification.standard.institution;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.ItemXmlMigrator;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.dao.NotificationDao;
import com.tle.core.plugins.impl.PluginServiceImpl;

@SuppressWarnings("nls")
@Bind
@Singleton
public class NotificationConverter extends AbstractConverter<Object> implements ItemXmlMigrator
{
	static final String CONVERTER_ID = "notifications";
	static final String CONVERTER_FOLDER = CONVERTER_ID;
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(NotificationConverter.class) + ".";
	private static final String KEY_NAME = KEY_PREFIX + "converter";

	@Inject
	private NotificationDao dao;
	private XStream xstream;

	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	public String getStringId()
	{
		return CONVERTER_ID;
	}

	@PostConstruct
	public void setupXStream()
	{
		xstream = xmlHelper.createXStream(getClass().getClassLoader());
	}

	@Override
	protected NameValue getStandardTask()
	{
		return new BundleNameValue(KEY_NAME, getStringId());
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, CONVERTER_FOLDER);
		xmlHelper.writeExportFormatXmlFile(folder, true);
		List<Long> ids = dao.enumerateAllIds();
		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg"); //$NON-NLS-1$
		params.setMessageCallback(message);
		message.setTotal(ids.size());
		message.setType(CurrentLocale.get(KEY_NAME));
		message.setCurrent(0);
		for( Long id : ids )
		{
			Notification notification = dao.findById(id);
			final BucketFile bucketFolder = new BucketFile(folder, id);
			xmlHelper.writeXmlFile(bucketFolder, id + ".xml", notification, xstream);
			message.incrementCurrent();
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		dao.deleteAllForInstitution(institution);
	}

	@Override
	public void importIt(TemporaryFileHandle staging, final Institution institution, ConverterParams params, String cid)
		throws IOException
	{
		final SubTemporaryFile folder = new SubTemporaryFile(staging, CONVERTER_FOLDER);
		List<String> files = xmlHelper.getXmlFileList(folder);
		final DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg"); //$NON-NLS-1$
		params.setMessageCallback(message);
		message.setTotal(files.size());
		message.setType(CurrentLocale.get(KEY_NAME));
		message.setCurrent(0);
		for( final String file : files )
		{
			doInTransaction(new Runnable()
			{
				@Override
				public void run()
				{
					Notification notification = xmlHelper.readXmlFile(folder, file, xstream);
					notification.setInstitution(institution);
					dao.save(notification);
					dao.flush();
					dao.clear();
					message.incrementCurrent();
				}
			});
		}
	}

	@Override
	public void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception
	{
		// nothing
	}

	@Override
	public void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file)
		throws Exception
	{
		final SubTemporaryFile folder = new SubTemporaryFile(staging, CONVERTER_FOLDER);
		xmlHelper.writeExportFormatXmlFile(folder, true);
		params.setAttribute(ConvertInfo.class, new ConvertInfo(folder));
	}

	private static class ConvertInfo
	{
		SubTemporaryFile folder;
		int notificationUpto;

		public ConvertInfo(SubTemporaryFile folder)
		{
			this.folder = folder;
		}
	}

	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		PropBagEx notifications = xml.getSubtree("usersNotified");
		if( notifications != null )
		{
			Iterator<String> iter = notifications.iterateAllValues("*");
			while( iter.hasNext() )
			{
				String user = iter.next();
				Notification notification = new Notification();
				notification.setUserTo(user);
				notification.setDate(new Date());
				notification.setReason(Notification.REASON_WENTLIVE);
				String itemid = xml.getNode("uuid") + '/' + xml.getNode("version");
				notification.setItemid(itemid);
				notification.setItemidOnly(itemid);
				ConvertInfo info = params.getAttribute(ConvertInfo.class);
				final BucketFile bucketFolder = new BucketFile(info.folder, info.notificationUpto);
				xmlHelper.writeXmlFile(bucketFolder, (++info.notificationUpto) + ".xml", notification, xstream);
			}
			notifications.deleteNode("");
			return true;
		}
		return false;
	}
}
