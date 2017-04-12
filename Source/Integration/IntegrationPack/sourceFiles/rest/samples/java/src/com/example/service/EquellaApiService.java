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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.example.util.StringUtils;
import com.example.util.UrlUtils;
import com.example.util.XmlDocument;

/**
 * A service which interfaces with the EQUELLA API to search, retrieve and
 * modify resources
 */
public class EquellaApiService
{
	private EquellaApiService()
	{
	}

	/**
	 * Search for resources on the EQUELLA server.
	 * 
	 * @param query
	 * @param collectionUuids
	 * @param where
	 * @param start
	 * @param length
	 * @param order
	 * @param reverse
	 * @param showAll
	 * @param info
	 * @param token
	 * @return
	 */
	public static ObjectNode search(String query, String[] collectionUuids, String where, int start, int length,
		String order, boolean reverse, boolean showAll, String info, String token)
	{
		final List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
		if( !StringUtils.isEmpty(query) )
		{
			apiParams.add(new BasicNameValuePair("q", query));
		}
		if( !StringUtils.isEmpty(where) )
		{
			apiParams.add(new BasicNameValuePair("where", where));
		}
		apiParams.add(new BasicNameValuePair("start", Integer.toString(start)));
		apiParams.add(new BasicNameValuePair("length", Integer.toString(length)));
		if( !StringUtils.isEmpty(order) )
		{
			apiParams.add(new BasicNameValuePair("order", order));
		}
		apiParams.add(new BasicNameValuePair("showall", Boolean.toString(showAll)));
		apiParams.add(new BasicNameValuePair("reverse", Boolean.toString(reverse)));
		if( collectionUuids != null && collectionUuids.length > 0 )
		{
			apiParams.add(new BasicNameValuePair("collections", StringUtils.join(collectionUuids, ",")));
		}

		try
		{
			return JsonMapper.readJson(WebClient.execute(
				WebClient.createGET(Config.getEquellaUrl() + "api/search", apiParams), false, token));
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieve a resource from the EQUELLA server.
	 * 
	 * @param uuid
	 * @param version
	 * @param info
	 * @param token
	 * @return The JSON representation of the resource, or null if the resource
	 *         is not found.
	 */
	public static ObjectNode getResource(String uuid, int version, String info, String token)
	{
		try
		{
			final HttpResponse response = WebClient.execute(WebClient.createGET(Config.getEquellaUrl() + "api/item/"
				+ uuid + "/" + version + "?info=" + info, null), false, token);
			if( response.getStatusLine().getStatusCode() == 404 )
			{
				EntityUtils.consume(response.getEntity());
				return null;
			}
			return JsonMapper.readJson(response);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieve a resource from the EQUELLA server from a supplied API resource
	 * URL
	 * 
	 * @param location
	 * @param info
	 * @param token
	 * @return
	 */
	public static ObjectNode getResourceByLocation(String location, String info, String token)
	{
		try
		{
			final HttpResponse response = WebClient.execute(
				WebClient.createGET(UrlUtils.appendQueryString(location, "info=" + info), null), false, token);
			if( response.getStatusLine().getStatusCode() == 404 )
			{
				EntityUtils.consume(response.getEntity());
				return null;
			}
			return JsonMapper.readJson(response);
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates JSON for a new resource.
	 * 
	 * @param collectionUuid The collection of the new resource
	 * @return A JSON object with certain data pre-populated
	 */
	public static ObjectNode createBlankResource(String collectionUuid)
	{
		final ObjectNode resource = JsonMapper.createJson();
		resource.with("collection").put("uuid", collectionUuid);
		return resource;
	}

	/**
	 * Creates as resource, or updates an existing resource, on the EQUELLA
	 * server.
	 * 
	 * @param resourceJson
	 * @param uuid If this is blank then a new resource is assumed
	 * @param version
	 * @param name
	 * @param description
	 * @param token
	 * @return Returns the location of the new/edited resource
	 */
	public static String saveResource(ObjectNode resourceJson, String uuid, int version, String name,
		String description, List<FileItem> files, String token)
	{
		// Need to put name and description directly into the XML metadata
		// since the name and the description are read-only in the JSON

		final XmlDocument xmlDoc = new XmlDocument(JsonMapper.getString(resourceJson, "metadata", "<xml/>"));
		xmlDoc.createNodeFromXPath(Config.getNameXpath(), name);
		xmlDoc.createNodeFromXPath(Config.getDescriptionXpath(), description);
		resourceJson.put("metadata", xmlDoc.toString());

		// Upload and attach files
		final String fileAreaId = uploadFiles(resourceJson, files, token);

		// Send it off to EQUELLA
		try
		{
			// Add the file area parameter to the save so that EQUELLA knows
			// where the files for this resource are
			final List<NameValuePair> params = new ArrayList<NameValuePair>();
			if( fileAreaId != null )
			{
				params.add(new BasicNameValuePair("file", fileAreaId));
			}

			// When creating new resources over REST you should use POST
			// When updating resources with known URLs over REST you should use
			// PUT
			final HttpEntityEnclosingRequestBase putOrPost;
			if( uuid == null )
			{
				// New resource
				putOrPost = WebClient.createPOST(Config.getEquellaUrl() + "api/item", params);
			}
			else
			{
				// Edit resource
				putOrPost = WebClient.createPUT(Config.getEquellaUrl() + "api/item/" + uuid + '/' + version, params);
			}

			final StringEntity ent = new StringEntity(resourceJson.toString());
			ent.setContentType("application/json");
			putOrPost.setEntity(ent);
			final HttpResponse response = WebClient.execute(putOrPost, true, token);
			return response.getFirstHeader("Location").getValue();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Uploads file content to the temporary file area on the EQUELLA server.
	 * Also creates attachments on the resource to link to these files.
	 * 
	 * @param resourceJson
	 * @param files
	 * @param token
	 * @return
	 */
	private static String uploadFiles(ObjectNode resourceJson, List<FileItem> files, String token)
	{
		// If the files are non-empty then we need to create a files area on the
		// EQUELLA server
		if( files != null && files.size() > 0 )
		{
			try
			{
				// Create file area and retrieve the information
				final ObjectNode fileAreaInfo = JsonMapper.readJson(WebClient.execute(
					WebClient.createPOST(Config.getEquellaUrl() + "api/file", null), false, token));

				// Get the content upload URL for this file area
				final String fileContentUrl = fileAreaInfo.get("links").get("content").getValueAsText();

				// Post file contents, create an attachment on the resource for
				// each uploaded file
				ArrayNode attachmentsArray = (ArrayNode) resourceJson.get("attachments");
				if( attachmentsArray == null )
				{
					attachmentsArray = resourceJson.putArray("attachments");
				}
				for( FileItem file : files )
				{
					final ObjectNode attachmentNode = attachmentsArray.addObject();
					attachmentNode.put("filename", file.getName());
					attachmentNode.put("type", "file");

					// Note: filename needs to be URL path encoded (e.g. it may
					// contains spaces)
					final HttpPut put = WebClient.createPUT(
						fileContentUrl + "/" + UrlUtils.urlPathEncode(file.getName()), null);
					final InputStreamEntity inputStreamEntity = new InputStreamEntity(file.getInputStream(),
						file.getSize());
					inputStreamEntity.setContentType("application/octet-stream");
					put.setEntity(inputStreamEntity);
					WebClient.execute(put, true, token);
				}

				// Return the ID of the file area
				return fileAreaInfo.get("uuid").getValueAsText();
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
