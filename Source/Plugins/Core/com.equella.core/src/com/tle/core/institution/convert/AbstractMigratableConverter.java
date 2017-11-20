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
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.institution.convert.service.InstitutionImportService;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;

public abstract class AbstractMigratableConverter<T> implements Converter
{
	@Inject
	protected InitialiserService initialiserService;
	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected RunAsInstitution runAs;
	@Inject
	protected XmlHelper xmlHelper;
	@Inject
	protected InstitutionImportService instService;

	private PluginTracker<PostReadMigrator<T>> postReadMigs;

	@Override
	public void clone(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
		final String cid) throws IOException
	{
		exportIt(staging, institution, params, cid);
		runAs.executeAsSystem(institution, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					importIt(staging, institution, params, cid);
				}
				catch( IOException e )
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void exportIt(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
		String cid) throws IOException
	{
		doInTransaction(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					doExport(staging, institution, params);
				}
				catch( IOException e )
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	@Transactional
	public void doInTransaction(Runnable runnable)
	{
		runnable.run();
	}

	@Override
	public void importIt(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
		String cid) throws IOException
	{
		doInTransaction(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					doImport(staging, institution, params);
				}
				catch( IOException e )
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void deleteIt(final TemporaryFileHandle staging, final Institution institution, final ConverterParams params,
		String cid)
	{
		doInTransaction(new Runnable()
		{
			@Override
			public void run()
			{
				doDelete(institution, params);
			}
		});
	}

	public void doDelete(Institution institution, ConverterParams callback)
	{
		throw new Error("You must override doDelete() or deleteIt()"); //$NON-NLS-1$
	}

	/**
	 * @throws IOException
	 */
	public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams callback)
		throws IOException
	{
		throw new Error("You must override doExport() or exportIt()"); //$NON-NLS-1$
	}

	/**
	 * @throws IOException
	 */
	public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
		throws IOException
	{
		throw new Error("You must override importIt() or doImport()"); //$NON-NLS-1$
	}

	protected Collection<PostReadMigrator<T>> getMigrations(ConverterParams params)
	{
		Collection<PostReadMigrator<T>> migrations = Lists.newArrayList();
		if( postReadMigs != null )
		{
			InstitutionInfo info = params.getInstituionInfo();
			Map<String, Extension> postReadExtMap = postReadMigs.getExtensionMap();

			// Get imported postread migrations
			final Set<Extension> importedPostReadMigs = Sets.newHashSet();
			for( String impExt : info.getPostReadMigrations() )
			{
				importedPostReadMigs.add(postReadExtMap.get(impExt));
			}

			// Get all available post read migrations in order
			Set<Extension> allAvailablePostReadMigs = instService.orderExtsByDependencies(postReadMigs,
				postReadExtMap.values());

			// Compare and get required migrations
			migrations = Collections2.transform(Sets.filter(allAvailablePostReadMigs, new Predicate<Extension>()
			{
				@Override
				@SuppressWarnings("nls")
				public boolean apply(Extension ext)
				{
					boolean isForConverter = ext.getParameter("forconverter").valueAsString()
						.equals(getStringId().toLowerCase());
					return isForConverter
						&& (postReadMigs.isParamTrue(ext, "alwaysRun", false) || !importedPostReadMigs.contains(ext));
				}
			}), new Function<Extension, PostReadMigrator<T>>()
			{
				@Override
				public PostReadMigrator<T> apply(Extension ext)
				{
					return postReadMigs.getBeanByExtension(ext);
				}
			});
		}
		return migrations;
	}

	public void runMigrations(Collection<PostReadMigrator<T>> migrations, T o) throws IOException
	{
		for( PostReadMigrator<T> mg : migrations )
		{
			mg.migrate(o);
		}
	}

	protected NameValue getStandardTask()
	{
		return getStandardTask(getStringId());
	}

	protected NameValue getStandardTask(String converterId)
	{
		return new NameValue(CurrentLocale.get("institutions.tasks." + converterId.toLowerCase()), converterId); //$NON-NLS-1$
	}

	@Override
	public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params)
	{
		tasks.add(getStandardTask());
	}

	public abstract String getStringId();

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		String converterId = getStringId();
		if( converterId != null )
		{
			postReadMigs = new PluginTracker<PostReadMigrator<T>>(pluginService, "com.tle.core.institution.convert",
				"postreadmigration", "id").setBeanKey("bean");
		}
	}

	public interface HibernateOperation<T>
	{
		void execute(T data);
	}
}
