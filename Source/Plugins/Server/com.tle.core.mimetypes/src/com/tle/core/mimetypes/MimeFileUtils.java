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
