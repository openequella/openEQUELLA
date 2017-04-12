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