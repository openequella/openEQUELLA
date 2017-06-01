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

package com.tle.web.remoterepo.merlot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.MerlotSettings;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.merlot.service.MerlotService;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.sections.SectionInfo;

@Bind(MerlotWebService.class)
@Singleton
@SuppressWarnings("nls")
public class MerlotWebServiceImpl implements MerlotWebService
{
	// Two weeks should be fine
	private static final Cache<String, Collection<NameValue>> OPTION_CACHE = CacheBuilder.newBuilder().softValues()
		.expireAfterWrite(14, TimeUnit.DAYS).build();

	private static final Cache<String, Map<String, NameValue>> LANGUAGES_CACHE = CacheBuilder.newBuilder().softValues()
		.expireAfterWrite(14, TimeUnit.DAYS).build();

	private static final Cache<String, Map<String, Collection<NameValue>>> CATEGORIES_CACHE = CacheBuilder.newBuilder()
		.softValues().expireAfterWrite(14, TimeUnit.DAYS).build();

	@Inject
	private RemoteRepoWebService repoWebService;
	@Inject
	private MerlotService merlotService;

	@Override
	public MerlotSettings getSettings(SectionInfo info)
	{
		MerlotSettings settings = info.getAttributeForClass(MerlotSettings.class);
		if( settings == null )
		{
			FederatedSearch search = repoWebService.getRemoteRepository(info);
			settings = new MerlotSettings();
			settings.load(search);
			info.setAttribute(MerlotSettings.class, settings);
		}
		return settings;
	}

	@Override
	public Collection<NameValue> getCategories(SectionInfo info, String categoryId)
	{
		Map<String, Collection<NameValue>> categories = CATEGORIES_CACHE.getIfPresent("categories");
		if( categories == null )
		{
			categories = Maps.newHashMap();
			for( Map.Entry<String, Collection<NameValue>> entry : merlotService.getCategories(
				repoWebService.getRemoteRepository(info)).entrySet() )
			{
				categories.put("categories." + entry.getKey(), entry.getValue());
			}
			CATEGORIES_CACHE.put("categories", categories);
		}

		Collection<NameValue> rv = categories.get("categories." + (Check.isEmpty(categoryId) ? "root" : categoryId));
		if( rv == null )
		{
			rv = Collections.emptyList();
		}
		return rv;
	}

	@Override
	public Collection<NameValue> getCommunities(SectionInfo info)
	{
		Collection<NameValue> communities = OPTION_CACHE.getIfPresent("communities");
		if( communities == null )
		{
			communities = merlotService.getCommunities(repoWebService.getRemoteRepository(info));
			OPTION_CACHE.put("communities", communities);
		}
		return communities;
	}

	@Override
	public List<NameValue> getLanguages(SectionInfo info)
	{
		return new ArrayList<NameValue>(getLanguageMap(info).values());
	}

	private Map<String, NameValue> getLanguageMap(SectionInfo info)
	{
		Map<String, NameValue> languages = LANGUAGES_CACHE.getIfPresent("languages");
		if( languages == null )
		{
			languages = Maps.newHashMap();
			List<NameValue> langs = merlotService.getLanguages(repoWebService.getRemoteRepository(info));
			for( NameValue lang : langs )
			{
				languages.put(lang.getValue(), lang);
			}
			LANGUAGES_CACHE.put("languages", languages);
		}
		return languages;
	}

	@Override
	public Collection<NameValue> getMaterialTypes(SectionInfo info)
	{
		Collection<NameValue> materialTypes = OPTION_CACHE.getIfPresent("materialTypes");
		if( materialTypes == null )
		{
			materialTypes = merlotService.getMaterialTypes(repoWebService.getRemoteRepository(info));
			OPTION_CACHE.put("materialTypes", materialTypes);
		}
		return materialTypes;
	}

	@Override
	public Collection<NameValue> getTechnicalFormats(SectionInfo info)
	{
		Collection<NameValue> technicalFormats = OPTION_CACHE.getIfPresent("technicalFormats");
		if( technicalFormats == null )
		{
			technicalFormats = merlotService.getTechnicalFormats(repoWebService.getRemoteRepository(info));
			OPTION_CACHE.put("technicalFormats", technicalFormats);
		}
		return technicalFormats;
	}

	@Override
	public Collection<NameValue> getAudiences(SectionInfo info)
	{
		Collection<NameValue> audiences = OPTION_CACHE.getIfPresent("audiences");
		if( audiences == null )
		{
			audiences = merlotService.getAudiences(repoWebService.getRemoteRepository(info));
			OPTION_CACHE.put("audiences", audiences);
		}
		return audiences;
	}

	@Override
	public String lookupLanguage(SectionInfo info, String langCode)
	{
		NameValue nv = getLanguageMap(info).get(langCode);
		if( nv != null )
		{
			return nv.getName();
		}
		return null;
	}
}
