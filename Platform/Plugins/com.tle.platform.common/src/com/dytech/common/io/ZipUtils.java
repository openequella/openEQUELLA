/*
 * Copyright 2019 Apereo
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

package com.dytech.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class ZipUtils
{
	public ZipUtils()
	{
		super();
	}

	public static ZipFilter createZipFilter(String regex)
	{
		return new ZipFilter(regex);
	}

	public static void extract(File zip, File destination) throws IOException
	{
		ZipInputStream s = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
		extract(s, createZipFilter(".*"), destination); //$NON-NLS-1$
	}

	public static void extract(File zip, ZipFilter filter, File destination) throws IOException
	{
		extract(new ZipInputStream(new BufferedInputStream(new FileInputStream(zip))), filter, destination);
	}

	public static void extract(ZipInputStream zip, File destination) throws IOException
	{
		extract(zip, createZipFilter(".*"), destination); //$NON-NLS-1$
	}

	public static void extract(ZipInputStream zip, ZipFilter filter, File destination) throws IOException
	{
		destination.mkdirs();
		try
		{
			ZipEntry entry = zip.getNextEntry();
			while( entry != null )
			{
				if( filter.accept(entry.getName()) )
				{
					String target = destination.getPath() + File.separator + entry.getName();
					if( entry.isDirectory() )
					{
						(new File(target)).mkdirs();
					}
					else
					{
						File tfile = new File(target);
						tfile.getParentFile().mkdirs();
						Files.asByteSink(tfile).writeFrom(zip);
					}
				}

				entry = zip.getNextEntry();
			}
		}
		finally
		{
			Closeables.close(zip, false);
		}
	}

	public static void add(ZipOutputStream zipFile, String directory, File inputFile) throws IOException
	{
		try( BufferedInputStream in = new BufferedInputStream(new FileInputStream(inputFile)) )
		{
			zipFile.putNextEntry(new ZipEntry(directory + inputFile.getName()));
			ByteStreams.copy(in, zipFile);
			zipFile.closeEntry();
		}
	}

	public static void addDirectoryTree(ZipOutputStream zipFile, String directory, File inputFile) throws IOException
	{
		if( inputFile.isDirectory() )
		{
			File files[] = inputFile.listFiles();
			for( int i = 0; i < files.length; i++ )
			{
				String dir = directory;
				if( files[i].isDirectory() )
				{
					dir = dir + files[i].getName() + File.separator;
				}
				addDirectoryTree(zipFile, dir, files[i]);
			}

		}
		else
		{
			add(zipFile, directory, inputFile);
		}
	}
}
