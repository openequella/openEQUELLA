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

package com.tle.core.util.ims.beans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class IMSPackBase
{
	public IMSPackBase()
	{
		super();
	}

	public void unpackImsPackage(InputStream inStream) throws IOException
	{
		ZipInputStream zipInputStream = null;
		try
		{
			zipInputStream = new ZipInputStream(inStream);
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while( zipEntry != null )
			{
				final int size = (int) zipEntry.getSize();
				ByteArrayOutputStream decompressedFile = new ByteArrayOutputStream(size);

				byte[] rgb = new byte[4096];
				int n = 0;

				while( (n = zipInputStream.read(rgb)) > -1 )
				{
					decompressedFile.write(rgb, 0, n);
				}

				if( decompressedFile.size() > 0 )
				{
					imsItemOutput(zipEntry.getName(), decompressedFile);
				}

				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
		finally
		{
			if( zipInputStream != null )
			{
				zipInputStream.close();
			}
		}
	}

	protected void imsItemOutput(String szName, ByteArrayOutputStream decompressedFile)
	{
		System.out.println("Filename = " + szName); //$NON-NLS-1$
		System.out.println("Data = " + decompressedFile.toString()); //$NON-NLS-1$
	}
}