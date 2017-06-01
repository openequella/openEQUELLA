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

package com.tle.conversion;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.rmi.server.ExportException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dytech.devlib.PropBagEx;
import com.tle.conversion.exporters.Export;

@SuppressWarnings("nls")
public class Converter
{
	private static final Pattern FILE_EXTENSION = Pattern.compile(".*\\.(\\p{Alnum}+)");

	private final Map<String, Map<String, Export>> mappings = new HashMap<String, Map<String, Export>>();
	private final Map<String, String> synonyms = new HashMap<String, String>();

	public Converter()
	{
		try( InputStream config = getClass().getResourceAsStream("config.xml") )
		{
			PropBagEx xml = new PropBagEx(config);

			readSynonyms(xml);
			readMappings(xml);
		}
		catch( Exception ex )
		{
			// LOGGER.error("Failed reading export.xml", ex);
		}
	}

	/**
	 * Exports the file specified by <code>in</code> to the file specified by
	 * <code>out</code>.
	 * 
	 * @param in The file path for the input file.
	 * @param out The file path to be output to.
	 * @throws IOException If There is an error reading from or writing to
	 *             either of the files.
	 * @throws ExportException If there is an error exporting the file; eg. the
	 *             input format is not supported.
	 */
	public void exportFile(String in, String out) throws IOException
	{
		// LOGGER.info("Convert: " + in + " to " + out);
		getExporter(in, out).exportFile(in, out);
	}

	/**
	 * Reads the synonyms from the configuration xml.
	 * 
	 * @param xml The configuration xml.
	 */
	private void readSynonyms(PropBagEx xml)
	{
		for( PropBagEx synonymXml : xml.iterator("synonyms/synonym") )
		{
			String name = synonymXml.getNode("@for");
			String is = synonymXml.getNode("@is");

			String[] split = is.split(",");
			for( int j = 0; j < split.length; j++ )
			{
				synonyms.put(split[j], name);
			}
		}
	}

	/**
	 * Reads the mappings from the configuration xml.
	 * 
	 * @param xml The configuration xml.
	 */
	private void readMappings(PropBagEx xml)
	{
		for( PropBagEx mapXml : xml.iterator("mappings/map") )
		{
			String klass = mapXml.getNode("@class");

			createMapping(klass);
		}
	}

	/**
	 * Creates a mapping between the given "from" and "to" file types, with the
	 * given exporter.
	 * 
	 * @param from Comma separated values listing the file extensions that can
	 *            be exported from.
	 * @param to Comma separated values listing the file extensions that can be
	 *            exported to.
	 * @param exporter The fully qualified class name of the exporter to be used
	 *            for exporting.
	 */
	public void createMapping(String exporterClass)
	{
		try
		{
			Export exporter = null;
			try
			{
				Class<?>[] args = new Class[]{Converter.class};
				Constructor<?> cons = Class.forName(exporterClass).getConstructor(args);
				exporter = (Export) cons.newInstance(new Object[]{this});
			}
			catch( NoSuchMethodException innerEx )
			{
				exporter = (Export) Class.forName(exporterClass).newInstance();
			}

			Collection<String> inTypes = exporter.getInputTypes();
			Collection<String> outTypes = exporter.getOutputTypes();
			for( String type : inTypes )
			{
				type = type.toLowerCase();
				if( !mappings.containsKey(type) )
				{
					mappings.put(type, new HashMap<String, Export>());
				}

				createMappingTo(type, mappings.get(type), outTypes, exporter);
			}
		}
		catch( Exception ex )
		{
			// LOGGER.error("Could not create mapping for " + exporterClass,
			// ex);
		}
	}

	/**
	 * Helper method for createMapping(String, String, String)
	 * 
	 * @see #createMapping(String, String, String)
	 */
	private void createMappingTo(String fromType, Map<String, Export> from, Collection<String> toTypes, Export exporter)
	{
		for( String totype : toTypes )
		{
			if( !from.containsKey(totype) )
			{
				from.put(totype, exporter);
			}
		}
	}

	/**
	 * Gets the exporter class for the given conversion.
	 * 
	 * @param from The file to convert from.
	 * @param to The file to convert to.
	 * @return The exporter to perform the conversion.
	 */
	public Export getExporter(String from, String to)
	{
		String fromSyn = getSynonym(getExtension(from));
		String toSyn = getSynonym(getExtension(to));

		if( mappings.containsKey(fromSyn) )
		{
			Map<String, Export> table = mappings.get(fromSyn);
			if( table.containsKey(toSyn) )
			{
				return table.get(toSyn);
			}
		}
		return null;
	}

	/**
	 * Retrieves the synonym for a given file type if it exists.
	 * 
	 * @param s The file type to get the synonym of.
	 * @return The synonym, or the original file type if a synonym does not
	 *         exist.
	 */
	public String getSynonym(String s)
	{
		if( synonyms.containsKey(s) )
		{
			return synonyms.get(s);
		}
		else
		{
			return s;
		}
	}

	/**
	 * Takes a filename, path, URL, or file extension and returns the file
	 * extension. This will remove any dot that preceeds the extension, eg,
	 * "blah.html" becomes "html".
	 * 
	 * @param filename a filename, path, URL, or file extension.
	 * @return
	 */
	public String getExtension(String filename)
	{
		Matcher m = FILE_EXTENSION.matcher(filename);
		if( m.matches() )
		{
			return m.group(1).toLowerCase();
		}
		else
		{
			return filename.toLowerCase();
		}
	}
}