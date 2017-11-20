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

package com.tle.core.entity.convert;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.entity.dao.AbstractEntityDao;
import com.tle.core.entity.dao.EntityLockingDao;
import com.tle.core.entity.registry.EntityRegistry;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.DefaultMessageCallback;
import com.tle.core.institution.convert.PostReadMigrator;

@SuppressWarnings("nls")
@Bind
@Singleton
public class EntityConverter extends AbstractConverter<BaseEntity>
{
	@Inject
	private EntityRegistry registry;
	@Inject
	private EntityLockingDao entityLockingDao;

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(null);
		params.setMessageCallback(message);
		final List<AbstractEntityService<?, BaseEntity>> services = registry.getAllEntityServices();
		for( AbstractEntityService<?, BaseEntity> service : services )
		{
			try
			{
				final String folder = getFolder(service);
				final XStream xstream = service.getXStream();

				final AbstractEntityDao<BaseEntity> dao = service.getEntityDao();
				final List<BaseEntity> allEntities = dao.enumerateAllIncludingSystem();

				message.setTotal(allEntities.size());
				message.setKey("institutions.converter.generic.genericmsg");
				message.setType(folder);
				message.setCurrent(0);

				final SubTemporaryFile exportFolder = new SubTemporaryFile(staging, folder);

				// write export format
				xmlHelper.writeExportFormatXmlFile(exportFolder, true);

				for( BaseEntity entity : allEntities )
				{
					final EntityFile entityFile = new EntityFile(entity);
					final String uuid = entity.getUuid();
					final BucketFile bucketFolder = new BucketFile(exportFolder, uuid);
					final SubTemporaryFile entityExportFolder = new SubTemporaryFile(bucketFolder, uuid);

					service.prepareExport(entityExportFolder, entity, params);

					fileSystemService.copyToStaging(entityFile, entityExportFolder, false);
					entity.setInstitution(null);
					xmlHelper.writeXmlFile(bucketFolder, uuid + ".xml", entity, xstream);
					message.incrementCurrent();
				}

				dao.clear();
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		final DefaultMessageCallback message = new DefaultMessageCallback(null);
		params.setMessageCallback(message);

		// Go backwards for dependancies
		final List<AbstractEntityService<?, BaseEntity>> services = registry.getAllEntityServices();
		for( ListIterator<AbstractEntityService<?, BaseEntity>> iter = services.listIterator(services.size()); iter
			.hasPrevious(); )
		{
			final AbstractEntityService<?, BaseEntity> service = iter.previous();
			final AbstractEntityDao<BaseEntity> entityDao = service.getEntityDao();
			final List<Long> ids = entityDao.enumerateAllIdsIncludingSystem();

			message.setTotal(ids.size());
			message.setKey("institutions.converter.generic.genericdeletemsg");
			message.setType(entityDao.getPersistentClass().getSimpleName().toLowerCase());
			message.setCurrent(0);

			for( Long id : ids )
			{
				BaseEntity entity = entityDao.findById(id);
				service.prepareDelete(entity, params);

				entityDao.delete(entity);
				entityDao.flush();
				entityDao.clear();
				message.incrementCurrent();
			}
		}

		entityLockingDao.deleteAll();
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
	{
		final Collection<PostReadMigrator<BaseEntity>> migrations = getMigrations(params);
		for( AbstractEntityService<?, BaseEntity> service : registry.getAllEntityServices() )
		{
			final XStream xstream = service.getXStream();
			final String folder = getFolder(service);
			final SubTemporaryFile entityImportFolder = new SubTemporaryFile(staging, folder);
			final List<String> entries = xmlHelper.getXmlFileList(entityImportFolder);

			// recreate it for every entry, the services may clobber it
			// (e.g. TermServiceImpl)
			final DefaultMessageCallback message = new DefaultMessageCallback(
				"institutions.converter.generic.genericmsg");
			params.setMessageCallback(message);
			message.setTotal(entries.size());
			message.setType(folder);
			message.setCurrent(0);

			final Map<Long, Long> old2new = params.getOld2new();
			final AbstractEntityDao<BaseEntity> dao = service.getEntityDao();
			for( String entry : entries )
			{
				try
				{
					final BaseEntity entity = xmlHelper.readXmlFile(entityImportFolder, entry, xstream);

					// data folder
					final SubTemporaryFile thisEntitiesImportFolder = new SubTemporaryFile(entityImportFolder,
						entry.replace(".xml", Constants.BLANK));
					service.prepareImport(thisEntitiesImportFolder, entity, params);

					final long oldId = entity.getId();
					initialiserService.initialiseClones(entity);
					entity.setInstitution(institution);

					runMigrations(migrations, entity);

					old2new.put(oldId, dao.save(entity));
					service.afterAdd(new EntityPack<BaseEntity>(entity, null));
					dao.flush();
					dao.clear();

					service.afterImport(thisEntitiesImportFolder, entity, params);

					fileSystemService.commitFiles(thisEntitiesImportFolder, new EntityFile(entity));
					message.incrementCurrent();
				}
				catch( Exception e )
				{
					throw new RuntimeException("Failed importing " + entry + " of " + folder, e);
				}
			}
		}
	}

	private String getFolder(AbstractEntityService<?, BaseEntity> aes)
	{
		final String folder = aes.getExportImportFolder();
		if( !Check.isEmpty(folder) )
		{
			return folder;
		}
		return aes.getEntityClass().getSimpleName().toLowerCase();
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.ENTITIES;
	}
}
