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

package com.tle.core.mimetypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.tle.beans.Institution;
import com.tle.beans.mime.MimeEntry;

public final class MimeFileUtils
{
	@SuppressWarnings("nls")
	public static List<MimeEntry> readLegacyFile(InputStream inp, Institution institution)
	{
		List<MimeEntry> entries = new ArrayList<MimeEntry>();
		try( BufferedReader reader = new BufferedReader(new InputStreamReader(inp, "UTF-8")) )
		{
			for( String line = reader.readLine(); line != null; line = reader.readLine() )
			{
				String parts[] = line.split("\\s+");
				if( parts.length <= 0 || parts[0].startsWith("#") || parts[0].length() == 0 )
				{
					continue;
				}
				MimeEntry mimeEntry = new MimeEntry();
				mimeEntry.setInstitution(institution);
				List<String> exts = new ArrayList<String>();
				for( int i = 1; i < parts.length; i++ )
				{
					String ext = parts[i];
					exts.add(ext);
				}
				mimeEntry.setExtensions(exts);
				mimeEntry.setType(parts[0]);
				entries.add(mimeEntry);
			}
		}
		catch( IOException ioe )
		{
			throw new RuntimeException(ioe);
		}
		return entries;
	}

	private MimeFileUtils()
	{
		throw new Error();
	}
}
