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

package com.tle.core.taxonomy.institution.migration;

import java.util.UUID;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TermUuidXmlMigration extends XmlMigrator
{

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		SubTemporaryFile taxonomyFolder = new SubTemporaryFile(staging, "taxonomy2");

		// Path: taxonomy2/*bucket*/*taxonomy-uuid*/terms/*#(directory)*/#.xml
		for( String entry : fileSystemService.grep(taxonomyFolder, "", "*/*/terms/*/*.xml") )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(taxonomyFolder, entry);
			if( !xml.nodeExists("uuid") )
			{
				xml.createNode("uuid", UUID.randomUUID().toString());
				xmlHelper.writeFromPropBagEx(taxonomyFolder, entry, xml);
			}

		}
	}

}
