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

package com.tle.beans.search;

import com.dytech.edge.common.Constants;
import com.tle.beans.entity.FederatedSearch;

@SuppressWarnings("nls")
public class MerlotSettings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "MerlotSearchEngine";

	private static final String LICENCE_KEY = "licenceKey";
	private static final String ADVANCED_API = "advanced";

	private String licenceKey;
	private boolean advancedApi;

	public MerlotSettings()
	{
		super();
	}

	public MerlotSettings(FederatedSearch gateway)
	{
		super(gateway);
	}

	@Override
	protected String getType()
	{
		return SEARCH_TYPE;
	}

	@Override
	protected void _load()
	{
		super._load();

		licenceKey = get(LICENCE_KEY, Constants.BLANK);
		advancedApi = get(ADVANCED_API, false);
	}

	@Override
	protected void _save()
	{
		super._save();

		put(LICENCE_KEY, licenceKey);
		put(ADVANCED_API, advancedApi);
	}

	public boolean isAdvancedApi()
	{
		return advancedApi;
	}

	public void setAdvancedApi(boolean advancedApi)
	{
		this.advancedApi = advancedApi;
	}

	public String getLicenceKey()
	{
		return licenceKey;
	}

	public void setLicenceKey(String licenceKey)
	{
		this.licenceKey = licenceKey;
	}
}
