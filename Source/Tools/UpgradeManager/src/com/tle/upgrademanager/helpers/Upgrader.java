/*
 * Copyright 2019 Apereo
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

package com.tle.upgrademanager.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

@SuppressWarnings("nls")
public class Upgrader
{
	private final AjaxState ajaxState;
	private final String ajaxId;

	public Upgrader(String ajaxId, AjaxState ajaxState)
	{
		this.ajaxState = ajaxState;

		this.ajaxId = ajaxId;
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
