package com.tle.webtests.framework.ant;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class PluginNodeDeserializer implements JsonDeserializer<PluginNode>, JsonSerializer<PluginNode>
{
	private final Type stringListType;

	public PluginNodeDeserializer()
	{
		stringListType = new TypeToken<List<String>>()
		{
		}.getType();
	}

	@Override
	@SuppressWarnings("nls")
	public PluginNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		throws JsonParseException
	{
		PluginNode node = new PluginNode();
		JsonObject object = json.getAsJsonObject();
		Set<Entry<String, JsonElement>> entries = object.entrySet();
		List<PluginNode> children = Lists.newArrayList();
		for( Entry<String, JsonElement> entry : entries )
		{
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			if( key.equals("plugins") )
			{
				List<String> plugins = context.deserialize(value, stringListType);
				node.setPlugins(plugins);
			}
			else
			{
				PluginNode childNode = context.deserialize(value, PluginNode.class);
				childNode.setName(key);
				children.add(childNode);
			}
		}
		node.setChildren(children);
		return node;
	}

	@SuppressWarnings("nls")
	@Override
	public JsonElement serialize(PluginNode node, Type type, JsonSerializationContext context)
	{
		JsonObject jsonObject = new JsonObject();
		List<String> plugins = node.getPlugins();
		if( plugins != null )
		{
			Collections.sort(plugins);
			jsonObject.add("plugins", context.serialize(plugins));
		}
		List<PluginNode> children = node.getChildren();
		for( PluginNode childNode : children )
		{
			jsonObject.add(childNode.getName(), context.serialize(childNode));
		}
		return jsonObject;
	}
}