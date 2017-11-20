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

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.plugins.PluginTracker;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MigrationConverter extends AbstractConverter<Object>
{
	private static final Logger LOGGER = Logger.getLogger(MigrationConverter.class);

	@Inject
	private PluginTracker<Migrator> xmlMigs;
	@Inject
	private PluginTracker<ItemXmlMigrator> itemXmlMigs;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		// nothing
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		// nothing
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		// Xml migrations
		for( Migrator mg : getXmlMigrators(params.getInstituionInfo()) )
		{
			LOGGER.info("Running xml migration " + mg.getClass().getCanonicalName());
			try
			{
				mg.execute(staging, params.getInstituionInfo(), params);
			}
			catch( Exception ex )
			{
				throw new RuntimeApplicationException(ex);
			}
		}

		// Item xml migrations
		LOGGER.info("Running item xml migrations");
		migrateItemXml(staging, params, getItemXmlMigrators(params.getInstituionInfo()));
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( type == ConvertType.IMPORT )
		{
			super.addTasks(type, tasks, params);

			for( Migrator mig : getXmlMigrators(params.getInstituionInfo()) )
			{
				LOGGER.info("Adding tasks for migrator " + mig.getClass().getCanonicalName());
				mig.addTasks(type, tasks, params);
			}
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.MIGRATION;
	}

	private synchronized Set<Migrator> getXmlMigrators(InstitutionInfo info)
	{
		Set<Migrator> xmlMigrations = new LinkedHashSet<Migrator>();

		// Get all xmlmigration extensions
		Map<String, Extension> extMap = xmlMigs.getExtensionMap();

		// Get import xml migrations
		Set<Extension> importedXmlMigs = Sets.newHashSet();
		for( String impExt : info.getXmlMigrations() )
		{
			importedXmlMigs.add(extMap.get(impExt));
		}

		// Get all available xml migrations in order
		Set<Extension> allAvailableXmlMigs = instService.orderExtsByDependencies(xmlMigs, extMap.values());

		// Compare imported and available and get required migrations
		Set<Extension> requiredXmlMigs = Sets.difference(allAvailableXmlMigs, importedXmlMigs);
		for( Extension ext : requiredXmlMigs )
		{
			xmlMigrations.add(xmlMigs.getBeanByExtension(ext));
		}

		return xmlMigrations;
	}

	private synchronized Set<ItemXmlMigrator> getItemXmlMigrators(InstitutionInfo info)
	{
		Set<ItemXmlMigrator> itemXmlMigrations = new LinkedHashSet<ItemXmlMigrator>();

		// Get all itemxmlmigration extensions
		Map<String, Extension> itemExtMap = itemXmlMigs.getExtensionMap();

		// Get import item xml migrations
		Set<Extension> importedItemXmlMigs = Sets.newHashSet();
		for( String impExt : info.getItemXmlMigrations() )
		{
			importedItemXmlMigs.add(itemExtMap.get(impExt));
		}

		// Get all available item xml migrations in order
		Set<Extension> allAvailableItemXmlMigs = instService.orderExtsByDependencies(itemXmlMigs, itemExtMap.values());

		// Compare imported and available and get required migrations
		Set<Extension> requiredItemXmlMigs = Sets.difference(allAvailableItemXmlMigs, importedItemXmlMigs);
		for( Extension ext : requiredItemXmlMigs )
		{
			itemXmlMigrations.add(itemXmlMigs.getBeanByExtension(ext));
		}

		return itemXmlMigrations;
	}

	public void migrateItemXml(TemporaryFileHandle staging, ConverterParams params,
		Set<ItemXmlMigrator> itemXmlMigrations)
	{
		DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.calculateitems");
		params.getCallback().setMessageCallback(message);
		final SubTemporaryFile itemsFolder = new SubTemporaryFile(staging, "items");
		message.setKey("institutions.converter.migration.allitemsmsg");
		final List<String> entries = xmlHelper.getXmlFileList(itemsFolder);
		message.setTotal(entries.size());

		for( ItemXmlMigrator itemMigrator : itemXmlMigrations )
		{
			try
			{
				itemMigrator.beforeMigrate(params, staging, itemsFolder);
			}
			catch( Exception ex )
			{
				throw new RuntimeException(
					"Error running item migrator " + itemMigrator.getClass().getName() + " on beforeMigrate", ex);
			}
		}

		for( String entry : entries )
		{
			boolean changed = false;
			PropBagEx xml = xmlHelper.readToPropBagEx(itemsFolder, entry);

			for( ItemXmlMigrator itemMigrator : itemXmlMigrations )
			{
				try
				{
					changed |= itemMigrator.migrate(params, xml, itemsFolder, entry);
				}
				catch( Exception ex )
				{
					throw new RuntimeException("Error running item migrator '" + itemMigrator.getClass().getName()
						+ "' on item '" + entry + "'", ex); //$NON-NLS-2$
				}
			}

			if( changed )
			{
				if( xml.isNodeTrue("@delete") )
				{
					fileSystemService.removeFile(itemsFolder, entry);
				}
				else
				{
					xmlHelper.writeFromPropBagEx(itemsFolder, entry, xml);
				}
			}

			message.incrementCurrent();
		}

		// after migrate...
		for( ItemXmlMigrator itemMigrator : itemXmlMigrations )
		{
			try
			{
				itemMigrator.afterMigrate(params, itemsFolder);
			}
			catch( Exception ex )
			{
				throw new RuntimeException(
					"Error running item migrator " + itemMigrator.getClass().getName() + " on afterMigrate", ex);
			}
		}
	}
}