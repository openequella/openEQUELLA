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

package com.tle.core.oauth.convert;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.oauth.beans.OAuthToken;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.oauth.dao.OAuthTokenDao;
import com.tle.core.oauth.service.OAuthService;

import com.tle.core.xml.service.impl.XmlServiceImpl;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class OAuthTokenConverter extends AbstractConverter<OAuthToken>
{
	private static final String PREFIX = "com.tle.core.oauth.";
	private static final String OAUTHTOKENS = "oauthtoken";

	@Inject
	private OAuthTokenDao tokenDao;
	@Inject
	private OAuthService oauthService;
	private XStream xstream;

	@SuppressWarnings("deprecation")
	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	@Override
	public String getStringId()
	{
		return OAUTHTOKENS;
	}

	@Override
	protected NameValue getStandardTask()
	{
		return new BundleNameValue(PREFIX + "converter.name", getStringId());
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
			throws IOException
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(PREFIX + "converter.exportmsg");
		callback.setMessageCallback(message);
		final List<OAuthToken> tokens = tokenDao.enumerateAll();
		message.setTotal(tokens.size());

		final SubTemporaryFile exportFolder = new SubTemporaryFile(staging, OAUTHTOKENS);
		xmlHelper.writeExportFormatXmlFile(exportFolder, true);
		for( OAuthToken token : tokens )
		{
			final long id = token.getId();
			final BucketFile bucketFolder = new BucketFile(exportFolder, id);
			xmlHelper.writeXmlFile(bucketFolder, id + ".xml", token, getXStream());

			message.incrementCurrent();
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
			throws IOException
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(PREFIX + "converter.importmsg");
		callback.setMessageCallback(message);
		final SubTemporaryFile importFolder = new SubTemporaryFile(staging, OAUTHTOKENS);
		final List<String> tokenFiles = xmlHelper.getXmlFileList(importFolder);
		message.setTotal(tokenFiles.size());

		for( String tokenFile : tokenFiles )
		{
			final OAuthToken token = xmlHelper.readXmlFile(importFolder, tokenFile, getXStream());
			token.setInstitution(institution);

			tokenDao.save(token);
			tokenDao.flush();
			tokenDao.clear();
			message.incrementCurrent();
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(PREFIX + "converter.deletemsg");
		callback.setMessageCallback(message);
		final List<OAuthToken> tokens = tokenDao.enumerateAll();
		message.setTotal(tokens.size());

		for( final OAuthToken token : tokens )
		{
			tokenDao.delete(token);
			tokenDao.flush();
			tokenDao.clear();
			message.incrementCurrent();
		}
	}

	private synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = new XmlServiceImpl.ExtXStream(getClass().getClassLoader()) {
				@Override
				protected MapperWrapper wrapMapper(MapperWrapper next) {
					return new HibernateMapper(next);
				}
			};
			xstream.registerConverter(new HibernateProxyConverter());
			xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
			xstream.alias("com.tle.core.oauth.beans.OAuthToken", OAuthToken.class);
			xstream.alias("com.tle.core.oauth.beans.OAuthClient", OAuthClient.class);
			xstream.registerConverter(new ClientXStreamConverter());
		}
		return xstream;
	}

	private class ClientXStreamConverter implements Converter
	{
		@SuppressWarnings("rawtypes")
		@Override
		public boolean canConvert(Class clazz)
		{
			return OAuthClient.class == clazz;
		}

		@Override
		public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
		{
			OAuthClient client = (OAuthClient) obj;
			String uuid = client.getUuid();
			writer.addAttribute("uuid", uuid);
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
		{
			String uuidFromStream = reader.getAttribute("uuid");
			return oauthService.getByUuid(uuidFromStream);
		}
	}
}
