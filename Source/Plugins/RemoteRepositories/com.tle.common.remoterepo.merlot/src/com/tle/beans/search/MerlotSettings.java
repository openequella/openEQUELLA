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
