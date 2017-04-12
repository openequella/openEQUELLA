/*
 * Created on 1/12/2005
 */
package com.tle.beans.search;

@SuppressWarnings("nls")
public class SRWSettings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "SRWSearchEngine";

	private String url;
	private String schemaId;

	@Override
	protected String getType()
	{
		return SEARCH_TYPE;
	}

	@Override
	protected void _load()
	{
		super._load();
		url = get("url", url);
		schemaId = get("schema", schemaId);
	}

	@Override
	protected void _save()
	{
		super._save();
		put("url", url);
		put("schema", schemaId);
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getSchemaId()
	{
		return schemaId;
	}

	public void setSchemaId(String schemaId)
	{
		this.schemaId = schemaId;
	}
}
