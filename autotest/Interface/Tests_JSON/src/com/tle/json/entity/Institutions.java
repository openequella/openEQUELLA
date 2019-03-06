package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class Institutions extends BaseJSONCreator
{
	public static ObjectNode json(String name, String password, String filestoreId, String url, boolean enabled)
	{
		ObjectNode institution = mapper.createObjectNode();
		institution.put("name", name);
		if( password != null )
		{
			institution.put("password", password);
		}
		if( filestoreId != null )
		{
			institution.put("filestoreId", filestoreId);
		}
		if( url != null )
		{
			institution.put("url", url);
		}
		institution.put("enabled", enabled);
		return institution;
	}

}
