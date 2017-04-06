package com.tle.common.adminconsole;

import java.io.IOException;
import java.io.InputStream;

import com.dytech.common.GeneralConstants;

public final class FileUploader
{
	private static final int UPLOAD_CHUNK_SIZE = GeneralConstants.BYTES_PER_MEGABYTE;

	public static void upload(RemoteAdminService service, String staging, String filename, InputStream stream)
		throws IOException
	{
		try
		{
			byte[] bytes = new byte[UPLOAD_CHUNK_SIZE];
			int read = 0;
			boolean append = false;
			while( (read = stream.read(bytes, 0, UPLOAD_CHUNK_SIZE)) > 0 )
			{
				byte[] data = bytes;

				// hack: uploadFile doesn't take length param,
				// which is fine, we don't want to send unused bytes over the
				// wire...
				if( read < UPLOAD_CHUNK_SIZE )
				{
					data = new byte[read];
					System.arraycopy(bytes, 0, data, 0, read);
				}

				service.uploadFile(staging, filename, data, append);
				append = true;
			}
		}
		catch( IOException e )
		{
			try
			{
				// Try to remove grotesque, disfigured, incomplete, aborted
				// creations
				service.removeFile(staging, filename);
			}
			catch( Exception other )
			{
				// Forget it.
			}
			throw e;
		}
	}

	private FileUploader()
	{
		throw new Error();
	}
}
