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

package com.tle.core.mimetypes.institution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.mimetypes.MimeFileUtils;
import com.tle.core.xml.service.XmlService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Bind
@Singleton
public class MimeMigrator extends XmlMigrator
{
	public static final List<String> EQUELLA_TYPES = getEquellaMimeEntries();

	@Inject
	private XmlService xmlService;

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		List<MimeEntry> defaultTypes = getDefaultMimeEntries();
		SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);

		for( MimeEntry mimeEntry : defaultTypes )
		{
			xmlHelper.writeFile(mimeFolder, MimeEntryConverter.getFilenameForEntry(mimeEntry),
				xmlService.serialiseToXml(mimeEntry));
		}
	}

	private static List<String> getEquellaMimeEntries()
	{
		List<MimeEntry> defaultTypes = MimeFileUtils
			.readLegacyFile(MimeMigrator.class.getResourceAsStream("mime.types"), null); //$NON-NLS-1$
		ArrayList<String> equellaTypes = new ArrayList<String>();
		for( MimeEntry mimeEntry : defaultTypes )
		{
			if( mimeEntry.getType().startsWith("equella/") ) //$NON-NLS-1$
			{
				equellaTypes.add(mimeEntry.getType());
			}
		}

		return equellaTypes;
	}

	@SuppressWarnings("nls")
	public static List<MimeEntry> getDefaultMimeEntries()
	{
		List<MimeEntry> defaultTypes = MimeFileUtils
			.readLegacyFile(MimeMigrator.class.getResourceAsStream("mime.types"), null);

		Map<String, JSONObject> typeMap = new HashMap<String, JSONObject>();
		try
		{
			String s = Resources.toString(MimeMigrator.class.getResource("attributes.conf"), Charsets.UTF_8);
			JSONArray jsonArray = JSONArray.fromObject(s);
			for( Object objEntry : jsonArray )
			{
				JSONObject entry = (JSONObject) objEntry;
				for( String type : entry.getString("type").split(",") )
				{
					typeMap.put(type, entry);
				}
			}
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}

		for( MimeEntry mimeEntry : defaultTypes )
		{
			String type = mimeEntry.getType();
			String starType = type.substring(0, type.indexOf('/')) + "/*";
			addAttributes(typeMap, starType, mimeEntry);
			addAttributes(typeMap, type, mimeEntry);
		}
		return defaultTypes;
	}

	@SuppressWarnings("unchecked")
	private static void addAttributes(Map<String, JSONObject> typeMap, String type, MimeEntry mimeEntry)
	{
		Map<String, String> attributes = mimeEntry.getAttributes();
		JSONObject attrs = typeMap.get(type);
		if( attrs != null )
		{
			Set<String> keySet = attrs.keySet();
			for( String key : keySet )
			{
				if( key.equals("description") ) //$NON-NLS-1$
				{
					mimeEntry.setDescription(attrs.getString(key));
				}
				else if( !key.equals("type") ) //$NON-NLS-1$
				{
					attributes.put(key, attrs.getString(key));
				}
			}
		}
	}

}
