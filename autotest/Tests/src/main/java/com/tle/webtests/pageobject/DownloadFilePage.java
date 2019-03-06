package com.tle.webtests.pageobject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import com.google.common.io.Closeables;
import org.apache.commons.codec.digest.DigestUtils;
import org.openqa.selenium.NotFoundException;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;

public class DownloadFilePage extends AbstractPage<DownloadFilePage>
{
	private final File file;
	private final String md5;
	private final long minSize;
	private static File DownDir = null;

	public static synchronized File getDownDir() {
		if (DownDir == null) {
			try {
				DownDir = Files.createTempDirectory("eqdown").toFile();
			} catch (IOException e) {
				throw new Error(e);
			}
		}
		return DownDir;
	}

	public DownloadFilePage(PageContext context, File file, String md5, long minSize)
	{
		super(context, null, 120);
		this.file = file;
		this.md5 = md5;
		this.minSize = minSize;
	}

	public DownloadFilePage(PageContext context, String file, long minSize)
	{
		this(context, new File(getDownDir(), file), "", minSize);
	}

	public DownloadFilePage(PageContext context, String file)
	{
		this(context, new File(getDownDir(), file), "", 0);
	}

	public DownloadFilePage(PageContext context, URL url) throws IOException
	{
		this(context, url, "");
	}

	public DownloadFilePage(PageContext context, URL url, String md5) throws IOException
	{
		this(context, new File(getDownDir(), url.getFile()), md5, 0);
		if( !fileIsDownloaded() )
		{
			com.google.common.io.Resources.copy(url, Files.newOutputStream(file.toPath()));
		}
	}

	public DownloadFilePage(PageContext context, String file, String md5)
	{
		this(context, new File(getDownDir(), file), md5, 0);
	}

	@Override
	public void checkLoaded()
	{
		if( !file.exists() )
		{
			throw new NotFoundException("File does not exist");
		}
		if( !Check.isEmpty(md5) )
		{
			FileInputStream inputStream = null;
			try
			{
				inputStream = new FileInputStream(file);

				String md5Calculated = DigestUtils.md5Hex(inputStream);
				if( !md5Calculated.equalsIgnoreCase(md5) )
				{
					throw new NotFoundException("File not downloaded yet");
				}
			}
			catch( IOException e )
			{
				throw new NotFoundException("File not downloaded yet");
			}
			finally
			{
				Closeables.closeQuietly(inputStream);
			}

		}
		if( minSize > 0 )
		{
			long length = file.length();
			if( length < minSize )
			{
				throw new NotFoundException("File not downloaded yet");
			}
		}
	}

	public boolean fileIsDownloaded()
	{
		if( !file.exists() )
		{
			return false;
		}
		if( !Check.isEmpty(md5) )
		{
			FileInputStream inputStream = null;
			try
			{
				inputStream = new FileInputStream(file);

				String md5Calculated = DigestUtils.md5Hex(inputStream);
				if( !md5Calculated.equalsIgnoreCase(md5) )
				{
					return false;
				}
			}
			catch( IOException e )
			{
				return false;
			}
			finally
			{
				Closeables.closeQuietly(inputStream);
			}

		}
		return true;
	}

	public File getFile()
	{
		return file;
	}

	public boolean deleteFile()
	{
		try {
			return Files.deleteIfExists(file.toPath());
		} catch (IOException e) {
			// quiet
		}
		return false;
	}
}
