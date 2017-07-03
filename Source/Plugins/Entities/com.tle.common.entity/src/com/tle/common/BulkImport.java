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

package com.tle.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.tle.common.util.CsvReader;

public abstract class BulkImport<T>
{
	public abstract T getOld(CsvReader reader) throws IOException;

	public abstract T createNew();

	public abstract void update(CsvReader reader, T t, boolean create) throws Exception;

	public abstract void add(T t) throws Exception;

	public abstract void edit(T t) throws Exception;

	public List<T> bulkImport(byte[] file, boolean override) throws Exception
	{
		CsvReader reader = new CsvReader(new ByteArrayInputStream(file), Charset.forName("UTF-8"));
		return bulkImport(reader, override);
	}

	public List<T> bulkImport(CsvReader reader, boolean override)
	{
		List<T> objects = new ArrayList<T>();
		try
		{
			if( reader.readHeaders() )
			{
				while( reader.readRecord() )
				{
					processOne(reader, override, objects);
				}
			}
			return objects;
		}
		catch( Exception ioe )
		{
			throw new RuntimeException(ioe);
		}
	}

	protected void processOne(CsvReader reader, boolean override, List<T> objects) throws IOException, Exception
	{
		T t = getOld(reader);
		boolean editing = false;
		if( t == null || override )
		{
			if( t == null )
			{
				t = createNew();
			}
			else
			{
				editing = true;
			}
			update(reader, t, !editing);
			if( editing )
			{
				edit(t);
			}
			else
			{
				add(t);
				objects.add(t);
			}
		}
	}
}