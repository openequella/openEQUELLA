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

package com.tle.core.harvester.impl;

import java.io.OutputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.common.beans.exception.ApplicationException;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.common.settings.standard.HarvesterSkipDrmSettings;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.search.DownloadItemSearch;
import com.tle.core.harvester.soap.SoapHarvesterService;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.DrmService;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.core.soap.service.SoapXMLService;
import com.tle.core.util.archive.ArchiveType;
import com.tle.web.viewurl.ViewItemUrlFactory;

@SuppressWarnings("nls")
@Bind(SoapHarvesterService.class)
@Singleton
public class SoapHarvesterServiceImpl implements PrivateSoapHarvesterService, SoapHarvesterService
{
	private static final String HARVESTER_USAGE = "harvesterUsage";

	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private FreeTextService freetextService;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private FreeTextService freeTextService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private UserService userService;
	@Inject
	private SoapXMLService soapXML;
	@Inject
	private DynaCollectionService dynaCollectionService;
	@Inject
	private StagingService stagingService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private ConfigurationService config;
	@Inject
	private DrmService drmService;
	@Inject
	private SchemaService schemaService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private WebServiceContext webServiceContext;

	@Override
	public String getDynamicCollections() throws Exception
	{
		List<VirtualisableAndValue<DynaCollection>> dcs = dynaCollectionService.enumerateExpanded(HARVESTER_USAGE);

		final PropBagEx collectionsXML = new PropBagEx();

		for( VirtualisableAndValue<DynaCollection> dcValue : dcs )
		{
			DynaCollection collection = dcValue.getVt();
			PropBagEx collectionXML = new PropBagEx().newSubtree("dyncol");
			collectionXML.setNode("id", collection.getId());
			collectionXML.setNode("uuid", collection.getUuid());
			collectionXML.setNode("name", CurrentLocale.get(collection.getName()));
			collectionXML.setNode("system", collection.isSystemType());
			String second = dcValue.getVirtualisedValue();
			if( second == null )
			{
				second = "";
			}
			collectionXML.setNode("virtval", second);

			collectionsXML.append("/", collectionXML);
		}

		return collectionsXML.toString();
	}

	@Override
	public String getSearchableCollections() throws Exception
	{
		Collection<ItemDefinition> collections = collectionService.enumerateSearchable();

		final PropBagEx collectionsXML = new PropBagEx();
		for( ItemDefinition collection : collections )
		{
			collectionsXML.append("/", soapXML.convertCollectionToXML(collection));
		}

		return collectionsXML.toString();
	}

	@Override
	public String searchItemsSince(String[] collectionUuids, boolean onlyLive, String since)
	{
		DownloadItemSearch search = new DownloadItemSearch();

		// Would be better if we could use security filtering on
		// UUID return results
		List<ItemDefinition> matchingSearchableUuid = collectionService
			.getMatchingSearchableUuid(Arrays.asList(collectionUuids));
		search.setCollectionUuids(collectionService.convertToUuids(matchingSearchableUuid));
		search.setDateRange(convertToDateRange(since));
		if( onlyLive )
		{
			search.setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}
		search.setSortType(SortType.DATEMODIFIED);

		SearchResults<Item> results = freetextService.search(search, 0, -1);

		PropBagEx xml = new PropBagEx().newSubtree("results");
		convertResultsToXml(results, xml);

		return xml.toString();
	}

	@Override
	public String searchDynamicCollection(String dynaCollection, String virtualisationValue, String since,
		boolean liveOnly)
	{
		DynaCollection dc = dynaCollectionService.getByUuid(dynaCollection);
		String freeTextQuery = dynaCollectionService.getFreeTextQuery(dc);
		FreeTextBooleanQuery searchClause = dynaCollectionService.getSearchClause(dc, virtualisationValue);

		DownloadItemSearch search = new DownloadItemSearch();
		search.setQuery(freeTextQuery);
		search.setFreeTextQuery(searchClause);
		search.setDateRange(convertToDateRange(since));

		SearchResults<Item> results = freeTextService.search(search, 0, -1);

		PropBagEx xml = new PropBagEx().newSubtree("results");
		convertResultsToXml(results, xml);

		return xml.toString();
	}

	private Date[] convertToDateRange(String since)
	{
		if( since == null || since.isEmpty() )
		{
			return null;
		}

		try
		{
			UtcDate utcDate = new UtcDate(since, Dates.ISO_WITH_TIMEZONE);
			return new Date[]{utcDate.toDate(), new Date()};
		}
		catch( ParseException e )
		{
			throw new RuntimeException("Error parsing date " + since);
		}
	}

	private void convertResultsToXml(SearchResults<Item> results, PropBagEx xml)
	{
		for( Item item : results.getResults() )
		{
			if( item != null )
			{
				ItemDefinition itemDef = initialiserService.initialise(item.getItemDefinition());
				item = initialiserService.initialise(item);
				item.setItemDefinition(itemDef);

				PropBagEx itemXml = soapXML
					.convertItemPackToXML(new ItemPack(item, itemService.getItemXmlPropBag(item), null), true);
				itemXml.setNode("item/url", urlFactory.createFullItemUrl(item.getItemId()).getHref());
				xml.newSubtree("result").append("", itemXml);
			}
		}
		xml.setNode("@count", results.getCount());
		xml.setNode("available", results.getAvailable());
	}

	@Override
	public String getItemXml(String itemUuid, int version) throws Exception
	{
		final ItemPack<Item> pack = itemService.getItemPack(new ItemId(itemUuid, version));
		final Item item = pack.getItem();
		drmCheck(item);

		PropBagEx packXml = soapXML.convertItemPackToXML(pack, false);
		PropBagEx xml = itemService.getItemXmlPropBag(item);

		xml.append("item", packXml.aquireSubtree("item/rights"));
		xml.append("item", packXml.aquireSubtree("item/attachments"));
		xml.append("item", packXml.aquireSubtree("item/navigationNodes"));
		xml.append("item", packXml.aquireSubtree("item/datecreated"));
		xml.append("item", packXml.aquireSubtree("item/datemodified"));

		if( packXml.nodeExists("item/itembody/packagefile") )
		{
			xml.append("item/itembody", packXml.aquireSubtree("item/itembody/packagefile"));
		}

		Schema schema = item.getItemDefinition().getSchema();
		String s = null;
		if( schema != null )
		{
			s = schemaService.transformForExport(schema.getId(), "HARVESTER", xml, true);
		}
		if( s == null )
		{
			s = xml.toString();
		}
		return s;
	}

	@Override
	@SecureOnCall(priv = "DOWNLOAD_ITEM")
	public void getItemAttachments(OutputStream cos, Item item) throws Exception
	{
		ItemFile itemFile = itemFileService.getItemFile(item);

		StagingFile stagingFile = stagingService.createStagingArea();

		if( fileSystemService.fileExists(itemFile) )
		{
			fileSystemService.copyToStaging(itemFile, "", stagingFile, "", false);
		}

		try
		{
			fileSystemService.zipFile(stagingFile, cos, ArchiveType.TAR_GZ);
		}
		finally
		{
			fileSystemService.removeFile(stagingFile, "");
		}
	}

	private void drmCheck(Item item) throws Exception
	{
		boolean skip = config.getProperties(new HarvesterSkipDrmSettings()).isHarvestingSkipDrm();
		if( !skip && !drmService.hasAcceptedOrRequiresNoAcceptance(item, false, false) )
		{
			throw new ApplicationException(CurrentLocale.get("com.tle.core.harvester.soap.drmerror"));
		}
	}

	private WebAuthenticationDetails getDetails()
	{
		return userService.getWebAuthenticationDetails(
			(HttpServletRequest) webServiceContext.getMessageContext().get(AbstractHTTPDestination.HTTP_REQUEST));
	}

	@Override
	public String login(String username, String password) throws Exception
	{
		return soapXML.convertUserToXML(userService.login(username, password, getDetails(), true).getUserBean())
			.toString();
	}

	@Override
	public String loginWithToken(String token) throws Exception
	{
		return soapXML.convertUserToXML(userService.loginWithToken(token, getDetails(), true).getUserBean()).toString();
	}

	@Override
	public void logout()
	{
		userService.logoutToGuest(getDetails(), false);
	}

	@Override
	public String prepareDownload(String itemUuid, int version) throws Exception
	{
		Item item = itemService.get(new ItemId(itemUuid, version));

		drmCheck(item);

		final StagingFile staging = stagingService.createStagingArea();
		getItemAttachments(fileSystemService.getOutputStream(staging, "download.tar.gz", false), item);
		return institutionService.institutionalise("file/" + staging.getUuid() + "/$/download.tar.gz");
	}
}
