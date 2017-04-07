package com.tle.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ReferenceLanguagePack extends XmlScanningTask
{
	private static final Pattern KEY_VALUE = Pattern.compile("^[A-Za-z0-9\\._\\-]+ +=(?: .*)?", Pattern.DOTALL);

	private final Map<String, LangFile> bundles = new HashMap<String, LangFile>();

	private File dest;

	private SAXBuilder builder = Helper.createSAXBuilder();

	@Override
	public void execute() throws BuildException
	{
		setup();
		makePack(new File(scanFolder, "Server/com.tle.core.i18n/"));
	}

	@Override
	protected void setup() throws BuildException
	{
		super.setup();
		if( dest == null )
		{
			throw new BuildException("Attribute 'dest' must be defined");
		}
	}

	@Override
	protected String defaultLookFor()
	{
		return PLUGIN_XML;
	}

	public void makePack(File applicationFolder)
	{
		try
		{
			log("Gathering base language files");
			readInAllBaseLanguagePacks(applicationFolder);
			log("Gathering plug-in language files");
			scanFiles(scanFolder);
			log("Producing complete ZIP");
			zipItAllUp();
		}
		catch( Exception e )
		{
			throw new BuildException(e.getMessage(), e);
		}
		finally
		{
			deleteTempFiles();
		}
	}

	private void zipItAllUp()
	{
		dest.delete();

		ZipOutputStream zout = null;
		try
		{
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));

			for( Map.Entry<String, LangFile> bundle : bundles.entrySet() )
			{
				String filename = bundle.getKey();
				LangFile langFile = bundle.getValue();

				if( filename.endsWith(".properties") )
				{
					// Close the writer first to flush out any remaining
					// changes.
					Closeables.closeQuietly(langFile.getWriter());
					copyToZip(langFile.getFile(), zout, filename);
				}
				else
				{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					new XMLOutputter().output(langFile.getXmlDoc(), baos);
					copyToZip(new ByteArrayInputStream(baos.toByteArray()), zout, filename);
				}
			}
		}
		catch( IOException ex )
		{
			throw new BuildException("Error trying to write ZIP file: " + dest, ex);
		}
		finally
		{
			Closeables.closeQuietly(zout);
		}
	}

	private void copyToZip(Object src, ZipOutputStream zout, String filename) throws IOException
	{
		zout.putNextEntry(new ZipEntry(filename));

		InputStream in = null;
		try
		{
			if( src instanceof InputStream )
			{
				in = (InputStream) src;
			}
			else
			{
				in = new BufferedInputStream(new FileInputStream((File) src));
			}
			ByteStreams.copy(in, zout);
		}
		catch( IOException ex )
		{
			throw new BuildException("Error trying to read in file: " + src, ex);
		}
		finally
		{
			Closeables.closeQuietly(in);
		}
	}

	@Override
	protected void doFile(File file)
	{
		try
		{
			Document doc = builder.build(file);

			Element rootElement = doc.getRootElement();
			String pluginId = rootElement.getAttributeValue("id");
			@SuppressWarnings("unchecked")
			List<Element> list = rootElement.getChildren("extension");

			for( Element ext : list )
			{
				if( ext.getAttributeValue("plugin-id").equals("com.tle.common.i18n")
					&& ext.getAttributeValue("point-id").equals("bundle") )
				{
					final String filename = getJPFParamValue(ext, "file");
					final String group = getJPFParamValue(ext, "group")
						+ (filename.endsWith(".xml") ? ".xml" : ".properties");

					final String prepend = pluginId + ".";
					final File lang = new File(new File(file.getParentFile(), "resources"), filename);

					readInLanguageFile(group, lang, prepend);
				}
			}
		}
		catch( BuildException be )
		{
			throw be;
		}
		catch( Exception ex )
		{
			throw new BuildException("Error reading Plugin XML file: " + file, ex);
		}
	}

	private void readInAllBaseLanguagePacks(File baseDir)
	{
		final Pattern p = Pattern.compile("^i18n-(.+\\.properties)$");

		for( File f : new File(baseDir, "src/com/tle/core/services/language/impl").listFiles() )
		{
			final Matcher m = p.matcher(f.getName());
			if( m.matches() )
			{
				readInLanguageFile(m.group(1), f, null);
			}
		}
	}

	private void readInLanguageFile(String group, File languagePack, String prepend)
	{
		if( isVerbose() )
		{
			log("For group " + group + ", reading in " + languagePack);
		}

		LangFile pair = bundles.get(group);
		if( pair == null )
		{
			pair = new LangFile(group);
			bundles.put(group, pair);
		}
		try
		{
			if( languagePack.getName().endsWith(".properties") )
			{
				appendProperties(pair.getWriter(), languagePack, prepend);
			}
			else
			{
				mergeXml(pair.getXmlDoc(), languagePack, prepend);
			}
		}
		catch( Exception ex )
		{
			throw new BuildException("Error copying contents of file: " + languagePack, ex);
		}
	}

	private void mergeXml(Document xmlDoc, File languagePack, String prepend) throws JDOMException, IOException
	{
		Element root = xmlDoc.getRootElement();

		@SuppressWarnings("unchecked")
		List<Element> entries = builder.build(languagePack).getRootElement().getChildren("entry");
		while( !entries.isEmpty() )
		{
			Element e = (Element) entries.get(0).detach();
			e.setAttribute("key", prepend + e.getAttributeValue("key"));
			root.addContent(e);
		}
	}

	private void appendProperties(BufferedWriter appendTo, File languagePack, String prepend) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(languagePack));
		try
		{
			appendTo.write("\n\n# Following included from " + languagePack + "\n");
			if( prepend == null )
			{
				CharStreams.copy(reader, appendTo);
			}
			else
			{
				String line = reader.readLine();
				while( line != null )
				{
					line = line.trim();
					while( line.endsWith("\\") )
					{
						line += "\n" + reader.readLine();
						line = line.trim();
					}

					if( !line.isEmpty() )
					{
						char firstChar = line.charAt(0);
						if( firstChar == '/' )
						{
							// Full property - don't prepend and strip the first
							// bit
							appendTo.write(line.substring(1));
						}
						else if( firstChar == '#' )
						{
							// Comment - write it out
							appendTo.write(line);
						}
						else if( KEY_VALUE.matcher(line).matches() )
						{
							// 'key = value'
							appendTo.write(prepend);
							appendTo.write(line);
						}
						else
						{
							log("Line from " + languagePack + " doesn't conform to our"
								+ " standards. It must be either an empty line, a comment starting with #,"
								+ " 'somekey = somevalue' or be indented with a tab character if it's a multi-line"
								+ " value\n" + line);
							throw new BuildException("Line from " + languagePack + " doesn't conform to our"
								+ " standards. It must be either an empty line, a comment starting with #,"
								+ " 'somekey = somevalue' or be indented with a tab character if it's a multi-line"
								+ " value\n" + line);
						}
					}
					appendTo.newLine();

					line = reader.readLine();
				}
			}
		}
		finally
		{
			Closeables.closeQuietly(reader);
		}
	}

	private void deleteTempFiles()
	{
		for( LangFile pair : bundles.values() )
		{
			pair.getFile().delete();
		}
	}

	public File getDest()
	{
		return dest;
	}

	public void setDest(File dest)
	{
		this.dest = dest;
	}

	private static final class LangFile
	{
		private final File file;
		private final BufferedWriter writer;
		private final Document xmlDoc;

		public LangFile(String group)
		{
			try
			{
				file = File.createTempFile("langpack-" + group, ".properties");
				writer = new BufferedWriter(new FileWriter(file));

				xmlDoc = new Document(new Element("properties"));
				xmlDoc.setDocType(new DocType("properties", "http://java.sun.com/dtd/properties.dtd"));
			}
			catch( IOException ex )
			{
				throw new BuildException("Error creating temporary file or opening for writing", ex);
			}
		}

		public File getFile()
		{
			return file;
		}

		public BufferedWriter getWriter()
		{
			return writer;
		}

		public Document getXmlDoc()
		{
			return xmlDoc;
		}
	}
}
