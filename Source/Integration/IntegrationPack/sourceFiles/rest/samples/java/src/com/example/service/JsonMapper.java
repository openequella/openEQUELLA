/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * Utility for manipulating JSON objects
 */
public class JsonMapper
{
	private static final ObjectMapper mapper = new ObjectMapper();
	static
	{
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
	}

	private JsonMapper()
	{
	}

	/**
	 * Parse JSON from the response.
	 * 
	 * @param response
	 * @return
	 * @throws IOException
	 */
	public static ObjectNode readJson(HttpResponse response) throws IOException
	{
		InputStream content = null;
		try
		{
			content = response.getEntity().getContent();
			return (ObjectNode) mapper.readTree(content);
		}
		finally
		{
			if( content != null )
			{
				content.close();
			}
		}
	}

	/**
	 * Creates a blank JSON object.
	 * 
	 * @return A blank JSON object
	 */
	public static ObjectNode createJson()
	{
		return mapper.createObjectNode();
	}

	/**
	 * Retrieve an object node, or null if it doesn't exist
	 * 
	 * @param parentNode
	 * @param attr
	 * @return
	 */
	public static ObjectNode getObject(JsonNode parentNode, String attr)
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
	public static String getString(JsonNode parentNode, String attr, String defaultValue)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return defaultValue;
		}
		return node.getValueAsText();
	}

	/**
	 * Retrieve a date node value
	 * 
	 * @param parentNode
	 * @param attr
	 * @return
	 */
	public static Date getDate(JsonNode parentNode, String attr)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return null;
		}
		return ISO8601Utils.parse(node.getValueAsText());
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
	public static int getInt(JsonNode parentNode, String attr, int defaultValue)
	{
		JsonNode node = parentNode.get(attr);
		if( node == null )
		{
			return defaultValue;
		}
		return node.getValueAsInt();
	}
}
