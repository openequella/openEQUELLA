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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Objects;

import javax.inject.Singleton;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.dytech.edge.common.Constants;
import com.google.common.io.CharStreams;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

/**
 * @author Andrew Gibb
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class ConvertXsltTemplateFileToStringXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
		throws IOException
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");

		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);

			// Chop off the .xml from entry
			String path = entry.substring(0, entry.length() - 4);

			// Load XSLT file. Read to string. Save string. Delete file.
			xsltConversion(xml.getSubtree("slow/itemSummarySections"), idefFolder, path);

			xmlHelper.writeFromPropBagEx(idefFolder, entry, xml);
		}
	}

	private void xsltConversion(PropBagEx xml, TemporaryFileHandle idefFolder, String path) throws IOException
	{
		PropBagIterator iter = xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");

		for( PropBagEx config : iter )
		{
			if( config.getNode("value").equals("xsltSection") )
			{
				// Get XSLT template path
				String filePath = String.format("%s/%s", path, config.getNode("configuration"));

				if( !Objects.equals(filePath, "") )
				{
					// If the file exists read contents to string and close
					// writer otherwise just remove
					if( fileExists(idefFolder, filePath) )
					{
						try( Reader reader = new UnicodeReader(fileSystemService.read(idefFolder, filePath), "UTF-8") )
						{
							StringWriter writer = new StringWriter();
							CharStreams.copy(reader, writer);
							String xsltContent = writer.getBuffer().toString();

							// Remove XSLT Template
							fileSystemService.removeFile(idefFolder, filePath);

							// Update XML
							config.setNode("configuration", xsltContent);
						}
					}
					else
					{
						config.setNode("configuration", Constants.BLANK);
					}
				}
			}
		}
	}
}
