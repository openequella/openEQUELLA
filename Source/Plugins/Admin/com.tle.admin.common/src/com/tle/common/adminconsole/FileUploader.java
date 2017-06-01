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
