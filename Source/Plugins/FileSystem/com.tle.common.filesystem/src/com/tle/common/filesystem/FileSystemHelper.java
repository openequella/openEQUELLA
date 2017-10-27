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

package com.tle.common.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.GeneralConstants;
import com.dytech.common.io.FileUtils;
import com.dytech.devlib.Md5;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/*
 * Provides methods to modifying the file system. Note: Don't use the
 * BufferedInput/OuputStream classes. Just use FileInput/Output stream with the
 * block read operations. Preferably read DEFAULT_BUFFER_SIZE bytes at time. It
 * will be many times faster!
 */
@SuppressWarnings("nls")
@NonNullByDefault
public final class FileSystemHelper
{
	private static final Log LOGGER = LogFactory.getLog(FileSystemHelper.class);
	private static final int DEFAULT_BUFFER_SIZE = 128 * GeneralConstants.BYTES_PER_KILOBYTE;

	public static final Set<String> ILLEGAL_FILENAMES = new HashSet<String>(
			Arrays.asList("com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3",
					"lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "con", "nul", "prn"));

	private FileSystemHelper()
	{
		throw new IllegalAccessError("Do not instantiate");
	}

	public static void createFile(File file, @Nullable byte[] data) throws IOException
	{
		boolean madeDirs = file.getParentFile().mkdirs();
		if( madeDirs || file.getParentFile().exists() )
		{
			try( OutputStream out = new FileOutputStream(file) )
			{
				if( data != null )
				{
					out.write(data);
				}
			}
		}
		else
		{
			throw new IOException("Could not create directory " + file.getParent());
		}
	}

	public static long recursiveFileLength(File infile) throws FileNotFoundException
	{
		if( infile.isDirectory() )
		{
			long l = 0;
			File[] files = infile.listFiles();
			if( files != null )
			{
				for( File f : files )
				{
					l += recursiveFileLength(f);
				}
			}
			return l;
		}
		return infile.length();
	}

	public static long mostRecentModification(File infile)
	{
		long mod = 0;
		if( infile.exists() )
		{
			if( infile.isDirectory() )
			{
				for( File f : infile.listFiles() )
				{
					mod = Math.max(mod, mostRecentModification(f));
				}
			}
			else
			{
				mod = infile.lastModified();
			}
		}
		return mod;
	}

	public static boolean exists(File file)
	{
		return file.exists();
	}

	public static boolean renameOnly(File from, File to)
	{
		return from.renameTo(to);
	}

	public static boolean rename(File from, File to, boolean keepGoingOnError)
	{
		try
		{
			boolean madeDirs = to.getParentFile().mkdirs();
			if( !(madeDirs || to.getParentFile().exists()) )
			{
				throw new IOException("Could not create/confirm directory " + to.getParentFile().getAbsolutePath());
			}
			if( from.renameTo(to) )
			{
				return true;
			}

			LOGGER.info("Couldn't rename, copying then deleting instead");
			copy(from, to, false, keepGoingOnError);
			return FileUtils.delete(from.toPath());
		}
		catch( IOException ioe )
		{
			LOGGER.error("Error renaming file", ioe);
			return false;
		}
	}

	public static FileEntry[] listDir(File file, FileFilter filter)
	{
		if( !file.exists() )
		{
			return new FileEntry[0];
		}

		File[] files = file.listFiles(filter);
		if( files == null )
		{
			LOGGER.info("Null file list for:" + file.getAbsolutePath());
			return new FileEntry[0];
		}

		FileEntry[] entries = new FileEntry[files.length];
		for( int i = 0; i < files.length; i++ )
		{
			FileEntry entry = new FileEntry(files[i]);
			entry.setName(decode(files[i].getName()));
			entries[i] = entry;
		}

		return entries;
	}

	@Nullable
	public static File[] listDirFiles(File file, FileFilter filter)
	{
		return file.listFiles(filter);
	}

	public static long copy(File from, File to) throws IOException
	{
		return copy(from, to, false, false);
	}

	public static long copy(File from, File to, boolean ignoreInternal, boolean keepGoingOnError) throws IOException
	{
		boolean madeDirs = to.getParentFile().mkdirs();
		if( !(madeDirs || to.getParentFile().exists()) )
		{
			throw new IOException("Could not create/confirm directory " + to.getParentFile().getAbsolutePath());
		}
		return copyRec(from, to, new byte[DEFAULT_BUFFER_SIZE], ignoreInternal, keepGoingOnError);
	}

	/**
	 * @param from
	 * @param to
	 * @param buf
	 * @param ignore
	 * @param bytesCopied Number of bytes copied so far (out param)
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return bytes copied TODO: this is probably an incorrect count...
	 */
	private static long copyRec(File from, File to, byte[] buf, boolean ignore, boolean keepGoingOnError)
			throws IOException
	{
		if( ignore && from.isDirectory() && from.getName().startsWith("_") )
		{
			return 0;
		}

		long copied = 0;

		if( from.isDirectory() )
		{
			boolean madeDir = to.mkdir();
			if( !(madeDir || to.exists()) )
			{
				if( !keepGoingOnError )
				{
					throw new IOException("Could not create/confirm directory " + to.getAbsolutePath());
				}
				else
				{
					LOGGER.error("Could not create/confirm directory " + to.getAbsolutePath());
				}
			}
			File[] files = from.listFiles();
			for( File element : files )
			{
				copied += copyRec(element, new File(to, element.getName()), buf, ignore, keepGoingOnError);
			}
		}
		else
		{
			InputStream in = null;
			OutputStream out = null;

			try
			{
				in = new FileInputStream(from);
				out = new FileOutputStream(to);

				int length = in.read(buf);
				while( length > 0 )
				{
					out.write(buf, 0, length);
					copied += length;
					length = in.read(buf);
				}
			}
			catch( IOException error )
			{
				if( !keepGoingOnError )
				{
					throw error;
				}
				else
				{
					LOGGER.error("Error copying file", error);
				}
			}
			finally
			{
				IOException closeError = null;
				try
				{
					if( out != null )
					{
						out.close();
					}
				}
				catch( IOException e )
				{
					closeError = e;
				}
				try
				{
					if( in != null )
					{
						in.close();
					}
				}
				catch( IOException e )
				{
					if( closeError == null )
					{
						closeError = e;
					}
				}
				if( closeError != null && !keepGoingOnError )
				{
					throw closeError;
				}
			}
		}
		return copied;
	}

	private static void md5recurse(File file, byte[] filebytes, MessageDigest md5) throws IOException
	{
		if( file.isDirectory() )
		{
			File[] files = file.listFiles();
			List<File> results = new ArrayList<File>();
			for( File element : files )
			{
				results.add(element);
			}
			Collections.sort(results);

			for( File f : results )
			{
				md5recurse(f, filebytes, md5);
			}
		}
		else if( file.exists() )
		{
			LOGGER.info("MD5ing:" + file.getAbsolutePath());
			InputStream inp = new FileInputStream(file);
			checkSumFromStream(filebytes, md5, inp);
		}
	}

	public static void checkSumFromStream(byte[] filebytes, MessageDigest md5, InputStream inp) throws IOException
	{
		try( InputStream in = inp )
		{
			int read = 0;
			while( (read = in.read(filebytes)) >= 0 )
			{
				md5.update(filebytes, 0, read);
			}
		}
	}

	public static String md5recurse(File file, byte[] buffer) throws IOException
	{

		try
		{
			MessageDigest md5 = MessageDigest.getInstance("md5");
			md5recurse(file, buffer, md5);
			return Md5.stringify(md5.digest());
		}
		catch( NoSuchAlgorithmException e )
		{
			return null;
		}
	}

	/**
	 * @param file
	 * @return
	 */
	public static long lastModified(File file)
	{
		return file.lastModified();
	}

	/**
	 * @param file
	 * @return
	 */
	public static void mkdir(File file)
	{
		if( !file.mkdirs() && !file.exists() )
		{
			throw new FileSystemException("Error creating dir: " + file);
		}
	}

	/**
	 * @param file
	 * @return
	 */
	public static boolean isDir(File file)
	{
		return file.isDirectory();
	}

	public static int countFiles(File file)
	{
		int current = 0;
		if( file.isDirectory() )
		{
			File[] fileList = file.listFiles();
			for( File element : fileList )
			{
				if( element.isDirectory() )
				{
					current += countFiles(element);
				}
				else
				{
					current++;
				}
			}
		}
		return current + 1;
	}

	public static String encode(String filename)
	{
		// if the filename contains directory folders, we need encode each
		// seperately
		if( filename.contains("\\") || filename.contains("/") )
		{
			String[] parts = filename.split("\\\\|/");
			StringBuilder full = new StringBuilder();
			boolean first = true;
			for( String part : parts )
			{
				if( !first )
				{
					full.append('/');
				}
				full = full.append(encode(part));
				first = false;
			}
			return full.toString();
		}

		StringBuilder szOut = new StringBuilder();
		if( filename.isEmpty() )
		{
			return filename;
		}
		int lastIndex = filename.length() - 1;
		char first = filename.charAt(0);
		char last = filename.charAt(lastIndex);
		boolean encodeFirst = ILLEGAL_FILENAMES.contains(filename.toLowerCase()) || first == '.';
		boolean encodeLast = last == '.' || last == ' ';
		for( int i = 0; i < filename.length(); i++ )
		{
			boolean encode = false;
			char ch = filename.charAt(i);
			if( ch < 0x20 || (encodeFirst && i == 0) || (encodeLast && i == lastIndex) )
			{
				encode = true;
			}
			else
			{
				switch( ch )
				{
					case ':':
					case '*':
					case '?':
					case '"':
					case '<':
					case '>':
					case '|':
					case '^':
					case '%':
					case '+':
						encode = true;
						break;

					default:
						break;
				}
			}
			if( encode )
			{
				szOut.append('%');
				int intval = ch;
				szOut.append(String.format("%02x", intval));
			}
			else
			{
				szOut.append(ch);
			}
		}
		return szOut.toString();
	}

	public static String decode(String url)
	{
		try
		{
			return URLDecoder.decode(url, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Transforms the given list in-place.
	 *
	 * @param files
	 * @return
	 */
	public static List<String> decode(List<String> files)
	{
		return Lists.newArrayList(Lists.transform(files, new Function<String, String>()
		{
			@Override
			public String apply(String file)
			{
				return decode(file);
			}
		}));
	}
}
