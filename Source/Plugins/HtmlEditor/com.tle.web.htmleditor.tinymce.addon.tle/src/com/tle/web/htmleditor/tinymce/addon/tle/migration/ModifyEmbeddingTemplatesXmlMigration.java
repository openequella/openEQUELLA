package com.tle.web.htmleditor.tinymce.addon.tle.migration;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;
import com.tle.core.mimetypes.institution.MimeEntryConverter;
import com.tle.web.htmleditor.tinymce.addon.tle.TinyMceAddonConstants;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ModifyEmbeddingTemplatesXmlMigration extends XmlMigrator
{
	private static Properties oldProps;
	private static Properties newProps;
	static
	{
		oldProps = loadProperties("embedder/old_templates.properties");
		newProps = loadProperties("embedder/templates.properties");
	}

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile mimeFolder = MimeEntryConverter.getMimeFolder(staging);
		final Set<String> types = getMigratableMimeTypes();
		for( String type : types )
		{
			final String filename = MimeEntryConverter.getFilenameForType(type);
			if( fileExists(mimeFolder, filename) )
			{
				final MimeEntry mimeEntry = xmlHelper.readXmlFile(mimeFolder, filename);
				final String currentTemplate = mimeEntry.getAttributes().get(TinyMceAddonConstants.MIME_TEMPLATE_KEY);
				// Only upgrade if it hasn't been changed
				if( currentTemplate == null || getOldTemplate(mimeEntry.getType()).equals(currentTemplate) )
				{
					mimeEntry.getAttributes().put(TinyMceAddonConstants.MIME_TEMPLATE_KEY,
						getNewTemplate(mimeEntry.getType()));
					xmlHelper.writeXmlFile(mimeFolder, filename, mimeEntry);
				}
			}
		}
	}

	public static Set<String> getMigratableMimeTypes()
	{
		final Set<String> types = Sets.newHashSet();
		for( Object key : oldProps.keySet() )
		{
			types.add(key.toString());
		}
		return types;
	}

	private static Properties loadProperties(String filename)
	{
		try( InputStream file = ModifyEmbeddingTemplatesDatabaseMigration.class.getClassLoader().getResourceAsStream(
			filename) )
		{
			if( file != null )
			{
				final Properties props = new Properties();
				props.load(file);
				return props;
			}
			else
			{
				throw new RuntimeException(new FileNotFoundException(filename));
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	public static String getOldTemplate(String type)
	{
		return oldProps.getProperty(type);
	}

	public static String getNewTemplate(String type)
	{
		return newProps.getProperty(type);
	}
}
