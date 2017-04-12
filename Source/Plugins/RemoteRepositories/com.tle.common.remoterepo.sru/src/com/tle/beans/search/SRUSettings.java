/**
 * 
 */
package com.tle.beans.search;

/**
 * @author larry
 */
public class SRUSettings extends XmlBasedSearchSettings
{
	private static final String SEARCH_TYPE = "SRUSearchEngine";

	private String url;
	private String schemaId;

	/**
	 * @see com.tle.beans.search.SearchSettings#getType()
	 */
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
