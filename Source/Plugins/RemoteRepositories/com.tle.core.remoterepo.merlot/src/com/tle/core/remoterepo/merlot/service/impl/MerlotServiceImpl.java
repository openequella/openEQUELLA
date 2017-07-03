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

package com.tle.core.remoterepo.merlot.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gdata.util.common.html.HtmlToText;
import com.google.gdata.util.common.io.Closeables;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.MerlotSettings;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.searching.SearchResults;
import com.tle.common.util.Dates;
import com.tle.common.util.LocalDate;
import com.tle.common.util.TleDate;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.merlot.service.MerlotSearchParams;
import com.tle.core.remoterepo.merlot.service.MerlotService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;

/**
 * @author agibb
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind(MerlotService.class)
public class MerlotServiceImpl implements MerlotService
{
	/**
	 * Returned from the Basic API
	 */
	private static final String STATUS_OK = "ok";

	/**
	 * Fixed at 10 since the advanced API only supports pages of 10 (whereas the
	 * Basic API supports offsets and results counts, go figure)
	 */
	private static final int PAGE_SIZE = 10;

	private static final String MERLOT_BASE_URL = "http://www.merlot.org/merlot/";

	private static final String MERLOT_ADVANCED_URL = MERLOT_BASE_URL + "materialsAdvanced.rest";
	private static final String MERLOT_BASIC_URL = MERLOT_BASE_URL + "materials.rest";
	private static final String MERLOT_CATEGORIES_URL = MERLOT_BASE_URL + "categories.rest";
	private static final String MERLOT_COMMUNITIES_URL = MERLOT_BASE_URL + "communities.rest";
	private static final String MERLOT_LANGUAGES_URL = MERLOT_BASE_URL + "languages.rest";
	private static final String MERLOT_MATERIALTYPES_URL = MERLOT_BASE_URL + "materialTypes.rest";
	private static final String MERLOT_TECHNICALFORMATS_URL = MERLOT_BASE_URL + "technicalFormats.rest";
	private static final String MERLOT_MATERIALAUDIENCES_URL = MERLOT_BASE_URL + "materialAudiences.rest";

	private static final Logger LOGGER = Logger.getLogger(MerlotService.class);

	@Inject
	private HttpService httpService;
	@Inject
	private ConfigurationService configService;

	@Override
	public SearchResults<MerlotSearchResult> search(MerlotSearchParams search, int offset, int perpage)
	{
		final String allKeyWords = "allKeyWords";
		final String anyKeyWords = "anyKeyWords";
		final String exactKeyWords = "exactPhraseKeyWords";
		final String stringTrue = "true";
		final String stringFalse = "false";

		final MerlotSettings settings = getSettings(search.getMerlotSearch());
		final String licenceKey = settings.getLicenceKey();
		final boolean advanced = settings.isAdvancedApi();

		final StringBuilder buildUrl = new StringBuilder(advanced ? MERLOT_ADVANCED_URL : MERLOT_BASIC_URL);
		buildUrl.append("?licenseKey=");
		buildUrl.append(licenceKey);

		if( search.getKeywordUse() != null )
		{
			switch( search.getKeywordUse() )
			{
				case ALL:
					appendParam(buildUrl, allKeyWords, stringTrue);
					appendParam(buildUrl, anyKeyWords, stringFalse);
					appendParam(buildUrl, exactKeyWords, stringFalse);
					break;
				case ANY:
					appendParam(buildUrl, allKeyWords, stringFalse);
					appendParam(buildUrl, anyKeyWords, stringTrue);
					appendParam(buildUrl, exactKeyWords, stringFalse);
					break;
				case EXACT_PHRASE:
					appendParam(buildUrl, allKeyWords, stringFalse);
					appendParam(buildUrl, anyKeyWords, stringFalse);
					appendParam(buildUrl, exactKeyWords, stringTrue);
					break;
			}
		}
		else
		{
			appendParam(buildUrl, allKeyWords, stringTrue);
			appendParam(buildUrl, anyKeyWords, stringFalse);
			appendParam(buildUrl, exactKeyWords, stringFalse);
		}

		if( advanced )
		{
			appendParam(buildUrl, "keywords", search.getQuery());
			appendParam(buildUrl, "page", offset / PAGE_SIZE + 1);
			appendParam(buildUrl, "category", search.getCategory());
			appendParam(buildUrl, "community", search.getCommunity());
			appendParam(buildUrl, "language", search.getLanguage());
			appendParam(buildUrl, "materialType", search.getMaterialType());
			appendParam(buildUrl, "technicalFormat", search.getTechnicalFormat());
			appendParam(buildUrl, "audience", search.getMaterialAudience());
			// don't search for cost items specifically, otherwise you would be
			// search cost or no-cost
			// and not filtering at all! Same goes for creative commons.
			if( !search.isCost() )
			{
				appendParam(buildUrl, "cost", "0");
			}
			if( search.isCreativeCommons() )
			{
				appendParam(buildUrl, "creativeCommons", "1");
			}
			final TleDate createdAfter = search.getCreatedAfter();
			if( createdAfter != null )
			{
				appendParam(buildUrl, "createdSince", createdAfter.format(Dates.ISO_DATE_ONLY));
			}
			final TleDate createdBefore = search.getCreatedBefore();
			if( createdBefore != null )
			{
				appendParam(buildUrl, "createdBefore", createdBefore.format(Dates.ISO_DATE_ONLY));
			}
			appendParam(buildUrl, "sort.property", search.getSort());
			appendMobileParam(search, buildUrl);
		}
		else
		{
			appendParam(buildUrl, "keywords", search.getQuery());
			appendParam(buildUrl, "firstRecNumber", offset);
			appendParam(buildUrl, "size", perpage);
		}

		Response response = null;
		try
		{
			response = httpService.getWebContent(new Request(buildUrl.toString()), configService.getProxyDetails());
			final PropBagEx xml = new PropBagEx(response.getBody());

			checkForError(xml, advanced);

			int resultCount = (advanced ? -1 : xml.getIntNode("summary/resultCount"));
			// !! The MERLOT Basic API will only *ever* return 10 results max
			// final int available = (advanced ?
			// Integer.parseInt(xml.getNode("nummaterialstotal"))
			// : xml.getIntNode("summary/totalCount"));
			final int available = (advanced ? Integer.parseInt(xml.getNode("nummaterialstotal")) : resultCount);
			// int returnedOffset = (advanced ? offset :
			// xml.getIntNode("summary/lastRecNumber")
			// - resultCount);

			final List<MerlotSearchResult> results = new ArrayList<MerlotSearchResult>();

			int counter = 0;
			final PropBagThoroughIterator pti = (advanced ? xml.iterateAll("material") : xml
				.iterateAll("results/material"));
			for( PropBagEx r : pti )
			{
				final MerlotSearchResult searchResult = new MerlotSearchResult(counter + offset);
				convertXmlToSearchResult(searchResult, r, advanced);
				results.add(searchResult);
				counter++;
			}
			if( resultCount == -1 )
			{
				resultCount = counter;
			}

			return new MerlotSearchResults(results, resultCount, offset, available);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
		finally
		{
			Closeables.closeQuietly(response);
		}
	}

	private void appendMobileParam(MerlotSearchParams search, StringBuilder buildUrl)
	{

		if( !search.getMobileOS().isEmpty() )
		{
			// mobile OS with no types
			if( search.getMobileType().isEmpty() )
			{
				for( String mobileOS : search.getMobileOS() )
				{
					appendParam(buildUrl, "os", mobileOS + ":phone");
					appendParam(buildUrl, "os", mobileOS + ":tablet");
					appendParam(buildUrl, "os", mobileOS + ":other");
				}
			}
			else
			{
				// mobile OS and mobile type
				for( String mobileOS : search.getMobileOS() )
				{
					for( String mobileType : search.getMobileType() )
					{
						appendParam(buildUrl, "os", mobileOS + ":" + mobileType);
					}
				}
			}
		}
		else if( !search.getMobileType().isEmpty() )
		{
			// mobile type with no OS
			for( String mobileType : search.getMobileType() )
			{
				appendParam(buildUrl, "os", "android:" + mobileType);
				appendParam(buildUrl, "os", "ios:" + mobileType);
				appendParam(buildUrl, "os", "blackberry:" + mobileType);
				appendParam(buildUrl, "os", "window:" + mobileType);
			}
		}
	}

	private void appendParam(StringBuilder buildUrl, String paramName, int paramValue)
	{
		appendParam(buildUrl, paramName, Integer.toString(paramValue));
	}

	private void appendParam(StringBuilder buildUrl, String paramName, String paramValue)
	{
		if( !Check.isEmpty(paramValue) )
		{
			buildUrl.append("&").append(paramName).append("=").append(URLUtils.basicUrlEncode(paramValue));
		}
	}

	private void checkForError(PropBagEx xml, boolean advanced)
	{
		if( advanced )
		{
			if( xml.getNodeName().equals("invalid") )
			{
				throw new RuntimeException(CurrentLocale.get("com.tle.core.remoterepo.merlot.error.search",
					xml.getNode("")));
			}
		}
		else
		{
			final String status = xml.getNode("status");
			if( !STATUS_OK.equals(status) )
			{
				final String errorMessage = xml.getNode("error/message");
				final String code = xml.getNode("error/@code");
				throw new RuntimeException(CurrentLocale.get("com.tle.core.remoterepo.merlot.error.search", code
					+ " - " + errorMessage));
			}
		}
	}

	private void convertXmlToSearchResult(MerlotSearchResult searchResult, PropBagEx r, boolean advanced)
		throws Exception
	{
		searchResult.setTitle(r.getNode("title"));

		String description = HtmlToText.htmlToPlainText(r.getNode("description"));
		description = description.replaceAll("[\t\n]+", " ");
		searchResult.setDescription(description);

		searchResult.setUrl(r.getNode("URL"));
		searchResult.setAuthorName(r.getNode("authorName"));
		searchResult.setDetailUrl(r.getNode("detailURL"));

		if( advanced )
		{
			readAdvancedProperties(searchResult, r);
		}
		else
		{
			readBasicProperties(searchResult, r);
		}
	}

	private void readAdvancedProperties(MerlotSearchResult searchResult, PropBagEx r) throws Exception
	{
		searchResult.setPublishedDate(readStringDateProperty(r, "creationDate"));
		searchResult.setModifiedDate(readStringDateProperty(r, "modifiedDate"));
		searchResult.setCreativeCommons(readProperty(r, "creativecommons"));
		searchResult.setCategories(readListProperty(r, "categories/category"));
		searchResult.setAudiences(readListProperty(r, "audiences/audience"));

		// Grrr, languages is *supposed* to be a repeated node but appears to be
		// a space separated list!
		List<String> langs = readListProperty(r, "languages/language");
		List<String> actualLangs = new ArrayList<String>();
		if( !Check.isEmpty(langs) )
		{
			for( String lang : langs )
			{
				String[] parts = lang.split("\\s");
				for( String part : parts )
				{
					actualLangs.add(part);
				}
			}
		}
		searchResult.setLanguages(actualLangs);

		searchResult.setTechnicalRequirements(readProperty(r, "technicalrequirements"));
		searchResult.setCommunity(readProperty(r, "community"));
		searchResult.setCopyright(readProperty(r, "copyright"));
		searchResult.setCost(readProperty(r, "cost"));
		searchResult.setMaterialType(readProperty(r, "materialType"));
		searchResult.setSection508Compliant(readProperty(r, "compliant"));
		searchResult.setSourceAvailable(readProperty(r, "sourceavailable"));
		searchResult.setSubmitter(readProperty(r, "submitter"));
		searchResult.setPeerReviewUrl(readProperty(r, "peerreview"));
		searchResult.setPeerReviewRating(readFloatProperty(r, "peerreview/@score"));
		searchResult.setCommentsUrl(readProperty(r, "comments"));
		searchResult.setCommentsRating(readFloatProperty(r, "comments/@avgscore"));
		searchResult.setCommentsCount(readIntProperty(r, "comments/@count"));
		searchResult.setPersonalCollectionsUrl(readProperty(r, "personalCollections"));
		searchResult.setPersonalCollectionsCount(readIntProperty(r, "personalCollections/@count"));
		searchResult.setLearningExercisesUrl(readProperty(r, "learningexcercises"));
		searchResult.setLearningExercisesCount(readIntProperty(r, "learningexcercises/@count"));
		searchResult.setXml(r);
	}

	private void readBasicProperties(MerlotSearchResult searchResult, PropBagEx r) throws Exception
	{
		final String creationDateString = r.getNode("creationDate");
		if( !Check.isEmpty(creationDateString) )
		{
			searchResult.setPublishedDate(new Date(Long.parseLong(creationDateString)));
		}
		searchResult.setCreativeCommons(readProperty(r, "creativeCommons"));
		searchResult.setXml(r);
	}

	private float readFloatProperty(PropBagEx r, String property)
	{
		final String propVal = r.getNode(property);
		if( !Check.isEmpty(propVal) )
		{
			return Float.valueOf(propVal);
		}
		return 0F;
	}

	private int readIntProperty(PropBagEx r, String property)
	{
		final String propVal = r.getNode(property);
		if( !Check.isEmpty(propVal) )
		{
			return Integer.valueOf(propVal);
		}
		return 0;
	}

	private String readProperty(PropBagEx r, String property)
	{
		final String propVal = r.getNode(property);
		if( !Check.isEmpty(propVal) )
		{
			return propVal;
		}
		return null;
	}

	private List<String> readListProperty(PropBagEx r, String property)
	{
		final List<String> propVals = r.getNodeList(property);
		if( !Check.isEmpty(propVals) )
		{
			return propVals;
		}
		return null;
	}

	private Date readStringDateProperty(PropBagEx r, String property)
	{
		try
		{
			final String propVal = r.getNode(property);
			if( !Check.isEmpty(propVal) )
			{
				// See bug #7560, problems with non english language packs and
				// date parsing
				if( CurrentLocale.getLocale().getLanguage().equalsIgnoreCase(Locale.ENGLISH.getLanguage()) )
				{
					SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
					sdf.setTimeZone(CurrentTimeZone.get());
					return sdf.parse(propVal);
				}
				return new LocalDate(propVal, Dates.MERLOT, CurrentTimeZone.get()).toDate();
			}
			return null;
		}
		catch( ParseException dp )
		{
			throw Throwables.propagate(dp);
		}
	}

	@Override
	public MerlotSearchResult getResult(MerlotSearchParams search, int index)
	{
		final MerlotSettings settings = getSettings(search.getMerlotSearch());
		final int recIndex;
		final int offset;
		if( settings.isAdvancedApi() )
		{
			// round off to the start of the page index
			offset = (index / PAGE_SIZE) * PAGE_SIZE;
			recIndex = index % PAGE_SIZE;
		}
		else
		{
			offset = 0;
			recIndex = index;
		}
		final SearchResults<MerlotSearchResult> results = search(search, offset, PAGE_SIZE);
		return results.getResults().get(recIndex);
	}

	private MerlotSettings getSettings(FederatedSearch merlotSearch)
	{
		MerlotSettings settings = new MerlotSettings();
		settings.load(merlotSearch);
		return settings;
	}

	private String serviceUrl(FederatedSearch merlotSearch, String endpoint)
	{
		final MerlotSettings settings = getSettings(merlotSearch);
		final String licenceKey = settings.getLicenceKey();

		final StringBuilder buildUrl = new StringBuilder(endpoint);
		buildUrl.append("?licenseKey=");
		buildUrl.append(licenceKey);
		return buildUrl.toString();
	}

	@Override
	public Map<String, Collection<NameValue>> getCategories(FederatedSearch merlotSearch)
	{
		Response response = null;
		try
		{
			response = httpService.getWebContent(new Request(serviceUrl(merlotSearch, MERLOT_CATEGORIES_URL)),
				configService.getProxyDetails());

			final PropBagEx xml = new PropBagEx(response.getBody());
			final ListMultimap<String, NameValue> nvs = ArrayListMultimap.create();

			for( PropBagEx cat : xml.iterator("category") )
			{
				String id = cat.getNode("@id");
				String name = cat.getNode("name");
				nvs.put("root", new NameValue(name, id));
				recurseCats(cat, nvs.get(id), null);
			}

			return nvs.asMap();
		}
		catch( Exception t )
		{
			String msg = CurrentLocale.get("com.tle.core.remoterepo.merlot.error.filtervalues");
			LOGGER.error(msg, t);
			throw new RuntimeException(msg);
		}
		finally
		{
			Closeables.closeQuietly(response);
		}
	}

	private void recurseCats(PropBagEx xml, List<NameValue> nvs, String parentPath)
	{
		for( PropBagEx cat : xml.iterator("category") )
		{
			String id = cat.getNode("@id");
			String name = PathUtils.filePath(parentPath, cat.getNode("name"));
			nvs.add(new NameValue(name, id));
			recurseCats(cat, nvs, name);
		}
	}

	private List<NameValue> getSimpleNameValues(String endpoint, String objectNode, String nameNode, String valueNode)
	{
		try( Response response = httpService.getWebContent(new Request(endpoint), configService.getProxyDetails()) )
		{
			final PropBagEx xml = new PropBagEx(response.getBody());
			List<NameValue> nvs = new ArrayList<NameValue>();
			for( PropBagEx com : xml.iterateAll(objectNode) )
			{
				String id = com.getNode(valueNode);
				String name = com.getNode(nameNode);
				nvs.add(new NameValue(name, id));
			}

			Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
			return nvs;
		}
		catch( Exception t )
		{
			String msg = CurrentLocale.get("com.tle.core.remoterepo.merlot.error.filtervalues");
			LOGGER.error(msg, t);
			throw new RuntimeException(msg);
		}
	}

	@Override
	public List<NameValue> getCommunities(FederatedSearch merlotSearch)
	{
		return getSimpleNameValues(serviceUrl(merlotSearch, MERLOT_COMMUNITIES_URL), "community", "", "@id");
	}

	@Override
	public List<NameValue> getLanguages(FederatedSearch merlotSearch)
	{
		return getSimpleNameValues(serviceUrl(merlotSearch, MERLOT_LANGUAGES_URL), "language", "", "@code");
	}

	@Override
	public List<NameValue> getMaterialTypes(FederatedSearch merlotSearch)
	{
		return getSimpleNameValues(serviceUrl(merlotSearch, MERLOT_MATERIALTYPES_URL), "materialtype", "", "");
	}

	@Override
	public List<NameValue> getTechnicalFormats(FederatedSearch merlotSearch)
	{
		return getSimpleNameValues(serviceUrl(merlotSearch, MERLOT_TECHNICALFORMATS_URL), "technicalformat", "", "");
	}

	@Override
	public List<NameValue> getAudiences(FederatedSearch merlotSearch)
	{
		return getSimpleNameValues(serviceUrl(merlotSearch, MERLOT_MATERIALAUDIENCES_URL), "materialaudience", "", "");
	}

}
