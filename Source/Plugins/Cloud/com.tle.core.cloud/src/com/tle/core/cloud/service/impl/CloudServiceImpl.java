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

package com.tle.core.cloud.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.mime.MimeEntry;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.NamedThreadFactory;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.searching.SortField;
import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.CloudAttachmentBean;
import com.tle.core.cloud.beans.CloudFacetBean;
import com.tle.core.cloud.beans.CloudFacetSearchResultsBean;
import com.tle.core.cloud.beans.CloudItemBean;
import com.tle.core.cloud.beans.CloudNavigationSettingsBean;
import com.tle.core.cloud.beans.CloudSearchResultsBean;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.beans.converted.CloudNavigationSettings;
import com.tle.core.cloud.search.CloudSearch;
import com.tle.core.cloud.search.filter.CloudFilterInfo;
import com.tle.core.cloud.service.CloudFacetSearchResults;
import com.tle.core.cloud.service.CloudSearchResults;
import com.tle.core.cloud.service.CloudService;
import com.tle.core.cloud.settings.CloudSettings;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemResolverExtension;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.HttpService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Response;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.DebugSettings;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(CloudService.class)
@Singleton
public class CloudServiceImpl implements CloudService, ItemResolverExtension
{
	private static final String CLOUD_URL = "http://cloud.equella.com/api/";
	//private static final String CLOUD_URL = "http://epspqa.stg-openclass.com/cloud/api/";

	private static final String AUTOTEST_TOKEN = "41bbfcf4-0a2b-49cf-9931-d0ce592d16ae";

	@Nullable
	private ObjectMapper jsonMapper;
	private final ExecutorService executor = new ThreadPoolExecutor(12, 60, 30, TimeUnit.MINUTES,
		new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("CloudService"));

	@Inject
	private ObjectMapperService objectMapperService;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private HttpService httpService;

	// We only use a single key here - it's just a handy timeout cache
	private LoadingCache<String, CloudFilterInfo> filterCache = CacheBuilder.newBuilder()
		.expireAfterAccess(30, TimeUnit.MINUTES).build(new CacheLoader<String, CloudFilterInfo>()
		{
			@Override
			public CloudFilterInfo load(String key) throws Exception
			{
				try
				{
					return buildFiltersCache();
				}
				catch( Throwable t )
				{
					throw Throwables.propagate(t);
				}
			}
		});

	private LoadingCache<String, Integer> resultCountCache = CacheBuilder.newBuilder()
		.expireAfterWrite(30, TimeUnit.MINUTES).softValues().build(new CacheLoader<String, Integer>()
		{
			@Override
			public Integer load(String query) throws Exception
			{
				final String response = doQuery(query, null, false, null, null, 0, 0);
				final ObjectNode result = (ObjectNode) getObjectMapper().readTree(response);
				return result.get("available").asInt();
			}
		});

	private LoadingCache<PagedCloudSearch, CloudSearchResultsInternal> resultCache = CacheBuilder.newBuilder()
		.expireAfterWrite(30, TimeUnit.MINUTES).softValues()
		.build(new CacheLoader<PagedCloudSearch, CloudSearchResultsInternal>()
		{
			@Override
			public CloudSearchResultsInternal load(PagedCloudSearch pcs) throws Exception
			{
				final CloudSearch cloudSearch = pcs.getCloudSearch();
				final int offset = pcs.getOffset();

				final SortField[] sortType = cloudSearch.getSortFields();
				final boolean reverse = cloudSearch.isSortReversed();
				final String sort = sortType != null ? sortType[0].getField() : null;

				final String query = cloudSearch.getQuery();
				final String response = doQuery(query, sort, reverse, buildWhereClause(cloudSearch),
					"basic,metadata,attachment,detail", offset, pcs.getLength());
				final CloudSearchResultsBean csr = getObjectMapper().readValue(response, CloudSearchResultsBean.class);

				final List<CloudItem> itemResults = Lists
					.newArrayList(Lists.transform(csr.getResults(), new Function<CloudItemBean, CloudItem>()
				{
					@Override
					public CloudItem apply(CloudItemBean cib)
					{
						return convertCloudItemBean(cib);
					}
				}));
				// EPS feature request perhaps? thoughts?
				return new CloudSearchResultsInternal(itemResults, csr.getLength(), offset, csr.getAvailable());
			}
		});

	private synchronized ObjectMapper getObjectMapper()
	{
		if( jsonMapper == null )
		{
			jsonMapper = objectMapperService.createObjectMapper("rest");
			jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return jsonMapper;
	}

	@Override
	public boolean isCloudy()
	{
		return httpService.canAccessInternet() && !configService.getProperties(new CloudSettings()).isDisabled();
	}

	@Override
	public int resultCount(String query)
	{
		try
		{
			return resultCountCache.get(query);
		}
		catch( ExecutionException ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public CloudSearchResults search(final CloudSearch cloudSearch, final int offset, final int length)
	{
		try
		{
			final PagedCloudSearch pagedCloudSearch = new PagedCloudSearch(cloudSearch, offset, length);

			CloudSearchResultsInternal results = resultCache.getIfPresent(cloudSearch);
			final Future<CloudSearchResultsInternal> resultsFuture;
			if( results == null )
			{
				resultsFuture = executor.submit(new Callable<CloudSearchResultsInternal>()
				{
					@Override
					public CloudSearchResultsInternal call()
					{
						try
						{
							return resultCache.get(pagedCloudSearch);
						}
						catch( ExecutionException ee )
						{
							throw Throwables.propagate(ee.getCause());
						}
					}
				});
			}
			else
			{
				resultsFuture = null;
			}

			Integer unfilteredCount = resultCountCache.getIfPresent(cloudSearch.getQuery());
			final Future<Integer> countFuture;
			if( unfilteredCount == null )
			{
				countFuture = executor.submit(new Callable<Integer>()
				{
					@Override
					public Integer call()
					{
						try
						{
							return resultCountCache.get(cloudSearch.getQuery());
						}
						catch( ExecutionException ee )
						{
							throw Throwables.propagate(ee.getCause());
						}
					}
				});
			}
			else
			{
				countFuture = null;
			}

			if( resultsFuture != null )
			{
				try
				{
					results = resultsFuture.get();
				}
				catch( ExecutionException ee )
				{
					throw Throwables.propagate(ee.getCause());
				}
			}
			if( countFuture != null )
			{
				try
				{
					unfilteredCount = countFuture.get();
				}
				catch( ExecutionException ee )
				{
					throw Throwables.propagate(ee.getCause());
				}
			}

			final int available = results.getAvailable();
			final int filtered = unfilteredCount - available;
			return new CloudSearchResults(results.getResults(), results.getCount(), results.getOffset(), available,
				filtered);
		}
		catch( Throwable t )
		{
			throw Throwables.propagate(t);
		}
	}

	@Nullable
	@Override
	public CloudItem getItem(String uuid, int version)
	{
		try
		{
			final String response = doGetItem(uuid, Integer.toString(version), "all");
			if( response == null )
			{
				return null;
			}
			final CloudItemBean cib = getObjectMapper().readValue(response, CloudItemBean.class);
			return convertCloudItemBean(cib);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public int getLiveItemVersion(String uuid)
	{
		try
		{
			final String response = doGetItem(uuid, "latest", "detail");
			if( response == null )
			{
				throw new NotFoundException("No cloud item with UUID " + uuid);
			}
			final CloudItemBean cib = getObjectMapper().readValue(response, CloudItemBean.class);
			return cib.getVersion();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public MimeEntry getMimeType(CloudAttachment cloudAttachment)
	{
		if( CloudAttachment.TYPE_URL.equals(cloudAttachment.getType()) )
		{
			return mimeTypeService.getEntryForMimeType("equella/link");
		}
		return mimeTypeService.getEntryForFilename(cloudAttachment.getUrl());
	}

	private CloudFilterInfo buildFiltersCache() throws Throwable
	{
		final CloudFilterInfo cfi = new CloudFilterInfo();

		final FacetSearchWorkerThread lang = new FacetSearchWorkerThread(CloudConstants.LANGUAGE_PATH)
		{
			@Override
			protected Function<CloudFacetBean, NameValue> getTransformer()
			{
				return new Function<CloudFacetBean, NameValue>()
				{
					@Override
					public NameValue apply(CloudFacetBean cfb)
					{
						String lang = cfb.getTerm();
						Locale locale = lang.contains("_") ? new Locale(lang.split("_")[0], lang.split("_")[1])
							: Locale.forLanguageTag(lang);
						return new NameValue(locale.getDisplayName(CurrentLocale.getLocale()), lang);
					}
				};
			}

			@Override
			protected void clean(List<NameValue> nameValues)
			{
				//Clean up langs without names (dodgy data)
				final Iterator<NameValue> nv = nameValues.iterator();
				while( nv.hasNext() )
				{
					final NameValue name = nv.next();
					if( Strings.isNullOrEmpty(name.getName()) )
					{
						nv.remove();
					}
				}
			}
		};
		final FacetSearchWorkerThread lic = new FacetSearchWorkerThread(CloudConstants.LICENCE_PATH);
		final FacetSearchWorkerThread pub = new FacetSearchWorkerThread(CloudConstants.PUBLISHER_PATH);
		final FacetSearchWorkerThread edu = new FacetSearchWorkerThread(CloudConstants.EDUCATION_LEVEL_PATH);
		final Future<List<NameValue>> langFuture = executor.submit(lang);
		final Future<List<NameValue>> licFuture = executor.submit(lic);
		final Future<List<NameValue>> pubFuture = executor.submit(pub);
		final Future<List<NameValue>> eduFuture = executor.submit(edu);
		try
		{
			cfi.setLanguages(langFuture.get());
			cfi.setLicences(licFuture.get());
			cfi.setPublishers(pubFuture.get());
			cfi.setEducationLevels(eduFuture.get());
		}
		catch( ExecutionException ee )
		{
			throw Throwables.propagate(ee.getCause());
		}
		return cfi;
	}

	@Nullable
	private String buildWhereClause(CloudSearch cs)
	{
		List<String> standard = Lists.newArrayList();
		List<String> custom = Lists.newArrayList();

		// Standard filters
		createClause(standard, CloudConstants.LANGUAGE_PATH, cs.getLanguage());
		createClause(standard, CloudConstants.LICENCE_PATH, cs.getLicence());
		createClause(standard, CloudConstants.PUBLISHER_PATH, cs.getPublisher());
		createClause(standard, CloudConstants.EDUCATION_LEVEL_PATH, cs.getEducationlevel());

		// Custom filters
		for( List<String> formats : cs.getFormats() )
		{
			createClause(custom, CloudConstants.FORMAT_PATH, formats);
		}

		boolean hasStd = !Check.isEmpty(standard);
		boolean hasCst = !Check.isEmpty(custom);

		if( hasStd || hasCst )
		{
			StringBuilder sb = new StringBuilder();
			sb.append("WHERE ");

			// Standard
			Iterator<String> stdIter = standard.iterator();
			while( stdIter.hasNext() )
			{
				sb.append(stdIter.next());
				if( stdIter.hasNext() )
				{
					sb.append(" AND ");
				}
			}

			// Custom
			Iterator<String> cstIter = custom.iterator();
			if( hasCst && hasStd )
			{
				sb.append(" AND (");
			}
			while( cstIter.hasNext() )
			{
				sb.append(cstIter.next());
				if( cstIter.hasNext() )
				{
					sb.append(" OR ");
				}
			}
			if( hasCst && hasStd )
			{
				sb.append(")");
			}

			return sb.toString();
		}

		return null;
	}

	private void createClause(List<String> clauses, String path, String clause)
	{
		if( !Check.isEmpty(clause) )
		{
			clause = clause.replaceAll("'", "''");
			clauses.add(MessageFormat.format("/xml{0} IS ''{1}''", path, clause));
		}
	}

	private void createClause(List<String> clauses, String path, List<String> formats)
	{
		if( !Check.isEmpty(formats) )
		{
			for( String f : formats )
			{
				createClause(clauses, path, f);
			}
		}
	}

	private CloudFacetSearchResults facetSearch(String... schemaNodes)
	{
		try
		{
			final String response = doFacetSearchQuery(schemaNodes);
			final CloudFacetSearchResultsBean cfsr = getObjectMapper().readValue(response,
				CloudFacetSearchResultsBean.class);

			final CloudFacetSearchResults results = new CloudFacetSearchResults(cfsr.getResults());
			return results;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Nullable
	private String doGetItem(String uuid, String version, String info)
	{
		final Request request = createRequest("item/" + uuid + '/' + version + '/');
		request.addParameter("info", info);
		try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
		{
			if( response.isOk() )
			{
				StringWriter sw = new StringWriter();
				CharStreams.copy(new InputStreamReader(response.getInputStream()), sw);
				return sw.toString();
			}
			else if( response.getCode() == 404 )
			{
				return null;
			}

			throw new RuntimeException("Unable to query cloud item: " + response.getMessage());
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	private String doFacetSearchQuery(String... nodes)
	{
		final Request request = createRequest("search/facet");
		request.addParameter("nodes", Joiner.on(',').join(nodes));
		request.addParameter("breadth", 200);
		try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
		{
			if( response.isOk() )
			{
				StringWriter sw = new StringWriter();
				CharStreams.copy(new InputStreamReader(response.getInputStream()), sw);
				return sw.toString();
			}

			throw new RuntimeException("Unable to query cloud search results: " + response.getMessage());
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	private String doQuery(String query, @Nullable String sort, boolean reverse, @Nullable String where,
		@Nullable String info, int offset, int length)
	{
		final Request request = createRequest("search");
		//cleanup query
		String q = query;
		q = q.replaceAll("/", "\\\\/");

		request.addParameter("q", q);
		request.addParameter("start", offset);
		request.addParameter("length", length);
		request.addParameter("reverse", Boolean.toString(reverse));
		if( sort != null )
		{
			request.addParameter("order", sort);
		}
		if( where != null )
		{
			request.addParameter("where", where);
		}
		if( info != null )
		{
			request.addParameter("info", info);
		}
		try( Response response = httpService.getWebContent(request, configService.getProxyDetails()) )
		{
			if( response.isOk() )
			{
				StringWriter sw = new StringWriter();
				CharStreams.copy(new InputStreamReader(response.getInputStream()), sw);
				return sw.toString();
			}

			throw new RuntimeException("Unable to query cloud search results: " + response.getMessage());
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	private Request createRequest(String url)
	{
		final Request request = new Request(CLOUD_URL + url);
		request.setMimeType("application/json");
		request.setCharset("utf-8");
		if( DebugSettings.isAutoTestMode() )
		{
			request.addHeader("Authorization", "Bearer " + AUTOTEST_TOKEN);
		}
		return request;
	}

	@Override
	public CloudFilterInfo getCloudFilterInfo()
	{
		try
		{
			return filterCache.get("");
		}
		catch( ExecutionException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private CloudItem convertCloudItemBean(CloudItemBean cib)
	{
		final CloudItem cloudItem = new CloudItem(cib.getUuid(), cib.getVersion());
		final I18NStrings nameStrings = cib.getNameStrings();
		if( nameStrings != null )
		{
			cloudItem.setNameStrings(nameStrings.getStrings());
		}
		final I18NStrings descriptionStrings = cib.getDescriptionStrings();
		if( descriptionStrings != null )
		{
			cloudItem.setDescriptionStrings(descriptionStrings.getStrings());
		}
		cloudItem.setMetadata(cib.getMetadata());
		cloudItem.setDateCreated(cib.getCreatedDate());
		cloudItem.setDateModified(cib.getModifiedDate());
		cloudItem.setAttachments(Lists
			.newArrayList(Lists.transform(cib.getAttachments(), new Function<CloudAttachmentBean, CloudAttachment>()
			{
				@Override
				public CloudAttachment apply(CloudAttachmentBean cab)
				{
					return convertCloudAttachmentBean(cab);
				}
			})));

		final CloudNavigationSettings navSettings = new CloudNavigationSettings();
		final CloudNavigationSettingsBean navSettingsBean = cib.getNavigation();
		if( navSettingsBean != null )
		{
			navSettings.setManualNavigation(navSettingsBean.isHideUnreferencedAttachments());
			navSettings.setShowSplitOption(navSettingsBean.isShowSplitOption());
		}
		cloudItem.setNavigationSettings(navSettings);
		return cloudItem;
	}

	private CloudAttachment convertCloudAttachmentBean(CloudAttachmentBean cab)
	{
		final CloudAttachment cloudAttachment = new CloudAttachment();
		cloudAttachment.setType(cab.getType());
		cloudAttachment.setUuid(cab.getUuid());
		cloudAttachment.setDescription(cab.getDescription());
		cloudAttachment.setUrl(cab.getFilename());
		cloudAttachment.setMd5sum(cab.getMd5());
		final Map<String, String> links = (Map<String, String>) cab.get("links");
		if( links != null )
		{
			cloudAttachment.setViewUrl(links.get("view"));
			cloudAttachment.setThumbnail(links.get("thumbnail"));
		}
		return cloudAttachment;
	}

	@Nullable
	@Override
	public <I extends IItem<?>> I resolveItem(ItemKey itemKey)
	{
		return (I) getItem(itemKey.getUuid(), itemKey.getVersion());
	}

	@Nullable
	@Override
	public PropBagEx resolveXml(IItem<?> item)
	{
		final CloudItem cloudItem = (CloudItem) item;
		return new PropBagEx(cloudItem.getMetadata());
	}

	@Nullable
	@Override
	public IAttachment resolveAttachment(ItemKey itemKey, String attachmentUuid)
	{
		final CloudItem item = getItem(itemKey.getUuid(), itemKey.getVersion());
		if( item == null )
		{
			return null;
		}
		return new UnmodifiableAttachments(item).getAttachmentByUuid(attachmentUuid);
	}

	@Override
	public boolean checkRestrictedAttachment(IItem<?> item, IAttachment attachment)
	{
		return false;
	}

	@Override
	public boolean canViewRestrictedAttachments(IItem<?> item)
	{
		return true;
	}

	@Override
	public boolean canRestrictAttachments(IItem<?> item)
	{
		return false;
	}

	private class FacetSearchWorkerThread implements Callable<List<NameValue>>
	{
		private final String facet;

		public FacetSearchWorkerThread(String facet)
		{
			this.facet = facet;
		}

		@Override
		public List<NameValue> call()
		{
			final CloudFacetSearchResults searchResults = facetSearch(facet);
			final List<NameValue> nameVals = new ArrayList<>(
				Lists.transform(searchResults.getResults(), getTransformer()));
			clean(nameVals);
			Collections.sort(nameVals, new NameValueComparator());
			return nameVals;
		}

		protected Function<CloudFacetBean, NameValue> getTransformer()
		{
			return new Function<CloudFacetBean, NameValue>()
			{
				@Override
				public NameValue apply(CloudFacetBean cfb)
				{
					return new NameValue(cfb.getTerm(), cfb.getTerm());
				}
			};
		}

		protected void clean(List<NameValue> nameValues)
		{
			//Nothing by default
		}
	}

	private static class CloudSearchResultsInternal extends SimpleSearchResults<CloudItem>
	{
		public CloudSearchResultsInternal(List<CloudItem> results, int count, int offset, int available)
		{
			super(results, count, offset, available);
		}
	}

	private static class PagedCloudSearch
	{
		private final CloudSearch cloudSearch;
		private final int offset;
		private final int length;

		public PagedCloudSearch(CloudSearch cloudSearch, int offset, int length)
		{
			this.cloudSearch = cloudSearch;
			this.offset = offset;
			this.length = length;
		}

		public CloudSearch getCloudSearch()
		{
			return cloudSearch;
		}

		public int getOffset()
		{
			return offset;
		}

		public int getLength()
		{
			return length;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(cloudSearch, offset, length);
		}

		@Override
		public boolean equals(@Nullable Object obj)
		{
			if( obj == null || !(obj instanceof PagedCloudSearch) )
			{
				return false;
			}
			else if( this == obj )
			{
				return true;
			}
			else
			{
				PagedCloudSearch rhs = (PagedCloudSearch) obj;
				return offset == rhs.offset && length == rhs.length && Objects.equals(cloudSearch, rhs.cloudSearch);
			}
		}
	}

	private static class NameValueComparator implements Comparator<NameValue>
	{
		@Override
		public int compare(NameValue arg0, NameValue arg1)
		{
			final String n1 = arg0.getName();
			final String n2 = arg1.getName();
			if( n1 == null )
			{
				return -1;
			}
			if( n2 == null )
			{
				return 1;
			}
			return n1.compareToIgnoreCase(n2);
		}
	}
}