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

package com.tle.core.taxonomy.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.Institution;
import com.tle.common.EntityPack;
import com.tle.common.Pair;
import com.tle.common.beans.exception.IllegalOperationException;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.TaxonomyConstants;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.events.services.EventService;
import com.tle.core.taxonomy.TaxonomyDao;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.TermService;
import com.tle.core.taxonomy.datasource.TaxonomyDataSource;
import com.tle.core.taxonomy.datasource.TaxonomyDataSourceFactory;
import com.tle.core.taxonomy.scripting.objects.TaxonomyServiceScriptObject;
import com.tle.core.taxonomy.scripting.objects.impl.TaxonomyServiceScriptWrapper;

@SuppressWarnings("nls")
@SecureEntity("TAXONOMY")
@Bind(TaxonomyService.class)
@Singleton
public class TaxonomyServiceImpl extends AbstractEntityServiceImpl<EntityEditingBean, Taxonomy, TaxonomyService>
	implements
		TaxonomyService,
		TaxonomyModifiedListener,
		ScriptObjectContributor,
		InstitutionListener
{
	private final Cache<Institution, Cache<String, TaxonomyDataSource>> dataSourceCache = CacheBuilder.newBuilder()
		.softValues().expireAfterAccess(1, TimeUnit.HOURS).build();
	private Object cacheLock = new Object();

	private PluginTracker<TaxonomyDataSourceFactory> dataSourceFactoryTracker;

	@Inject
	private TermService termService;
	@Inject
	private EventService eventService;

	@Inject
	public TaxonomyServiceImpl(TaxonomyDao taxonomyDao)
	{
		super(Node.TAXONOMY, taxonomyDao);
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, Taxonomy> session, Taxonomy entity,
		List<ValidationError> errors)
	{
		// Nothing to validate at this time
	}

	private TaxonomyDataSource getDataSource(final String uuid)
	{
		synchronized( cacheLock )
		{
			Institution inst = CurrentInstitution.get();
			Cache<String, TaxonomyDataSource> instEntry = dataSourceCache.getIfPresent(inst);
			if( instEntry == null )
			{
				instEntry = CacheBuilder.newBuilder().softValues().expireAfterAccess(1, TimeUnit.HOURS).build();
				dataSourceCache.put(inst, instEntry);
			}

			TaxonomyDataSource tds = instEntry.getIfPresent(uuid);
			if( tds == null )
			{
				final Taxonomy taxonomy = getDao().getByUuid(uuid);
				if( taxonomy == null )
				{
					throw new NotFoundException("Could not find taxonomy with UUID " + uuid);
				}

				tds = getDataSourceNoCache(taxonomy);
				instEntry.put(uuid, tds);
			}
			return tds;
		}
	}

	private TaxonomyDataSource getDataSourceNoCache(final Taxonomy taxonomy)
	{
		final TaxonomyDataSourceFactory factory = dataSourceFactoryTracker.getBeanMap()
			.get(taxonomy.getDataSourcePluginId());

		try
		{
			return factory.create(taxonomy);
		}
		catch( Exception ex )
		{
			throw new RuntimeException("Error instantiating taxonomy data source", ex);
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		dataSourceFactoryTracker = new PluginTracker<TaxonomyDataSourceFactory>(pluginService, "com.tle.core.taxonomy",
			"dataSourceFactory", PluginTracker.LOCAL_ID_FOR_KEY);
		dataSourceFactoryTracker.setBeanKey("bean");
	}

	@Override
	protected void afterStopEdit(EntityPack<Taxonomy> pack, Taxonomy oldEntity)
	{
		final Taxonomy newEntity = pack.getEntity();
		eventService.publishApplicationEvent(new TaxonomyModifiedEvent(newEntity.getUuid()));
	}

	// ///////////////////////////////////////////////////////////////////////
	//
	// The following methods delegate to the relevant data source instance
	//
	// ///////////////////////////////////////////////////////////////////////
	@Override
	public TermResult getTerm(String taxonomyUuid, String fullTermPath)
	{
		return getDataSource(taxonomyUuid).getTerm(fullTermPath);
	}

	@Override
	public List<TermResult> getChildTerms(String taxonomyUuid, String parentTermId)
	{
		return getDataSource(taxonomyUuid).getChildTerms(parentTermId);
	}

	@Override
	public Pair<Long, List<TermResult>> searchTerms(String taxonomyUuid, String query, SelectionRestriction restriction,
		int limit, boolean searchFullTerm)
	{
		return getDataSource(taxonomyUuid).searchTerms(query, restriction, limit, searchFullTerm);
	}

	@Override
	public String getDataForTerm(String taxonomyUuid, String termId, String key)
	{
		return getDataSource(taxonomyUuid).getDataForTerm(termId, key);
	}

	@Override
	public TermResult addTerm(String taxonomyUuid, String parentFullPath, String termValue, boolean createHierarchy)
	{
		TaxonomyDataSource dataSource = getDataSource(taxonomyUuid);
		if( dataSource.supportsTermAddition() )
		{
			return dataSource.addTerm(parentFullPath, termValue, createHierarchy);
		}

		throw new IllegalOperationException(CurrentLocale.get("com.tle.core.taxonomy.service.error.cantinsert"));
	}

	@Override
	public void validateTerm(String taxonomyUuid, String parentFullTermPath, String termValue, boolean requireParent)
		throws InvalidDataException
	{
		TaxonomyDataSource dataSource = getDataSource(taxonomyUuid);
		dataSource.validateTerm(parentFullTermPath, termValue, requireParent);
	}

	@Override
	public boolean supportsTermAddition(String taxonomyUuid)
	{
		return getDataSource(taxonomyUuid).supportsTermAddition();
	}

	@Override
	public boolean supportsTermBrowsing(String taxonomyUuid)
	{
		return getDataSource(taxonomyUuid).supportsTermBrowsing();
	}

	@Override
	public boolean supportsTermSearching(String taxonomyUuid)
	{
		return getDataSource(taxonomyUuid).supportsTermSearching();
	}

	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		objects.put(TaxonomyServiceScriptObject.DEFAULT_VARIABLE, new TaxonomyServiceScriptWrapper(this));
	}

	@Override
	public String getExportImportFolder()
	{
		return TaxonomyConstants.TAXONOMY_EXPORT_FOLDER;
	}

	@Override
	protected void deleteReferences(Taxonomy taxonomy)
	{
		// delete all the terms (even if this is not currently an internal
		// data source taxonomy, it still may have terms from a previous
		// incarnation)
		termService.deleteForTaxonomy(taxonomy);
	}

	@Override
	protected void doAfterImport(TemporaryFileHandle taxonomyImportFolder, EntityEditingBean bean, Taxonomy taxonomy,
		ConverterParams params)
	{
		final SubTemporaryFile termsFolder = new SubTemporaryFile(taxonomyImportFolder,
			TaxonomyConstants.TERMS_EXPORT_FOLDER);
		if( fileSystemService.fileExists(termsFolder) )
		{
			// DELETE ALL EXISTING TERMS
			termService.deleteForTaxonomy(taxonomy);

			termService.doImport(taxonomy, termsFolder, params.getInstituionInfo().getInstitution(), params);

			// remove the terms folder so it doesn't get committed as an entity
			// file
			fileSystemService.removeFile(termsFolder);
		}
	}

	@Override
	public void prepareExport(TemporaryFileHandle exportFolder, Taxonomy taxonomy, ConverterParams params)
	{
		// Export all the terms (even if this is not currently an internal data
		// source taxonomy, it still may have terms from a previous incarnation)
		final SubTemporaryFile termsFolder = new SubTemporaryFile(exportFolder, TaxonomyConstants.TERMS_EXPORT_FOLDER);

		termService.doExport(taxonomy, termsFolder, params.getInstituionInfo().getInstitution(), params);

		super.prepareExport(exportFolder, taxonomy, params);
	}

	@Override
	protected void beforeClone(TemporaryFileHandle staging, EntityPack<Taxonomy> pack)
	{
		// export the terms into the staging area
		prepareExport(staging, pack.getEntity(),
			new ConverterParams(institutionImportService.getInfoForCurrentInstitution()));
	}

	@Override
	public void taxonomyModifiedEvent(TaxonomyModifiedEvent event)
	{
		synchronized( cacheLock )
		{
			final Cache<String, TaxonomyDataSource> instEntry = dataSourceCache.getIfPresent(CurrentInstitution.get());
			if( instEntry != null )
			{
				instEntry.invalidate(event.getTaxonomyUuid());
			}
		}
	}

	@Override
	public void institutionEvent(InstitutionEvent event)
	{
		synchronized( cacheLock )
		{
			dataSourceCache.invalidateAll(event.getChanges().values());
		}
	}

	@Override
	public boolean isTaxonomyReadonly(String taxonomyUuid)
	{
		return getDataSource(taxonomyUuid).isReadonly();
	}

	@Override
	public TermResult getTermResultByUuid(String taxonomyUuid, String termUuid)
	{
		return getDataSource(taxonomyUuid).getTermByUuid(termUuid);
	}

	@Override
	public String getDataByTermUuid(String taxonomyUuid, String termUuid, String dataKey)
	{
		return getDataSource(taxonomyUuid).getDataByTermUuid(termUuid, dataKey);
	}

	@Override
	public Map<String, String> getAllDataByTermUuid(String taxonomyUuid, String termUuid)
	{
		return getDataSource(taxonomyUuid).getAllDataByTermUuid(termUuid);
	}

}
