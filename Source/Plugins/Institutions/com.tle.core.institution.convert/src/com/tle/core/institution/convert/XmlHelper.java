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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.dytech.common.io.UnicodeReader;
import com.dytech.common.text.NumberStringComparator;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter.FormatFile;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class XmlHelper
{
	private static final Logger LOGGER = Logger.getLogger(XmlHelper.class);

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private XmlService xmlService;
	private final XppDriver xppDriver = new XppDriver();

	public XStream createXStream(ClassLoader classLoader)
	{
		return xmlService.createDefault(classLoader);
	}

	public void writeFile(TemporaryFileHandle staging, String filename, String content)
	{
		try( Writer out = new OutputStreamWriter(fileSystemService.getOutputStream(staging, filename, false),
			Constants.UTF8) )
		{
			out.write(content);
		}
		catch( Exception e )
		{
			throw new RuntimeException("Error reading xml from file:" + staging.getAbsolutePath() + filename, e);
		}
	}

	public void writeFromPropBagEx(TemporaryFileHandle staging, String filename, PropBagEx xml)
	{
		writeFile(staging, filename, xml.toString());
	}

	public PropBagEx readToPropBagEx(TemporaryFileHandle staging, String filename)
	{
		try( InputStream stream = fileSystemService.read(staging, filename) )
		{
			return new PropBagEx(stream);
		}
		catch( Exception e )
		{
			throw new RuntimeException("Error reading xml from file:" + staging.getAbsolutePath() + filename, e);
		}
	}

	public void writeXmlFile(TemporaryFileHandle file, String path, Object obj, XStream xstream)
	{
		try( OutputStream outStream = fileSystemService.getOutputStream(file, path, false) )
		{
			xstream.toXML(obj, new OutputStreamWriter(outStream, Constants.UTF8));
		}
		catch( IOException ioe )
		{
			throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
		}
	}

	public void writeXmlFile(TemporaryFileHandle file, String path, Object obj)
	{
		try( OutputStream outStream = fileSystemService.getOutputStream(file, path, false) )
		{
			xmlService.serialiseToWriter(obj, new OutputStreamWriter(outStream, Constants.UTF8));
		}
		catch( IOException ioe )
		{
			throw new RuntimeException("Error writing file " + file.getAbsolutePath(), ioe);
		}
	}

	public <O> O readXmlFile(final TemporaryFileHandle file, String path, final XStream xstream)
	{
		return readXmlFile(file, path, xstream, null, null);
	}

	@SuppressWarnings("unchecked")
	public <O> O readXmlFile(final TemporaryFileHandle file, String path, final XStream xstream, O rootObject,
		DataHolder dataHolder)
	{
		try( Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8) )
		{
			return (O) xstream.unmarshal(xppDriver.createReader(reader), rootObject, dataHolder);
		}
		catch( IOException re )
		{
			LOGGER.error("Error reading: " + file.getAbsolutePath());
			throw new RuntimeException(re);
		}
	}

	@SuppressWarnings("unchecked")
	public <O> O readXmlFile(final TemporaryFileHandle file, String path)
	{
		try( Reader reader = new UnicodeReader(fileSystemService.read(file, path), Constants.UTF8) )
		{
			return (O) xmlService.deserialiseFromXml(getClass().getClassLoader(), reader);
		}
		catch( IOException re )
		{
			LOGGER.error("Error reading: " + file.getAbsolutePath());
			throw new RuntimeException(re);
		}
	}

	public List<String> getXmlFileList(final TemporaryFileHandle folder)
	{
		if( isBucketed(folder) )
		{
			return fileSystemService.grep(folder, "", "*/*.xml");
		}
		return fileSystemService.grep(folder, "", "*.xml");
	}

	/**
	 * Returns the files in order based on their filenames (i.e. the bucket
	 * folder, if any, is ignored)
	 * 
	 * @param folder
	 * @return
	 */
	public List<String> getXmlFileListOrdered(final TemporaryFileHandle folder)
	{
		final List<String> files = getXmlFileList(folder);
		Collections.sort(files, new NumberStringComparator<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public int compare(String term1, String term2)
			{
				File f1 = new File(term1);
				File f2 = new File(term2);
				return super.compare(f1.getName(), f2.getName());
			}
		});
		return files;
	}

	/**
	 * @param folder
	 * @param bucketed
	 */
	public void writeExportFormatXmlFile(final TemporaryFileHandle folder, boolean bucketed)
	{
		FormatFile ff = new FormatFile();
		ff.setBucketed(bucketed);
		writeXmlFile(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE, ff,
			xmlService.createDefault(getClass().getClassLoader()));
	}

	/**
	 * @param folder
	 * @return true if the format file exists and the format is set to bucketed
	 */
	public boolean isBucketed(final TemporaryFileHandle folder)
	{
		if( fileSystemService.fileExists(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE) )
		{
			FormatFile ff = readXmlFile(folder, InstitutionImportExportConstants.EXPORT_FORMAT_FILE);
			return ff.isBucketed();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public <O> O readXmlString(String xml)
	{
		return (O) xmlService.deserialiseFromXml(getClass().getClassLoader(), xml);
	}

	public <O> String writeToXmlString(O obj)
	{
		return xmlService.serialiseToXml(obj);
	}
}
