package com.tle.core.payment;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Throwables;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;

@Bind
@Singleton
public class JsonMapper
{
	private ObjectMapper mapper;

	@Inject
	private ObjectMapperService objectMapperService;

	/**
	 * Serialise an object
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public String convertToJson(Object object) throws IOException
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		getMapper().writeValue(pw, object);
		return sw.toString();
	}

	/**
	 * Parse JSON from the response.
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public ObjectNode readJson(InputStream content) throws IOException
	{
		try
		{
			return (ObjectNode) getMapper().readTree(content);
		}
		finally
		{
			if( content != null )
			{
				content.close();
			}
		}
	}

	public ArrayNode readJsonToArray(InputStream content) throws IOException
	{
		try
		{
			return (ArrayNode) getMapper().readTree(content);
		}
		finally
		{
			if( content != null )
			{
				content.close();
			}
		}
	}

	public <T> T readObject(ObjectNode object, Class<T> clazz)
	{
		try
		{
			return getMapper().readValue(object.toString(), clazz);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Creates a blank JSON object.
	 * 
	 * @return A blank JSON object
	 */
	public ObjectNode createJson()
	{
		return getMapper().createObjectNode();
	}

	/**
	 * Retrieve an object node, or null if it doesn't exist
	 * 
	 * @param parentNode
	 * @param attr
	 * @return
	 */
	public ObjectNode getObject(JsonNode parentNode, String attr)
	{
		return (ObjectNode) parentNode.get(attr);
	}

	/**
	 * Retrieve a string node value, or defaultValue if the node does not exist.
	 * 
	 * @param parentNode
	 * @param attr
	 * @param defaultValue
	 * @return
	 */
	public String getString(JsonNode parentNode, String attr, String defaultValue)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return defaultValue;
		}
		return node.asText();
	}

	/**
	 * Retrieve a boolean node value, or defaultValue if the node does not
	 * exist.
	 * 
	 * @param parentNode
	 * @param attr
	 * @param defaultValue
	 * @return
	 */
	public boolean getBoolean(JsonNode parentNode, String attr, boolean defaultValue)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return defaultValue;
		}
		return node.asBoolean();
	}

	/**
	 * Retrieve a date node value
	 * 
	 * @param parentNode
	 * @param attr
	 * @return
	 */
	public Date getDate(JsonNode parentNode, String attr)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return null;
		}
		return ISO8601Utils.parse(node.asText());
	}

	/**
	 * Retrieve an integer node value, or defaultValue if the node does not
	 * exist.
	 * 
	 * @param parentNode
	 * @param attr
	 * @param defaultValue
	 * @return
	 */
	public int getInt(JsonNode parentNode, String attr, int defaultValue)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return defaultValue;
		}
		return node.asInt();
	}

	public synchronized ObjectMapper getMapper()
	{
		if( mapper == null )
		{
			mapper = objectMapperService.createObjectMapper("rest");
		}
		return mapper;
	}

}