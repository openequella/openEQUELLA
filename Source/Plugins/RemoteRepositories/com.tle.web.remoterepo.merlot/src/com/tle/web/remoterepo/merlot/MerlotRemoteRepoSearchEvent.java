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

import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.util.TleDate;
import com.tle.core.remoterepo.merlot.service.MerlotSearchParams;
import com.tle.web.remoterepo.event.RemoteRepoSearchEvent;
import com.tle.web.sections.SectionId;

@NonNullByDefault(false)
public class MerlotRemoteRepoSearchEvent extends RemoteRepoSearchEvent<MerlotRemoteRepoSearchEvent>
	implements
		MerlotSearchParams
{
	private KeywordUse keywordUse;
	private String category;
	private String community;
	private String language;
	private String materialType;
	private String technicalFormat;
	private String materialAudience;
	private String sort;
	private boolean cost;
	private boolean creativeCommons;
	private TleDate createdBefore;
	private TleDate createdAfter;
	private Set<String> mobileOS;
	private Set<String> mobileType;

	public MerlotRemoteRepoSearchEvent(SectionId sectionId, FederatedSearch search)
	{
		super(sectionId, search);
	}

	@Override
	public FederatedSearch getMerlotSearch()
	{
		return getSearch();
	}

	@Override
	public KeywordUse getKeywordUse()
	{
		return keywordUse;
	}

	public void setKeywordUse(KeywordUse keywordUse)
	{
		this.keywordUse = keywordUse;
	}

	@Override
	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	@Override
	public String getCommunity()
	{
		return community;
	}

	public void setCommunity(String community)
	{
		this.community = community;
	}

	@Override
	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	@Override
	public String getMaterialType()
	{
		return materialType;
	}

	public void setMaterialType(String materialType)
	{
		this.materialType = materialType;
	}

	@Override
	public String getTechnicalFormat()
	{
		return technicalFormat;
	}

	public void setTechnicalFormat(String technicalFormat)
	{
		this.technicalFormat = technicalFormat;
	}

	@Override
	public String getMaterialAudience()
	{
		return materialAudience;
	}

	public void setMaterialAudience(String materialAudience)
	{
		this.materialAudience = materialAudience;
	}

	@Override
	public String getSort()
	{
		return sort;
	}

	public void setSort(String sort)
	{
		this.sort = sort;
	}

	@Override
	public boolean isCost()
	{
		return cost;
	}

	public void setCost(boolean cost)
	{
		this.cost = cost;
	}

	@Override
	public boolean isCreativeCommons()
	{
		return creativeCommons;
	}

	public void setCreativeCommons(boolean creativeCommons)
	{
		this.creativeCommons = creativeCommons;
	}

	@Override
	public TleDate getCreatedBefore()
	{
		return createdBefore;
	}

	public void setCreatedBefore(TleDate createdBefore)
	{
		this.createdBefore = createdBefore;
	}

	@Override
	public TleDate getCreatedAfter()
	{
		return createdAfter;
	}

	public void setCreatedAfter(TleDate createdAfter)
	{
		this.createdAfter = createdAfter;
	}

	@Override
	public Set<String> getMobileOS()
	{
		return mobileOS;
	}

	public void setMobileOS(Set<String> mobileOS)
	{
		this.mobileOS = mobileOS;
	}

	@Override
	public Set<String> getMobileType()
	{
		return mobileType;
	}

	public void setMobileType(Set<String> mobileType)
	{
		this.mobileType = mobileType;
	}

}
