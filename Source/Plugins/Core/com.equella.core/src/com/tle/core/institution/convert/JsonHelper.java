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

package com.tle.core.institution.convert;

import com.dytech.common.io.UnicodeReader;
import com.dytech.edge.common.Constants;
import com.google.gson.Gson;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import io.circe.syntax.package$;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.List;

@Bind
@Singleton
public class JsonHelper
{
	private static final Logger LOGGER = Logger.getLogger(JsonHelper.class);

	private Gson gson = new Gson();

	@Inject
	private FileSystemService fileSystemService;

	public List<String> getFileList(final TemporaryFileHandle folder)
	{
		return fileSystemService.grep(folder, "", "*/*.json");
	}

	@SuppressWarnings("unchecked")
	public <O> O read(final TemporaryFileHandle file, String path, Class<O> type)
	{
		try( Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8) )
		{
			return gson.fromJson(reader, type);
		}
		catch( IOException re )
		{
			LOGGER.error("Error reading: " + file.getAbsolutePath());
			throw new RuntimeException(re);
		}
	}

	public void write(TemporaryFileHandle file, String path, Object obj)
	{
		try( OutputStream outStream = fileSystemService.getOutputStream(file, path, false) )
		{
			gson.toJson(obj, new OutputStreamWriter(outStream, Constants.UTF8));
		}
		catch( IOException ioe )
		{
			throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
		}
	}
}
