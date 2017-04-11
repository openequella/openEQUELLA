package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class Workflows extends BaseJSONCreator
{
	public static ObjectNode json(String name)
	{
		ObjectNode workflow = mapper.createObjectNode();
		workflow.put("name", name);
		workflow.put("root", serial("start"));
		return workflow;
	}

	public static ObjectNode serial(String uuid, String name, boolean rejectPoint)
	{
		ObjectNode node = node("s", uuid, name);
		if( rejectPoint )
		{
			node.put("rejectPoint", true);
		}
		return node;
	}

	public static ObjectNode parallel(String uuid, String name, boolean rejectPoint)
	{
		ObjectNode node = node("p", uuid, name);
		if( rejectPoint )
		{
			node.put("rejectPoint", true);
		}
		return node;
	}

	public static ObjectNode serial(String name)
	{
		return serial(null, name, false);
	}

	private static ObjectNode node(String type, String uuid, String name)
	{
		ObjectNode node = mapper.createObjectNode();
		if( uuid != null )
		{
			node.put("uuid", uuid);
		}
		node.put("name", name);
		node.put("type", type);
		return node;
	}

	public static ObjectNode task(String name, boolean allowEditing, String... users)
	{
		return task(null, name, allowEditing, users);
	}

	public static ObjectNode task(String uuid, String name, boolean allowEditing, String... users)
	{
		ObjectNode node = node("t", uuid, name);
		node.put("allowEditing", allowEditing);
		setUsers(node, users);
		return node;
	}

	public static ObjectNode child(ObjectNode parent, ObjectNode child)
	{
		ArrayNode nodes = (ArrayNode) parent.get("nodes");
		if( nodes == null )
		{
			nodes = parent.putArray("nodes");
		}
		nodes.add(child);
		return parent;
	}

	public static void setUsers(ObjectNode task, String... users)
	{
		ArrayNode userArray = task.putArray("users");
		for( String user : users )
		{
			userArray.add(user);
		}
	}

	public static void setGroups(ObjectNode task, String... groups)
	{
		ArrayNode userArray = task.putArray("groups");
		for( String group : groups )
		{
			userArray.add(group);
		}
	}

	public static void setRoles(ObjectNode task, String... roles)
	{
		ArrayNode userArray = task.putArray("roles");
		for( String group : roles )
		{
			userArray.add(group);
		}
	}

	public static void rootChild(ObjectNode workflow, ObjectNode child)
	{
		child(workflow.with("root"), child);
	}

}
