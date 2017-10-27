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

package com.tle.core.institution.convert.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.beans.progress.ListProgressCallback;
import com.tle.common.filesystem.handle.ExportFile;
import com.tle.common.filesystem.handle.ImportFile;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.hash.Hash;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionValidationError;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.institution.convert.Converter;
import com.tle.core.institution.convert.Converter.ConverterId;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionImport;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.ItemXmlMigrator;
import com.tle.core.institution.convert.Migrator;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.institution.convert.extension.InstitutionInfoInitialiser;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.FileSystemService;
import com.tle.core.xml.service.XmlService;

@Bind(InstitutionImportService.class)
@Singleton
@SuppressWarnings({"deprecation", "nls"})
public class InstitutionImportServiceImpl implements InstitutionImportService
{
	private static final Logger LOGGER = Logger.getLogger(InstitutionImportServiceImpl.class);
	@Deprecated
	private static final String OLD_INSTITUTION_FILE = "institutionData.xml";
	private static final String INSTITUTION_FILE = "institutionInfo.xml";

	@Inject
	private InstitutionService instService;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private XmlService xmlService;
	@Inject
	private PluginTracker<Converter> converterTracker;
	//Module thing doesn't appear to work with ?
	//@Inject
	private PluginTracker<PostReadMigrator<?>> postReadMigTracker;
	@Inject
	private PluginTracker<Migrator> xmlMigTracker;
	@Inject
	private PluginTracker<ItemXmlMigrator> itemXmlMigTracker;
	@Inject
	private PluginTracker<InstitutionInfoInitialiser> institutionInfoInitialisers;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		postReadMigTracker = new PluginTracker<PostReadMigrator<?>>(pluginService,
			pluginService.getPluginIdForObject(this), "postreadmigration", "id").setBeanKey("bean");
	}

	@Override
	public Map<String, String> validate(Institution inst)
	{
		Map<String, String> errors = Maps.newHashMap();
		List<InstitutionValidationError> validate = instService.validate(inst);
		for( InstitutionValidationError error : validate )
		{
			errors.put(error.getId(), error.getMessage().toString());
		}
		return errors;
	}

	@Override
	@SecureOnCallSystem
	public Institution update(Institution prototype)
	{
		instService.update(prototype);
		return instService.getInstitution(prototype.getUniqueId());
	}

	public static class ConverterTasks
	{
		private List<NameValue> normalTasks = new ArrayList<NameValue>();
		private List<NameValue> afterTasks = new ArrayList<NameValue>();

		public List<NameValue> getAfterTasks()
		{
			return afterTasks;
		}

		public void setAfterTasks(List<NameValue> afterTasks)
		{
			this.afterTasks = afterTasks;
		}

		public List<NameValue> getNormalTasks()
		{
			return normalTasks;
		}

		public void setNormalTasks(List<NameValue> normalTasks)
		{
			this.normalTasks = normalTasks;
		}

		public void add(NameValue id)
		{
			normalTasks.add(id);
		}

		public void addAfter(NameValue id)
		{
			afterTasks.add(id);
		}
	}

	public ConverterTasks getConverterTasksInternal(ConvertType type, ConverterParams params)
	{
		ConverterTasks tasks = new ConverterTasks();
		List<Converter> converters = getConverters();
		if( type == ConvertType.DELETE )
		{
			for( ListIterator<Converter> iter = converters.listIterator(converters.size()); iter.hasPrevious(); )
			{
				Converter converter = iter.previous();
				converter.addTasks(type, tasks, params);
			}
		}
		else
		{
			for( Converter converter : converters )
			{
				converter.addTasks(type, tasks, params);
			}
		}
		return tasks;
	}

	private List<Converter> getConverters()
	{
		return converterTracker.getBeanList();
	}

	@Override
	@SecureOnCallSystem
	public List<String> getConverterTasks(ConvertType type, InstitutionInfo imported)
	{
		ConverterParams params = new ConverterParams(imported);
		ConverterTasks tasks = getConverterTasksInternal(type, params);
		List<String> entries = new ArrayList<String>();
		for( NameValue cid : tasks.getNormalTasks() )
		{
			entries.add(cid.getName());
		}
		for( NameValue cid : tasks.getAfterTasks() )
		{
			entries.add(cid.getName());
		}
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.InstitutionService#delete(com.tle.beans.Institution
	 * )
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	@SecureOnCallSystem
	public void delete(Institution i, final ListProgressCallback callback)
	{
		// We need to do with as the passed in institution isn't associated with
		// the session
		final Institution institution = instService.getInstitution(i.getUniqueId());
		InstitutionInfo instInfo = getInstitutionInfo(i);
		instInfo.setFlags(new HashSet<String>());
		final ConverterParams params = new ConverterParams(instInfo, callback);
		final ConverterTasks tasks = getConverterTasksInternal(ConvertType.DELETE, params);
		runAs.executeAsSystem(institution, new Runnable()
		{
			@Override
			public void run()
			{
				final StagingFile staging = stagingService.createStagingArea();
				for( NameValue task : tasks.getNormalTasks() )
				{
					String value = task.getValue();
					getConverter(value).deleteIt(staging, institution, params, value);
					params.getCallback().incrementCurrent();
				}
				for( NameValue task : tasks.getAfterTasks() )
				{
					String value = task.getValue();
					getConverter(value).deleteIt(staging, institution, params, value);
					params.getCallback().incrementCurrent();
				}
				instService.deleteInstitution(institution);
			}
		});
	}

	Converter getConverter(String task)
	{
		return converterTracker.getBeanMap().get(task);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.InstitutionService#createByCloning(com.tle.beans
	 * .Institution)
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	@SecureOnCallSystem
	public void clone(final long targetSchemaId, final Institution newInstitution, long cloneFromId,
		final ListProgressCallback callback, final Set<String> flags)
	{
		Map<String, String> errors = validate(newInstitution);
		if( !errors.isEmpty() )
		{
			throw new RuntimeException(errors.values().toString());
		}

		Institution cloneFrom = instService.getInstitution(cloneFromId);

		if( Check.isEmpty(newInstitution.getAdminPassword()) )
		{
			newInstitution.setAdminPassword(cloneFrom.getAdminPassword());
		}
		else
		{
			newInstitution.setAdminPassword(Hash.hashPassword(newInstitution.getAdminPassword()));
		}
		final ConverterParams params = new ConverterParams(getInstitutionInfo(cloneFrom), callback);
		params.setFlags(flags);
		final Institution newCloneInstitution = instService.createInstitution(newInstitution, targetSchemaId);
		runAs.executeAsSystem(cloneFrom, new Runnable()
		{
			@Override
			public void run()
			{
				ConverterTasks tasks = getConverterTasksInternal(ConvertType.CLONE, params);
				StagingFile staging = stagingService.createStagingArea();
				List<String> tasksDone = new ArrayList<String>();
				try
				{
					for( NameValue task : tasks.getNormalTasks() )
					{
						String value = task.getValue();
						tasksDone.add(value);
						getConverter(value).clone(staging, newCloneInstitution, params, value);
						params.getCallback().incrementCurrent();
					}
					for( NameValue task : tasks.getAfterTasks() )
					{
						String value = task.getValue();
						tasksDone.add(value);
						getConverter(value).clone(staging, newCloneInstitution, params, value);
						params.getCallback().incrementCurrent();
					}

				}
				catch( Exception t )
				{
					doErrorCleanup(staging, tasksDone, newCloneInstitution, params);
					Throwables.propagate(t);
				}

			}
		});
		instService.setEnabled(newCloneInstitution.getUniqueId(), true);
	}

	@Override
	@SecureOnCallSystem
	public InstitutionInfo getInstitutionInfo(Institution institution)
	{
		return getInstitutionInfoInternal(institution);
	}

	@Override
	public InstitutionInfo getInfoForCurrentInstitution()
	{
		return getInstitutionInfoInternal(CurrentInstitution.get());
	}

	private InstitutionInfo getInstitutionInfoInternal(Institution institution)
	{
		InstitutionInfo info = new InstitutionInfo();
		info.setServerURL(instService.getInstitutionUrl(institution).toString());
		info.setInstitution(institution);
		info.setBuildVersion(ApplicationVersion.get().getFull());
		addAllMigrations(info);
		return info;
	}

	/**
	 * @return A staging ID
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	@SecureOnCallSystem
	public String exportInstitution(final Institution institution, final ListProgressCallback callback,
		final Set<String> flags)
	{
		// inst short name, date and time
		final StringBuilder name = new StringBuilder();
		name.append(institution.getFilestoreId());
		name.append('-');
		name.append(new LocalDate(CurrentTimeZone.get()).format(Dates.ISO).replace("-", "").replace(":", ""));
		final String exportName = name.toString();

		final TemporaryFileHandle export = new ExportFile(exportName);
		runAs.executeAsSystem(institution, new Runnable()
		{
			@Override
			public void run()
			{
				InstitutionInfo instInfo = getInstitutionInfo(institution);

				instInfo.setFlags(flags);

				ConverterParams params = new ConverterParams(instInfo, callback);
				ConverterTasks tasks = getConverterTasksInternal(ConvertType.EXPORT, params);

				try( OutputStream outStream = fileSystemService.getOutputStream(export, INSTITUTION_FILE, false) )
				{
					xmlService.serialiseToWriter(instInfo, new OutputStreamWriter(outStream, "UTF-8"));

					// Do export tasks
					for( NameValue cid : tasks.getNormalTasks() )
					{
						params.setMessageCallback(null);
						String value = cid.getValue();
						Converter converter = getConverter(value);
						converter.exportIt(export, institution, params, value);
						params.getCallback().incrementCurrent();
					}
				}
				catch( IOException e )
				{
					Throwables.propagate(e);
				}
				finally
				{
					if( fileSystemService.fileExists(new ExportFile(exportName + ".tgz")) )
					{
						fileSystemService.removeFile(export);
					}
				}
			}

		});
		return export.getMyPathComponent();
	}

	private void addAllMigrations(InstitutionInfo instData)
	{
		instData.setXmlMigrations(Sets.newHashSet(xmlMigTracker.getExtensionMap().keySet()));
		instData.setItemXmlMigrations(Sets.newHashSet(itemXmlMigTracker.getExtensionMap().keySet()));
		instData.setPostReadMigrations(Sets.newHashSet(postReadMigTracker.getExtensionMap().keySet()));
	}

	@Override
	@SecureOnCallSystem
	public InstitutionInfo getInstitutionInfo(ImportFile staging)
	{
		if( fileSystemService.fileExists(staging, INSTITUTION_FILE) )
		{
			try( InputStream in = fileSystemService.read(staging, INSTITUTION_FILE) )
			{
				PropBagEx xml = new PropBagEx(in);
				return (InstitutionInfo) xmlService.deserialiseFromXml(getClass().getClassLoader(), xml.toString());
			}
			catch( IOException ex )
			{
				throw Throwables.propagate(ex);
			}
		}
		else if( fileSystemService.fileExists(staging, OLD_INSTITUTION_FILE) )
		{
			// Massage InstitutionImport into InstitutionInfo
			try( InputStream in = fileSystemService.read(staging, OLD_INSTITUTION_FILE) )
			{
				PropBagEx xml = new PropBagEx(in);
				removeOldData(xml);
				InstitutionImport imp = (InstitutionImport) xmlService.deserialiseFromXml(getClass().getClassLoader(),
					xml.toString());
				return createInfoFromImport(imp);
			}
			catch( IOException ex )
			{
				throw Throwables.propagate(ex);
			}
		}
		else
		{
			throw new RuntimeException("NO INSTITUTION INFO/DATA FOUND");
		}
	}

	private void removeOldData(PropBagEx xml)
	{
		// The following code could probably be moved into some
		// sort of AfterUnzipMigrator extension point or something
		// v3.2 and prior has shortName and homepage - remove them!
		PropBagEx instXml = xml.getSubtree("institution");
		instXml.deleteNode("homepage");
		String shortName = instXml.getNode("shortName", null);
		if( shortName != null )
		{
			instXml.deleteNode("shortName");
			instXml.setNode("filestoreId", shortName);
		}
	}

	private InstitutionInfo createInfoFromImport(InstitutionImport imp)
	{
		InstitutionInfo info = new InstitutionInfo();
		info.setInstitution(imp.getInstitution());
		info.setBuildVersion(imp.getBuildVersion());
		info.setServerURL(imp.getServerURL());

		Set<String> conversions = imp.getConversions();
		Set<String> flags = imp.getFlags();
		if( !Check.isEmpty(conversions) )
		{
			info.setFlags(convertToFlags(conversions));
		}
		else if( flags != null )
		{
			info.setFlags(flags);
		}

		for( InstitutionInfoInitialiser initialiser : institutionInfoInitialisers.getBeanList() )
		{
			initialiser.initialiseInstitutionInfo(info, imp);
		}
		return info;
	}

	@Override
	@SecureOnCallSystem
	public Institution importInstitution(final ImportFile staging, long targetSchemaId,
		final InstitutionInfo newInstInfo, final ListProgressCallback callback)
	{
		InstitutionInfo impInstInfo = getInstitutionInfo(staging);
		final ConverterParams params = new ConverterParams(impInstInfo, callback);

		if( impInstInfo.getServerURL() != null )
		{
			try
			{
				params.setOldServerURL(new URL(newInstInfo.getServerURL()));
			}
			catch( MalformedURLException e )
			{
				LOGGER.warn(e, e);
			}
		}

		// Raw institution (from import)
		Institution uploadedInstitution = impInstInfo.getInstitution();

		// New institution
		Institution newInstitution = newInstInfo.getInstitution();

		Map<String, String> errors = validate(newInstitution);
		if( !errors.isEmpty() )
		{
			throw new RuntimeException(errors.values().toString());
		}
		if( Check.isEmpty(newInstitution.getAdminPassword()) )
		{
			newInstitution.setAdminPassword(uploadedInstitution.getAdminPassword());
		}
		else
		{
			newInstitution.setAdminPassword(Hash.hashPassword(newInstitution.getAdminPassword()));
		}

		final Institution createdInst = instService.createInstitution(newInstitution, targetSchemaId);
		runAs.executeAsSystem(createdInst, new Runnable()
		{
			@Override
			public void run()
			{
				doImport(params, staging, newInstInfo, createdInst, callback);
			}
		});
		instService.setEnabled(createdInst.getUniqueId(), true);
		return newInstitution;
	}

	@Transactional(propagation = Propagation.NEVER)
	protected void doImport(ConverterParams params, ImportFile staging, InstitutionInfo newInstInfo,
		Institution createdInst, ListProgressCallback callback)
	{
		params.setCurrentServerURL(instService.getInstitutionUrl());
		ConverterTasks tasks = getConverterTasksInternal(ConvertType.IMPORT, params);
		List<String> tasksDone = new ArrayList<String>();
		try
		{
			for( NameValue task : tasks.getNormalTasks() )
			{
				String value = task.getValue();
				tasksDone.add(value);
				getConverter(value).importIt(staging, createdInst, params, value);
				params.getCallback().incrementCurrent();
			}
			for( NameValue task : tasks.getAfterTasks() )
			{
				String value = task.getValue();
				tasksDone.add(value);
				getConverter(value).importIt(staging, createdInst, params, value);
				params.getCallback().incrementCurrent();
			}
		}
		catch( Exception t )
		{
			try
			{
				doErrorCleanup(staging, tasksDone, createdInst, params);
			}
			catch( Exception t2 )
			{
				LOGGER.error("Error cleaning up", t2);
			}
			if( t instanceof RuntimeException )
			{
				throw (RuntimeException) t;
			}
			throw new RuntimeException(t);
		}
		finally
		{
			fileSystemService.removeFile(staging, "");
		}
	}

	@Override
	public Set<String> convertToFlags(Set<String> conversions)
	{
		Set<String> flags = new HashSet<String>();
		if( conversions.contains(ConverterId.ITEMS.name()) )
		{
			if( !conversions.contains(ConverterId.ITEMSATTACHMENTS.name()) )
			{
				flags.add(ConverterParams.NO_ITEMSATTACHMENTS);
			}
		}
		else
		{
			flags.add(ConverterParams.NO_ITEMS);
			flags.add(ConverterParams.NO_ITEMSATTACHMENTS);
		}

		if( !conversions.contains(ConverterId.AUDITLOGS.name()) )
		{
			flags.add(ConverterParams.NO_AUDITLOGS);
		}

		return flags;
	}

	protected void doErrorCleanup(final TemporaryFileHandle staging, final List<String> tasksDone,
		final Institution institution, final ConverterParams params)
	{
		runAs.executeAsSystem(institution, new Runnable()
		{
			@Override
			public void run()
			{
				tasksDone.add(0, ConverterId.CLEANUPFILES.name());
				ListIterator<String> iter = tasksDone.listIterator(tasksDone.size());

				while( iter.hasPrevious() )
				{
					String task = iter.previous();
					getConverter(task).deleteIt(staging, institution, params, task);
				}
				instService.deleteInstitution(institution);
			}
		});
	}

	@Override
	public void cancelImport(ImportFile staging)
	{
		fileSystemService.removeFile(staging, "");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.InstitutionService#getMatchingConversions(java.
	 * util.Collection)
	 */
	@Override
	public Collection<NameValue> getMatchingConversions(Collection<String> conversions)
	{
		List<NameValue> entries = new ArrayList<NameValue>();
		Map<String, Extension> extensionMap = converterTracker.getExtensionMap();
		for( String cid : conversions )
		{
			Extension extension = extensionMap.get(cid);
			if( extension != null )
			{
				Collection<Parameter> parameters = extension.getParameters("selections");
				for( Parameter parameter : parameters )
				{
					String id = parameter.getSubParameter("id").valueAsString();
					if( id.equals(cid) )
					{
						entries.add(new NameValue(
							CurrentLocale.get(parameter.getSubParameter("nameKey").valueAsString()), cid));
					}
				}
			}
			else
			{
				LOGGER.warn("Unhandled conversion:" + cid);
			}
		}
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.tle.core.services.InstitutionService#getMatchingIds(java.util.List)
	 */
	@Override
	public Set<String> getMatchingIds(Collection<String> values)
	{
		return new HashSet<String>(values);
	}

	@Override
	public Set<String> getAllConversions()
	{
		return converterTracker.getExtensionMap().keySet();
	}

	@Override
	public Set<Extension> orderExtsByDependencies(PluginTracker<?> tracker, final Collection<Extension> unOrderedExts)
	{
		Set<Extension> orderedExts = new LinkedHashSet<Extension>(unOrderedExts.size());
		for( Extension ext : unOrderedExts )
		{
			if( !orderedExts.contains(ext) )
			{
				collectDependencies(tracker, ext, orderedExts);
				orderedExts.add(ext);
			}
		}
		return orderedExts;
	}

	private void collectDependencies(PluginTracker<?> tracker, final Extension extension, final Set<Extension> result)
	{
		for( String dependency : getDependencies(extension) )
		{
			Extension ext = tracker.getExtension(dependency);
			if( ext == null )
			{
				throw new Error("Failed to find dependant:" + dependency + " on " + extension);
			}
			if( !result.contains(ext) )
			{
				collectDependencies(tracker, ext, result);
				result.add(ext);
			}
		}
	}

	private Set<String> getDependencies(Extension extension)
	{
		Set<String> depends = new HashSet<String>();
		Collection<Parameter> ids = extension.getParameters("depends");
		for( Parameter idParam : ids )
		{
			depends.add(idParam.valueAsString());
		}
		return depends;
	}
}
