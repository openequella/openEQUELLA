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

package com.tle.web.remoterepo.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.search.SearchSettings;
import com.tle.beans.search.XmlBasedSearchSettings;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.xslt.service.XsltService;
import com.tle.web.remoterepo.RemoteRepoSearch;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.WebWizardService;

/**
 * @author aholland
 */
@Bind(RemoteRepoWebService.class)
@Singleton
public class RemoteRepoWebServiceImpl implements RemoteRepoWebService
{
	@Inject
	private WebWizardService webWizardService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private XsltService xsltService;
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private FederatedSearchService fedService;
	@Inject
	private UserSessionService userSessionService;

	private PluginTracker<RemoteRepoSearch> remoteRepoSearches;

	@Override
	public void forwardToWizard(SectionInfo info, PropBagEx xml, FederatedSearch search)
	{
		forwardToWizard(info, null, xml, search);
	}

	@Override
	public void forwardToWizard(SectionInfo info, StagingFile staging, PropBagEx xml, FederatedSearch search)
	{
		try
		{
			final ItemDefinition collection = collectionService.getByUuid(search.getCollectionUuid());
			if( collection == null )
			{
				throw new Error("No import collection for this Remote Repository");
			}

			webWizardService.forwardToNewItemWizard(info, collection.getUuid(),
				getInitialXml(info, staging, xml, search, collection), staging, true);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	protected PropBagEx getInitialXml(SectionInfo info, StagingFile staging, PropBagEx xml, FederatedSearch search,
		ItemDefinition collection) throws Exception
	{
		SearchSettings settings = getRemoteRepoSearch(search).createSettings(search);
		if( settings instanceof XmlBasedSearchSettings )
		{
			String transformName = ((XmlBasedSearchSettings) settings).getSchemaInputTransform();
			final PropBagEx initXml;
			if( transformName != null )
			{
				initXml = new PropBagEx(
					schemaService.transformForImport(collection.getSchema().getId(), transformName, xml));
			}
			else
			{
				initXml = xml;
			}
			return initXml;
		}
		return null;
	}

	@Override
	public void forwardToSearch(SectionInfo info, FederatedSearch search, boolean clearContext)
	{
		RemoteRepoSearch remoteRepoSearch = getRemoteRepoSearch(search);
		if( clearContext )
		{
			userSessionService.removeAttribute(remoteRepoSearch.getContextKey());
		}
		remoteRepoSearch.forward(info, search);
	}

	@Override
	public FederatedSearch getRemoteRepository(SectionInfo info)
	{
		FederatedSearch search = info.getAttributeForClass(FederatedSearch.class);
		if( search == null )
		{
			RemoteRepoSection remoteRepoSection = info.lookupSection(RemoteRepoSection.class);
			final String searchUuid = remoteRepoSection.getSearchUuid(info);
			if( searchUuid != null )
			{
				search = fedService.getForSearching(searchUuid);
				info.setAttribute(FederatedSearch.class, search);
			}
		}
		return search;
	}

	@Override
	public void setRemoteRepository(SectionInfo info, String searchUuid)
	{
		final RemoteRepoSection repoSection = info.lookupSection(RemoteRepoSection.class);
		repoSection.setSearchUuid(info, searchUuid);
	}

	protected RemoteRepoSearch getRemoteRepoSearch(FederatedSearch search)
	{
		String type = search.getType();
		RemoteRepoSearch remote = remoteRepoSearches.getBeanMap().get(type);
		if( remote == null )
		{
			throw new Error("No plugin extension found for Remote Repository type '" + type + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return remote;
	}

	@Override
	public String getDisplayText(FederatedSearch search, PropBagEx xml)
	{
		SearchSettings settings = getRemoteRepoSearch(search).createSettings(search);
		if( settings instanceof XmlBasedSearchSettings )
		{
			String transformXslt = ((XmlBasedSearchSettings) settings).getDisplayXslt();
			if( !Check.isEmpty(transformXslt) )
			{
				return xsltService.transform(new EntityFile(search), transformXslt, xml, true);
			}
		}
		return null;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		remoteRepoSearches = new PluginTracker<RemoteRepoSearch>(pluginService, "com.tle.web.fedsearch",
			"remoteRepoSearch", "type");
		remoteRepoSearches.setBeanKey("class");
	}
}
