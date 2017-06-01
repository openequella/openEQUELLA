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
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.audit.AuditLogTable;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.auditlog.AuditLogDao;
import com.tle.core.auditlog.AuditLogExtension;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.filesystem.BucketFile;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDao;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.services.InstitutionImportService.ConvertType;
import com.tle.core.services.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.util.DefaultMessageCallback;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AuditLogConverter extends AbstractConverter<Object>
{
	private static final int PER_XML_FILE = 1000;
	private static final String AUDITLOGS = "auditlogs";
	private static final String AUDITLOGS2 = "auditlogs2";

	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(AuditLogConverter.class) + ".";
	private static final String KEY_NAME = KEY_PREFIX + "auditlogs.converter";

	@Inject
	private AuditLogDao auditLogDao;
	@Inject
	private AuditLogService auditLogService;

	@Override
	public void doDelete(Institution institution, ConverterParams params)
	{
		auditLogService.removeEntriesForInstitution(institution);
	}

	@Override
	public void doExport(TemporaryFileHandle staging, final Institution institution, ConverterParams callback)
		throws IOException
	{
		int offs = 0;
		int size = -1;
		SubTemporaryFile auditFolder = new SubTemporaryFile(staging, AUDITLOGS);
		// write out the format details
		xmlHelper.writeExportFormatXmlFile(auditFolder, true);
		do
		{
			List<AuditLogEntry> entries = auditLogDao.findAllByCriteria(null, offs, PER_XML_FILE,
				Restrictions.eq("institution", institution));
			size = entries.size();
			if( size != 0 )
			{
				final BucketFile bucketFolder = new BucketFile(auditFolder, offs);
				xmlHelper.writeXmlFile(bucketFolder, offs + "-" + (offs + size) + ".xml", entries);
				offs += size;
			}
		}
		while( size != 0 );

		Collection<AuditLogExtension> extensions = auditLogService.getExtensions();
		for( AuditLogExtension auditLogExtension : extensions )
		{
			GenericDao<? extends AuditLogTable, Long> dao = auditLogExtension.getDao();
			offs = 0;
			size = -1;
			auditFolder = new SubTemporaryFile(staging, AUDITLOGS2 + '/' + auditLogExtension.getShortName());
			// write out the format details
			xmlHelper.writeExportFormatXmlFile(auditFolder, true);
			do
			{
				List<? extends AuditLogTable> entries = dao.findAllByCriteria(null, offs, PER_XML_FILE,
					Restrictions.eq("institution", institution));
				size = entries.size();
				if( size != 0 )
				{
					final BucketFile bucketFolder = new BucketFile(auditFolder, offs);
					xmlHelper.writeXmlFile(bucketFolder, offs + "-" + (offs + size) + ".xml", entries);
					offs += size;
				}
			}
			while( size != 0 );
		}

	}

	@Override
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		SubTemporaryFile auditImportFolder = new SubTemporaryFile(staging, AUDITLOGS);

		List<String> filenames = xmlHelper.getXmlFileList(auditImportFolder);
		DefaultMessageCallback message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
		params.setMessageCallback(message);
		message.setTotal(filenames.size());
		message.setType(CurrentLocale.get(KEY_NAME));
		message.setCurrent(0);
		for( String xmlFilename : filenames )
		{
			final List<AuditLogEntry> entries = xmlHelper.readXmlFile(auditImportFolder, xmlFilename);
			for( AuditLogEntry entry : entries )
			{
				entry.setInstitution(institution);
				entry.setId(0);
				auditLogDao.save(entry);
				auditLogDao.flush();
				auditLogDao.clear();
			}
			message.incrementCurrent();
		}
		Collection<AuditLogExtension> extensions = auditLogService.getExtensions();
		for( AuditLogExtension auditLogExtension : extensions )
		{
			auditImportFolder = new SubTemporaryFile(staging, AUDITLOGS2 + '/' + auditLogExtension.getShortName());

			filenames = xmlHelper.getXmlFileList(auditImportFolder);
			message = new DefaultMessageCallback("institutions.converter.generic.genericmsg");
			params.setMessageCallback(message);
			message.setTotal(filenames.size());
			message.setType(CurrentLocale.get(KEY_NAME));
			message.setCurrent(0);
			GenericDao<? extends AuditLogTable, Long> dao = auditLogExtension.getDao();
			for( String xmlFilename : filenames )
			{
				final List<AuditLogTable> entries = xmlHelper.readXmlFile(auditImportFolder, xmlFilename);
				for( AuditLogTable entry : entries )
				{
					entry.setInstitution(institution);
					entry.setId(0);
					dao.saveAny(entry);
					dao.flush();
					dao.clear();
				}
				message.incrementCurrent();
			}
		}
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.AUDITLOGS;
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		if( !params.hasFlag(ConverterParams.NO_AUDITLOGS) )
		{
			if( type == ConvertType.DELETE )
			{
				tasks.addAfter(getStandardTask());
			}
			else
			{
				super.addTasks(type, tasks, params);
			}
		}
	}
}
