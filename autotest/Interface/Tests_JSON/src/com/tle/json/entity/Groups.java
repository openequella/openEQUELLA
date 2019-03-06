package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class Groups extends BaseJSONCreator
{
	public static ObjectNode json(String uuid, String name, String... userIds)
	{
		ObjectNode group = mapper.createObjectNode();
		if( uuid != null )
		{
			group.put("id", uuid);
		}
		group.put("name", name);
		ArrayNode groups = group.putArray("users");
		for( String userId : userIds )
		{
			groups.add(userId);
		}
		return group;
	}

}
