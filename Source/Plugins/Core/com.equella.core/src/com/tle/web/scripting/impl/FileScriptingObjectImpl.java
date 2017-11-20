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

package com.tle.web.scripting.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import com.dytech.edge.common.FileInfo;
import com.google.common.io.CharStreams;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.scripting.objects.FileScriptObject;
import com.tle.common.scripting.types.BinaryDataScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.scripting.service.ErrorThrowingFileHandle;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.impl.ItemScriptWrapper.ItemScriptTypeImpl;
import com.tle.web.scripting.impl.UtilsScriptWrapper.BinaryDataScriptTypeImpl;

@SuppressWarnings("nls")
public class FileScriptingObjectImpl extends AbstractScriptWrapper implements FileScriptObject
{
	private static final long serialVersionUID = 1L;

	private final FileSystemService fileSystemService;
	private final ItemFileService itemFileService;
	private final FileHandle handle;

	public FileScriptingObjectImpl(FileSystemService fileSystemService, ItemFileService itemFileService,
		FileHandle handle)
	{
		this.fileSystemService = fileSystemService;
		this.itemFileService = itemFileService;
		this.handle = handle;

		Check.checkNotNull(handle);
	}

	@Override
	public boolean isAvailable()
	{
		return (handle != null && !(handle instanceof ErrorThrowingFileHandle));
	}

	@Override
	public FileHandleScriptType copy(String src, String dest)
	{
		fileSystemService.copy(handle, src, dest);
		return new FileHandleScriptTypeImpl(dest, handle);
	}

	@Override
	public void deleteFile(String filename)
	{
		fileSystemService.removeFile(handle, filename);
	}

	@Override
	public boolean exists(String filename)
	{
		return fileSystemService.fileExists(handle, filename);
	}

	@Override
	public long fileLength(String filename)
	{
		try
		{
			return fileSystemService.fileLength(handle, filename);
		}
		catch( FileNotFoundException e )
		{
			return -1;
		}
	}

	@Override
	public long lastModified(String filename)
	{
		return fileSystemService.lastModified(handle, filename);
	}

	@Override
	public List<String> list(String folder, String pattern)
	{
		return fileSystemService.grepIncludingDirs(handle, folder, pattern == null ? "*" : pattern);
	}

	@Override
	public List<String> listFiles(String folder, String pattern)
	{
		return fileSystemService.grep(handle, folder, pattern == null ? "*" : pattern);
	}

	@Override
	public FileHandleScriptType move(String src, String dest)
	{
		fileSystemService.move(handle, src, dest);
		return new FileHandleScriptTypeImpl(dest, handle);
	}

	@Override
	public FileHandleScriptType createFolder(String path)
	{
		fileSystemService.mkdir(handle, path);
		return new FileHandleScriptTypeImpl(path, handle);
	}

	@Override
	public FileHandleScriptType writeTextFile(String filename, String text)
	{
		try( Reader rd = new StringReader(text) )
		{
			final FileInfo finfo = fileSystemService.write(handle, filename, rd, false);
			return new FileHandleScriptTypeImpl(finfo.getFilename(), handle);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String readTextFile(String filename)
	{
		try( InputStream in = fileSystemService.read(handle, filename) )
		{
			StringWriter sw = new StringWriter();
			CharStreams.copy(new InputStreamReader(in), sw);
			return sw.toString();
		}
		catch( IOException e )
		{
			return null;
		}
	}

	@Override
	public FileHandleScriptType writeBinaryFile(String filename, BinaryDataScriptType data)
	{
		try( InputStream is = new ByteArrayInputStream(((BinaryDataScriptTypeImpl) data).getData()) )
		{
			final FileInfo finfo = fileSystemService.write(handle, filename, is, false);
			return new FileHandleScriptTypeImpl(finfo.getFilename(), handle);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FileHandleScriptType getFileHandle(String filename)
	{
		return new FileHandleScriptTypeImpl(filename, handle);
	}

	@Override
	public FileHandleScriptType getFileHandle(ItemScriptType item, String filename)
	{
		if( item instanceof ItemScriptTypeImpl )
		{
			return new FileHandleScriptTypeImpl(filename,
				itemFileService.getItemFile(((ItemScriptTypeImpl) item).getItem()), true);
		}
		final ItemId itemId = new ItemId(item.getUuid(), item.getVersion());
		return new FileHandleScriptTypeImpl(filename, itemFileService.getItemFile(itemId, null), true);
	}

	public static class FileHandleScriptTypeImpl implements FileHandleScriptType
	{
		private final String filepath;
		private final FileHandle myHandle;
		private final boolean readOnly;

		protected FileHandleScriptTypeImpl(String filepath, FileHandle myHandle)
		{
			this(filepath, myHandle, false);
		}

		protected FileHandleScriptTypeImpl(String filepath, FileHandle myHandle, boolean readOnly)
		{
			this.filepath = filepath;
			this.myHandle = myHandle;
			this.readOnly = readOnly;
		}

		@Override
		public String getFilepath()
		{
			return filepath;
		}

		@Override
		public String getName()
		{
			return new File(filepath).getName();
		}

		@Override
		public String toString()
		{
			return filepath;
		}

		@Override
		public boolean isReadOnly()
		{
			return readOnly;
		}

		/**
		 * Internal use ONLY. Do NOT use in scripts
		 * 
		 * @return
		 */
		public FileHandle getHandle()
		{
			return myHandle;
		}
	}
}
