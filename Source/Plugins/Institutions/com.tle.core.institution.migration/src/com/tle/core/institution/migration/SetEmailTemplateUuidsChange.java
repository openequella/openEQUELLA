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

package com.tle.core.institution.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class SetEmailTemplateUuidsChange extends XmlMigrator
{
	public static final String XSLT_ESCALATION = "e4f0e654-6a84-d07d-37e9-a3e65312e79d";
	public static final String XSLT_MODERATION = "32e1cd99-2e4b-7d7f-2ccd-c37fa8193295";
	public static final String XSLT_REVIEW = "5090c6df-26f0-9b70-9713-1ab410fd49bb";
	public static final String XSLT_NOTIFYNEW = "d89d630d-14d0-c31c-f99a-fd1598d01a21";
	public static final String XSLT_BADURLS = "f9930946-776b-2e7d-b30e-bb40e0da2e9c";
	public static final String XSLT_Z3950 = "fc1731cd-18a6-807c-7a59-b2512a00e388";
	public static final String XSLT_EMAILPLAN = "10a0239f-7b8c-32c2-b460-717cba685c6e";
	public static final String XSLT_ITEMSHARE = "afae9bdc-1b0a-74e4-fc21-fbdaa15d7d00";
	public static final String XSLT_BB_NOTIFY = "8ac5e45c-8e72-7ffb-cecc-00f8fb7ae52b";

	private static final Map<String, String> MAPPING = new HashMap<String, String>();

	static
	{
		MAPPING.put("Escalation", XSLT_ESCALATION);
		MAPPING.put("Moderation", XSLT_MODERATION);
		MAPPING.put("Review", XSLT_REVIEW);
		MAPPING.put("Notify About New Items", XSLT_NOTIFYNEW);
		MAPPING.put("Urls", XSLT_BADURLS);
		MAPPING.put("Z3950", XSLT_Z3950);
		MAPPING.put("emailplan", XSLT_EMAILPLAN);
		MAPPING.put("itemshare", XSLT_ITEMSHARE);
		MAPPING.put("BlackboardNotify", XSLT_BB_NOTIFY);
	}

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile handle = new SubTemporaryFile(staging, "emailtemplate");
		final List<String> entries = xmlHelper.getXmlFileList(handle);
		for( String entry : entries )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(handle, entry);
			String newUuid = MAPPING.get(xml.getNode("name"));
			if( !Check.isEmpty(newUuid) )
			{
				String oldUuid = xml.getNode("uuid");
				if( !newUuid.equals(oldUuid) )
				{
					xml.setNode("uuid", newUuid);
					xmlHelper.writeFromPropBagEx(handle, entry, xml);

					// Rename the folder with the XSLTs and stuff
					fileSystemService.rename(handle, oldUuid, newUuid);
				}
			}
		}
	}
}
