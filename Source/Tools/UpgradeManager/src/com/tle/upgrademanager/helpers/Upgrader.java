package com.tle.upgrademanager.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.dytech.common.io.FileUtils;
import com.dytech.devlib.PropBagEx;
import com.google.common.io.ByteStreams;
import com.tle.upgrademanager.ManagerConfig;

@SuppressWarnings("nls")
public class Upgrader
{
	private final AjaxState ajaxState;
	private final ManagerConfig config;
	private final String ajaxId;

	public Upgrader(String ajaxId, AjaxState ajaxState, ManagerConfig config)
	{
		this.ajaxState = ajaxState;
		this.config = config;

		this.ajaxId = ajaxId;
	}

	public File downloadUpgrade(String oldFile, String newFile) throws Exception
	{
		ajaxState.addHeading(ajaxId, "Preparing download...");
		File vdir = config.getUpdatesDir();
		if( !vdir.exists() )
		{
			vdir.mkdirs();
		}

		URL url = new Version(config).getUrl("get", oldFile, newFile); //$NON-NLS-1$
		URLConnection conn = url.openConnection();

		File temp = new File(vdir, "upgrade.zip"); //$NON-NLS-1$

		final int total = conn.getContentLength();
		class Filter extends FilterInputStream
		{
			private int batch = 0;
			private int soFar = 0;

			public Filter(InputStream in)
			{
				super(in);
			}

			@Override
			public int read() throws IOException
			{
				int v = super.read();
				if( v >= 0 )
				{
					addSoFar(1);
				}
				return v;
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException
			{
				int v = super.read(b, off, len);
				addSoFar(v);
				return v;
			}

			private void addSoFar(int more)
			{
				soFar += more;
				if( soFar >= (total / 5) )
				{
					batch++;
					soFar = 0;
					ajaxState.addBasic(ajaxId, (batch * 20) + "%");
				}
			}
		}

		try( InputStream in = new Filter(new BufferedInputStream(conn.getInputStream()));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(temp)) )
		{
			ajaxState.start(ajaxId, "Downloading...");
			ByteStreams.copy(in, out);
			ajaxState.addBasic(ajaxId, "Download Complete");
			ajaxState.addHeading(ajaxId, "Processing Update");
		}
		catch( Exception ex )
		{
			ajaxState.addError(ajaxId, "Error downloading upgrade");
			ex.printStackTrace();
		}

		return processUpgrade(vdir, temp);

	}

	public File processUpgrade(File baseDir, File upgradeFile) throws Exception
	{
		try( ZipFile upgrade = new ZipFile(upgradeFile) )
		{
			PropBagEx manifest = getManifest(upgrade);
			String oldFileName = manifest.getNode("@old-file"); //$NON-NLS-1$
			String newFileName = manifest.getNode("@new-file"); //$NON-NLS-1$

			File newFile = new File(baseDir, newFileName);

			try( ZipFile oldFile = new ZipFile(new File(baseDir, oldFileName));
				OutputStream newOut = new BufferedOutputStream(new FileOutputStream(newFile)) )
			{
				dealWith(manifest.iterator("step"), upgrade, oldFile, newOut); //$NON-NLS-1$

				return newFile;
			}
			finally
			{
				ajaxState.addHeading(ajaxId, "Download complete");
				ajaxState.finish(ajaxId, "Click here to continue", "/pages/");
			}
		}
		finally
		{
			FileUtils.delete(upgradeFile);
		}
	}

	private PropBagEx getManifest(ZipFile upgrade) throws Exception
	{
		try( InputStream in = upgrade.getInputStream(upgrade.getEntry("manifest.xml")) )
		{
			return new PropBagEx(in);
		}
	}

	private void dealWith(Iterator<PropBagEx> i, ZipFile upgrade, ZipFile oldFile, OutputStream out) throws IOException
	{
		Set<String> ignoreEntries = new HashSet<String>();
		ZipOutputStream zout = new ZipOutputStream(out);

		boolean done = false;
		while( i.hasNext() && !done )
		{
			PropBagEx step = i.next();
			String type = step.getNode("@type"); //$NON-NLS-1$
			if( "ascend".equals(type) ) //$NON-NLS-1$
			{
				done = true;
			}
			else if( "add".equals(type) || "update".equals(type) ) //$NON-NLS-1$ //$NON-NLS-2$
			{
				String fileName = step.getNode("@file"); //$NON-NLS-1$
				zout.putNextEntry(new ZipEntry(fileName));
				ByteStreams.copy(upgrade.getInputStream(upgrade.getEntry(step.getNode("@data"))), zout); //$NON-NLS-1$
				zout.closeEntry();

				ignoreEntries.add(step.getNode("@file")); //$NON-NLS-1$
			}
			else if( "delete".equals(type) ) //$NON-NLS-1$
			{
				String fileName = step.getNode("@file"); //$NON-NLS-1$
				ignoreEntries.add(fileName);
			}
			else if( "descend".equals(type) ) //$NON-NLS-1$
			{
				String fileName = step.getNode("@file"); //$NON-NLS-1$

				ajaxState.addBasic(ajaxId, "Processing " + fileName);
				File temp = File.createTempFile("tlezipper", "zip"); //$NON-NLS-1$ //$NON-NLS-2$

				OutputStream tempOut = new BufferedOutputStream(new FileOutputStream(temp));
				ByteStreams.copy(oldFile.getInputStream(oldFile.getEntry(fileName)), tempOut);
				tempOut.close();

				ZipFile newOldFile = new ZipFile(temp);

				zout.putNextEntry(new ZipEntry(step.getNode("@file"))); //$NON-NLS-1$
				dealWith(i, upgrade, newOldFile, zout);
				zout.closeEntry();

				temp.delete();

				ignoreEntries.add(step.getNode("@file")); //$NON-NLS-1$
			}
		}

		Enumeration<? extends ZipEntry> e = oldFile.entries();
		while( e.hasMoreElements() )
		{
			ZipEntry zentry = e.nextElement();
			String name = zentry.getName();
			if( !ignoreEntries.contains(name) )
			{
				zout.putNextEntry(new ZipEntry(name));
				ByteStreams.copy(oldFile.getInputStream(zentry), zout);
				zout.closeEntry();
				ignoreEntries.add(name);
			}
		}
		zout.finish();
		zout.flush();
	}
}
