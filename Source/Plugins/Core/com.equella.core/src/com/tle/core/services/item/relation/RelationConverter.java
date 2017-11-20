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

package com.tle.core.services.item.relation;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.Relation;
import com.tle.common.NameValue;
import com.tle.common.beans.xml.IdOnlyConverter;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;

@Bind
@Singleton
public class RelationConverter extends AbstractConverter<Relation>
{
	private static final String RELATIONS_CID = "item_relations"; //$NON-NLS-1$
	private static final String RELATIONS_FOLDER = "item_relations"; //$NON-NLS-1$

	@Inject
	private RelationDao relationDao;
	private XStream xstream;

	@Override
	public void doDelete(Institution institution, ConverterParams callback)
	{
		// Handled by the itemDao
	}

	@Override
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		final SubTemporaryFile relationsExportFolder = new SubTemporaryFile(staging, RELATIONS_FOLDER);
		xmlHelper.writeExportFormatXmlFile(relationsExportFolder, true);
		final Collection<Long> allIds = relationDao.getAllIdsForInstitution();
		for( Long id : allIds )
		{
			Relation relation = relationDao.findById(id);
			relation = initialiserService.initialise(relation);

			final BucketFile relationFolder = new BucketFile(relationsExportFolder, id);
			xmlHelper.writeXmlFile(relationFolder, id + ".xml", relation, getXStream());
		}
	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		final SubTemporaryFile relationsFolder = new SubTemporaryFile(staging, RELATIONS_FOLDER);
		final List<String> entries = xmlHelper.getXmlFileList(relationsFolder);
		for( String entry : entries )
		{
			Relation relation = xmlHelper.readXmlFile(relationsFolder, entry, getXStream());
			relation.setId(0);
			remapItem(relation.getFirstItem(), params, entry);
			remapItem(relation.getSecondItem(), params, entry);
			relationDao.save(relation);
			relationDao.flush();
		}
	}

	@SuppressWarnings("nls")
	public void remapItem(Item item, ConverterParams params, String entry)
	{
		Long newId = params.getItems().get(item.getId());
		if( newId == null )
		{
			throw new RuntimeException("Missing item for id '" + item.getId() + "' in " + entry);
		}
		item.setId(newId);
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			if( !(type == ConvertType.DELETE) )
			{
				tasks.add(new NameValue("Item Relations", RELATIONS_CID));
			}
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return null;
	}

	public XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = xmlHelper.createXStream(getClass().getClassLoader());
			xstream.registerConverter(new IdOnlyConverter(Item.class));
		}
		return xstream;
	}
}
