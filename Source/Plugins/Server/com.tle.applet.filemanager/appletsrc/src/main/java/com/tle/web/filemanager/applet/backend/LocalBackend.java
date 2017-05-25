package com.tle.web.filemanager.applet.backend;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dytech.common.io.FileUtils;
import com.google.common.io.Files;
import com.tle.web.appletcommon.gui.UserCancelledException;
import com.tle.web.filemanager.common.FileInfo;

@SuppressWarnings("nls")
public class LocalBackend extends AbstractRemoteBackendImpl
{
	// FIXME: copied from com.tle.core.entity.services
	public static final Set<String> ILLEGAL_FILENAMES = new HashSet<String>(Arrays.asList("com1", "com2", "com3",
		"com4", "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8",
		"lpt9", "con", "nul", "prn"));

	private static final Logger LOGGER = Logger.getLogger(LocalBackend.class.getName());

	private final String base;
	private final boolean simulateSlowReading;
	private final Set<String> markedAsResources = new HashSet<String>();

	public LocalBackend()
	{
		this(System.getProperty("user.home"), false);
	}

	public LocalBackend(String base, boolean simulateSlowReading)
	{
		this.base = base;
		this.simulateSlowReading = simulateSlowReading;
	}

	@Override
	public List<FileInfo> listFiles(String directory)
	{
		FileFilter filter = new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return !pathname.isHidden();
			}
		};

		List<FileInfo> results = new ArrayList<FileInfo>();
		for( File file : getActualFile(directory).listFiles(filter) )
		{
			FileInfo info = new FileInfo();
			info.setPath(directory);
			info.setName(file.getName());
			info.setDirectory(file.isDirectory());
			info.setSize(file.length());
			info.setLastModified(file.lastModified());
			info.setMarkAsAttachment(markedAsResources.contains(info.getFullPath()));

			results.add(info);
		}
		return results;
	}

	@Override
	public InputStream readFile(String filename)
	{
		try
		{
			InputStream in = new FileInputStream(getActualFile(filename));
			if( simulateSlowReading )
			{
				in = new SlowInputStream(in);
			}
			return new BufferedInputStream(in);
		}
		catch( FileNotFoundException ex )
		{
			LOGGER.log(Level.WARNING, "File could not be found: " + filename, ex);
			return null;
		}
	}

	@Override
	public void writeFile(String filename, long length, InputStream content)
	{
		if( simulateSlowReading )
		{
			content = new SlowInputStream(content);
		}

		try
		{
			Files.asByteSink(getActualFile(filename)).writeFrom(content);
		}
		catch( IOException ex )
		{
			LOGGER.log(Level.WARNING, "Error writing to file: " + filename, ex);
		}
	}

	@Override
	public void delete(FileInfo info)
	{
		File file = getActualFile(info);
		try
		{
			FileUtils.delete(file);
		}
		catch( IOException ex )
		{
			LOGGER.log(Level.WARNING, "File could not be deleted: " + file.getAbsolutePath(), ex);
			throw new RuntimeException("Could not delete file", ex);
		}
	}

	private File getActualFile(FileInfo info)
	{
		return getActualFile(info.getFullPath());
	}

	private File getActualFile(String path)
	{
		return new File(base, path);
	}

	@Override
	public void toggleMarkAsResource(FileInfo info)
	{
		if( info.isMarkAsAttachment() )
		{
			markedAsResources.remove(info.getFullPath());
		}
		else
		{
			markedAsResources.add(info.getFullPath());
		}
	}

	@Override
	public void newFolder(FileInfo fileInfo)
	{
		getActualFile(fileInfo).mkdirs();
	}

	@Override
	public void copy(FileInfo sourceFile, FileInfo destFile)
	{
		throw new UnsupportedOperationException("Too lazy to implement copying, especially recusive folder copying!");
	}

	@Override
	public boolean move(FileInfo sourceFile, FileInfo destinationFile)
	{
		File dest = getActualFile(destinationFile);
		dest.getParentFile().mkdirs();
		return getActualFile(sourceFile).renameTo(dest);
	}

	@Override
	public void extractArchive(FileInfo info)
	{
		throw new UnsupportedOperationException("Too lazy to implement extraction!");
	}

	/* FIXME: Copied from com.tle.core.entity.services */
	public static String encode(String filename)
	{
		// if the filename contains directory folders, we need encode each
		// seperately
		if( filename.contains("\\") || filename.contains("/") ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			String[] parts = filename.split("\\\\|/"); //$NON-NLS-1$
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
				szOut.append(String.format("%02x", intval)); //$NON-NLS-1$
			}
			else
			{
				szOut.append(ch);
			}
		}
		return szOut.toString();
	}

	private static class SlowInputStream extends FilterInputStream
	{
		private final Random rand = new Random();

		public SlowInputStream(InputStream in)
		{
			super(in);
		}

		@Override
		public int read() throws IOException
		{
			goSlow();
			return super.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException
		{
			goSlow();
			return super.read(b, off, len);
		}

		private void goSlow()
		{
			if( Thread.currentThread().isInterrupted() )
			{
				throw new UserCancelledException();
			}

			try
			{
				Thread.sleep(rand.nextInt(90));
			}
			catch( InterruptedException e )
			{
				throw new UserCancelledException();
			}
		}
	}
}
