package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class Roles extends BaseJSONCreator
{
	public static ObjectNode json(String uuid, String name, String expression)
	{
		ObjectNode role = mapper.createObjectNode();
		if( uuid != null )
		{
			role.put("id", uuid);
		}
		role.put("name", name);
		role.put("expression", expression);
		return role;
	}

}
