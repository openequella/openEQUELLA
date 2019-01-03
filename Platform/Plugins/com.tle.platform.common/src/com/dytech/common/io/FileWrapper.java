/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dytech.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import com.google.common.io.Files;

@Deprecated
@SuppressWarnings("nls")
public class FileWrapper extends File
{
	public FileWrapper(File parent, String child)
	{
		super(parent, child);
	}

	public FileWrapper(String parent, String child)
	{
		super(parent, child);
	}

	public FileWrapper(String pathname)
	{
		super(pathname);
	}

	public FileWrapper(URI uri)
	{
		super(uri);
	}

	public void recurse(FileVisitor file, FileVisitor directory)
	{
		if( isDirectory() )
		{
			if( directory != null )
			{
				directory.visit(this);
			}
			FileWrapper files[] = listFileWrappers();
			for( int i = 0; i < files.length; i++ )
			{
				files[i].recurse(file, directory);
			}
		}
		else if( file != null )
		{
			file.visit(this);
		}
	}

	public void recursiveDelete() throws IOException
	{
		if( isDirectory() )
		{
			FileWrapper files[] = listFileWrappers();
			for( int i = 0; i < files.length; i++ )
			{
				files[i].recursiveDelete();
			}
		}
		if( !delete() )
		{
			throw new IOException(toString());
		}
		else
		{
			return;
		}
	}

	public FileWrapper[] listFileWrappers()
	{
		File files[] = listFiles();
		FileWrapper wrappers[] = new FileWrapper[files.length];
		for( int i = 0; i < files.length; i++ )
		{
			wrappers[i] = new FileWrapper(files[i].toString());
		}
		return wrappers;
	}

	public String getVolume() throws IOException
	{
		File roots[] = File.listRoots();
		String path = getCanonicalPath();
		for( int i = 0; i < roots.length; i++ )
		{
			String root = roots[i].toString();
			if( path.startsWith(root) )
			{
				return root;
			}
		}
		throw new IOException("Could not retrieve volume!");
	}

	public void move(FileWrapper destination, boolean overwrite) throws IOException
	{
		if( !exists() )
		{
			throw new FileNotFoundException("Could not move: file does not exist: " + toString());
		}
		if( destination.exists() )
		{
			if( !overwrite )
			{
				throw new IOException("Could not move: destination exists: " + destination.toString());
			}
			if( isFile() && !destination.isFile() )
			{
				throw new IOException("Could not move: destination is not a file: " + destination.toString());
			}
			if( isDirectory() && !destination.isDirectory() )
			{
				throw new IOException("Could not move: destination is not a directory: " + destination.toString());
			}
			try
			{
				destination.recursiveDelete();
			}
			catch( IOException e )
			{
				throw new IOException("Could not move: destination(s) could not be removed: " + e.getMessage());
			}
		}
		boolean madeDirs = (new FileWrapper(destination.getParent())).mkdirs();
		if( !(madeDirs || destination.getParentFile().exists()) )
		{
			throw new IOException("Could not create/confirm directory " + destination.getParentFile().getAbsolutePath());
		}

		String sourceVolume = getVolume();
		String destinationVolume = destination.getVolume();
		boolean sameVolume = sourceVolume.equals(destinationVolume);
		if( !sameVolume || !renameTo(destination) )
		{
			copy(destination, overwrite);
			recursiveDelete();
		}
	}

	public void copy(FileWrapper target, boolean overwrite) throws IOException
	{
		if( !exists() )
		{
			throw new FileNotFoundException("Source does not exist: " + toString());
		}
		if( isFile() )
		{
			copyFile(target, overwrite);
		}
		else if( isDirectory() )
		{
			copyDirectory(target, overwrite);
		}
		else
		{
			throw new IOException("Source is not a file or directory");
		}
	}

	protected void copyFile(File target, boolean overwrite) throws IOException
	{
		if( !canRead() )
		{
			throw new IOException("Can not read source file: " + toString());
		}
		if( target.exists() )
		{
			if( !target.isFile() )
			{
				throw new IllegalArgumentException("Existing destination is not a file: " + toString());
			}
			if( !overwrite )
			{
				throw new IOException("Will not overwrite destination: " + target.toString());
			}
			if( !target.delete() )
			{
				throw new IOException("Can not remove existing destination: " + target.toString());
			}
		}
		else
		{
			File parent = new File(target.getParent());
			boolean madeDirs = parent.mkdirs();
			if( !(madeDirs || parent.exists()) )
			{
				throw new IOException("Could not create parent directories: " + target.toString());
			}
			if( !parent.canWrite() )
			{
				throw new IOException("Can not write to destination directory: " + target.toString());
			}
		}

		Files.copy(this, target);
	}

	protected void copyDirectory(FileWrapper target, boolean overwrite) throws IOException
	{
		if( !canRead() )
		{
			throw new IOException("Can not read source directory: " + toString());
		}
		if( target.exists() )
		{
			if( !target.isDirectory() )
			{
				throw new IllegalArgumentException("Existing destination is not a directory: " + toString());
			}
			if( !overwrite )
			{
				throw new IOException("Will not overwrite destination: " + target.toString());
			}
		}
		boolean madeDirs = target.mkdirs();
		if( !(madeDirs || target.exists()) )
		{
			throw new IOException("Could not create/confirm directory " + target.getAbsolutePath());
		}

		FileWrapper files[] = listFileWrappers();
		for( int i = 0; i < files.length; i++ )
		{
			String from = files[i].getPath();
			String to = target.getPath() + from.substring(from.lastIndexOf(separator));
			files[i].copy(new FileWrapper(to), overwrite);
		}
	}
}
