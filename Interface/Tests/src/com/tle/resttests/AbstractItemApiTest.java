package com.tle.resttests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.common.URLUtils;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.Waiter;

/**
 * @author Aaron
 */
public abstract class AbstractItemApiTest extends AbstractRestApiTest
{
	private List<ItemId> toDelete = Lists.newArrayList();
	private String namePrefix;
	protected static final String COLLECTION_ATTACHMENTS = "9a1ddb24-6bf5-db3d-d8fe-4fca20ecf69c";
	protected static final String COLLECTION_PERMISSIONS = "a0efe7b5-f670-44b8-a84e-c6e2f42c909a";
	protected static final String COLLECTION_MODERATE = "3d31ac33-261e-404c-a157-487e51716268";
	protected static final String COLLECTION_SAVESCRIPT = "c7194cd0-f586-49b6-9fcc-4b1c5237efd9";
	protected static final String COLLECTION_NOTIFICATIONS = "4b492e6a-bfa7-48ed-8d6e-71d8763e4e78";
	protected static final String COLLECTION_BASIC = "b28f1ffe-2008-4f5e-d559-83c8acd79316";

	public AbstractItemApiTest()
	{
		this.namePrefix = getClass().getSimpleName();
	}

	@Override
	protected String getNamePrefix()
	{
		return namePrefix;
	}

	protected String[] copyStaging(ItemId itemId, String token) throws IOException
	{
		HttpResponse stagingResponse = execute(
			new HttpPost(appendQueryString(context.getBaseUrl() + "api/item/copy",
				queryString("uuid", itemId.getUuid(), "version", Integer.toString(itemId.getVersion())))), true, token);
		try
		{
			assertResponse(stagingResponse, 201, "201 not returned from staging creation");
		}
		finally
		{
			EntityUtils.consume(stagingResponse.getEntity());
		}
		ObjectNode stagingJson = (ObjectNode) getEntity(stagingResponse.getLastHeader("Location").getValue(), token);
		String stagingUuid = stagingJson.get("uuid").asText();
		String stagingDirUrl = stagingJson.get("links").get("self").asText();
		return new String[]{stagingUuid, stagingDirUrl};
	}

	protected String[] createStaging(String token) throws IOException
	{
		HttpResponse stagingResponse = execute(new HttpPost(context.getBaseUrl() + "api/staging"), true, token);
		assertResponse(stagingResponse, 201, "201 not returned from staging creation");
		ObjectNode stagingJson = (ObjectNode) getEntity(stagingResponse.getLastHeader("Location").getValue(), token);
		String stagingUuid = stagingJson.get("uuid").asText();
		String stagingDirUrl = stagingJson.get("links").get("self").asText();
		return new String[]{stagingUuid, stagingDirUrl};
	}

	protected void uploadFile(String stagingDirUrl, String filename, URL resource, Object... params) throws IOException
	{
		String avatarUrl = stagingDirUrl + '/' + com.tle.common.URLUtils.urlEncode(filename);
		HttpPut putfile = new HttpPut(appendQueryString(avatarUrl, queryString(params)));
		URLConnection file = resource.openConnection();
		InputStreamEntity inputStreamEntity = new InputStreamEntity(file.getInputStream(), file.getContentLength());
		inputStreamEntity.setContentType("application/octet-stream");
		putfile.setEntity(inputStreamEntity);
		HttpResponse putfileResponse = execute(putfile, true);
		assertResponse(putfileResponse, 200, "200 not returned from file upload");
	}

	protected JsonNode getItemJson(String itemUri, String info, String token) throws IOException
	{
		return getEntity(itemUri, token, "info", info);
	}

	protected JsonNode getItemSubResource(String itemId, int version, String subResource, String token,
		Object... params) throws IOException
	{
		return getEntity(getItemUri(new ItemId(itemId, version)) + "/" + subResource, token, params);
	}

	protected ObjectNode getItem(String uuid, int version, String info, String token) throws IOException
	{
		return (ObjectNode) getItemJson(getItemUri(uuid, String.valueOf(version)), info, token);
	}

	protected ObjectNode getItem(ItemId itemId, String info, String token) throws IOException
	{
		return (ObjectNode) getItemJson(getItemUri(itemId), info, token);
	}

	protected ObjectNode getItem(String itemUri, String info, String token) throws IOException
	{
		return (ObjectNode) getItemJson(itemUri, info, token);
	}

	protected ArrayNode getItems(String uuid, String version, String info, String token) throws IOException
	{
		return getItems(getItemUri(uuid, version), info, token);
	}

	protected ArrayNode getItems(String itemUri, String info, String token) throws IOException
	{
		return (ArrayNode) getItemJson(itemUri, info, token);
	}

	protected String getItemUri(ItemId itemId)
	{
		return getItemUri(itemId.getUuid(), String.valueOf(itemId.getVersion()));
	}

	protected String getItemUri(String uuid, String version)
	{
		StringBuilder sbuf = new StringBuilder();
		sbuf.append(context.getBaseUrl());
		sbuf.append("api/item/");
		sbuf.append(uuid);
		sbuf.append('/');
		sbuf.append(version);
		return sbuf.toString();
	}

	protected ObjectNode createItem(String json, String token, Object... paramNameValues) throws IOException
	{
		HttpResponse response = postItem(json, token, paramNameValues);
		assertResponse(response, 201, "Should have created the item");
		String itemUri = response.getFirstHeader("Location").getValue();
		return getItem(itemUri, null, token);
	}

	protected HttpResponse postItem(String json, String token, Object... paramNameValues) throws IOException
	{
		return postEntity(json, context.getBaseUrl() + "api/item", token, true, paramNameValues);
	}

	protected HttpResponse postItem(String json, boolean consume, String token, Object... paramNameValues)
		throws IOException
	{
		return postEntity(json, context.getBaseUrl() + "api/item", token, consume, paramNameValues);
	}

	protected HttpResponse deleteItem(String uuid, int version, String token, Object... paramNameValues)
		throws IOException
	{
		return deleteResource(getItemUri(uuid, Integer.toString(version)), token, paramNameValues);
	}

	protected HttpResponse itemAction(String itemUri, String action, String token, Object... paramNameValues)
		throws ClientProtocolException, IOException
	{
		HttpPost request = new HttpPost(itemUri + "/action/" + action + "?" + queryString(paramNameValues));
		return execute(request, true, token);
	}

	protected HttpResponse taskAction(ItemId itemId, String taskId, String action, String token,
		Object... paramNameValues) throws ClientProtocolException, IOException
	{
		HttpPost request = new HttpPost(getItemUri(itemId) + "/task/" + taskId + '/' + action + "?"
			+ queryString(paramNameValues));
		return execute(request, true, token);
	}

	protected ObjectNode editItem(ObjectNode item, String token, Object... paramNameValues) throws IOException
	{
		ItemId itemId = new ItemId(item.get("uuid").asText(), item.get("version").asInt());
		String itemUri = getItemUri(itemId);
		assertResponse(putItem(itemUri, item.toString(), token, paramNameValues), 200, "Should have been able to edit");
		return getItem(itemUri, null, token);
	}

	protected HttpResponse putItem(String uuid, int version, String json, String token, Object... paramNameValues)
		throws Exception
	{
		return putItem(getItemUri(uuid, String.valueOf(version)), json, token, paramNameValues);
	}

	protected ObjectNode putItemError(String uuid, int version, String json, String token, int responseCode,
		Object... paramNameValues) throws Exception
	{
		return putItemError(getItemUri(uuid, String.valueOf(version)), json, token, responseCode, paramNameValues);
	}

	protected HttpResponse putItem(ItemId itemId, String json, String token, Object... paramNameValues)
		throws IOException
	{
		return putItem(getItemUri(itemId), json, token, paramNameValues);
	}

	protected HttpResponse putItem(String itemUri, String json, String token, Object... paramNameValues)
		throws IOException
	{
		return putEntity(json, itemUri, token, true, paramNameValues);
	}

	protected ObjectNode putItemError(String itemUri, String json, String token, int responseCode,
		Object... paramNameValues) throws Exception
	{
		final HttpPut request = new HttpPut(appendQueryString(itemUri, queryString(paramNameValues)));
		final StringEntity ent = new StringEntity(json, "UTF-8");
		ent.setContentType("application/json");
		request.setEntity(ent);
		HttpResponse response = execute(request, false, token);
		try
		{
			assertResponse(response, responseCode, "Incorrect response code");
			ObjectNode error = (ObjectNode) mapper.readTree(response.getEntity().getContent());
			return error;
		}
		finally
		{
			EntityUtils.consume(response.getEntity());
		}
	}

	protected ObjectNode postItemError(String json, String token, int responseCode, Object... paramNameValues)
		throws Exception
	{
		final HttpPost request = new HttpPost(appendQueryString(context.getBaseUrl() + "api/item",
			queryString(paramNameValues)));
		final StringEntity ent = new StringEntity(json, "UTF-8");
		ent.setContentType("application/json");
		request.setEntity(ent);
		HttpResponse response = execute(request, false, token);
		ObjectNode error = (ObjectNode) mapper.readTree(response.getEntity().getContent());
		assertResponse(response, responseCode, "Incorrect response code");
		return error;
	}

	protected void assertNameVersionStatus(JsonNode itemNode, String name, int version, String status)
	{
		assertEquals(itemNode.get("name").textValue(), name);
		assertEquals(itemNode.get("version").intValue(), version);
		assertEquals(itemNode.get("status").textValue(), status);
	}

	protected void assertNulls(JsonNode tree, String... nodes)
	{
		for( String node : nodes )
		{
			assertNull(tree.get(node));
		}
	}

	protected void assertMetadata(JsonNode tree, String... pathsAndValues)
	{
		PropBagEx metaXml = new PropBagEx(tree.get("metadata").textValue());
		for( int i = 0; i < pathsAndValues.length; i += 2 )
		{
			String path = pathsAndValues[i];
			String value = pathsAndValues[i + 1];
			assertEquals(metaXml.getNode(path), value);
		}
	}

	protected void assertLinks(JsonNode tree, ItemId itemId)
	{
		JsonNode linksNode = tree.get("links");
		asserter.assertLink(linksNode, "self", getItemUri(itemId));
		// TODO do we have one?
		// asserter.assertLink(linksNode, "view", context.getBaseUrl() +
		// "items/" + itemId + "/");
	}

	// TODO: add params to all below

	protected void assertUrlAttachment(JsonNode urlAttachment, ItemId itemId)
	{
		assertUrlAttachment(urlAttachment, itemId, "Google", "http://google.com.au/");
	}

	protected void assertUrlAttachment(JsonNode urlAttachment, ItemId itemId, String title, String url)
	{
		asserter.assertAttachmentBasics(urlAttachment, itemId, "url", "32a79ea6-8b67-4b38-af85-341b2d512f09", title);
		assertEquals(urlAttachment.get("url").textValue(), url);
		assertEquals(urlAttachment.get("disabled").booleanValue(), false);
		// EQUELLA view URL is the web UI
		// asserter.assertLink(urlAttachment.get("links"), "view", url);
	}

	protected ItemId addDeletable(ObjectNode item)
	{
		ItemId itemId = new ItemId(item.get("uuid").asText(), item.get("version").asInt());
		toDelete.add(itemId);
		return itemId;
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		deleteItems();
		super.cleanupAfterClass();
	}

	protected void deleteItems() throws IOException
	{
		for( ItemId itemId : toDelete )
		{
			deleteItem(itemId.getUuid(), itemId.getVersion(), getToken(), "purge", true);
		}
		toDelete.clear();
	}

	protected ObjectNode createItemJson(String collection)
	{
		return createItemJson(collection, null);
	}

	protected ObjectNode createItemJson(String collection, String metadata)
	{
		ObjectNode item = mapper.createObjectNode();
		item.with("collection").put("uuid", collection);
		if( metadata != null )
		{
			item.put("metadata", metadata);
		}
		return item;
	}

	protected ObjectNode createItemJsonWithValues(String collection, String... metadataNvs)
	{
		ObjectNode item = mapper.createObjectNode();
		item.with("collection").put("uuid", collection);
		PropBagEx metadata = new PropBagEx();
		for( int i = 0; i < metadataNvs.length; i++ )
		{
			metadata.setNode(metadataNvs[i], metadataNvs[++i]);
		}
		item.put("metadata", metadata.toString());
		return item;
	}

	protected void waitForIndex(final String itemName, final String uuid) throws IOException
	{
		waitForIndex(itemName, uuid, false);
	}

	protected void waitForIndex(final String itemName, final String uuid, boolean all) throws IOException
	{
		List<String> names = Lists.newArrayList(itemName);
		List<String> uuids = Lists.newArrayList(uuid);

		waitForIndex(names, uuids, all);
	}

	protected JsonNode basicSearch(String query, String token, boolean showAll) throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("q", query));
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(context.getBaseUrl() + "api/search?showall=" + showAll + "&" + paramString);
		HttpResponse response = execute(get, false, token);
		return mapper.readTree(response.getEntity().getContent());
	}

	protected JsonNode basicSearch(String query, String token) throws Exception
	{
		return basicSearch(query, token, false);
	}

	protected void waitForIndex(final Collection<String> itemNames, final Collection<String> uuids, final boolean all)
		throws IOException
	{
		final String token = getToken();
		StringBuilder builder = new StringBuilder();

		for( String name : itemNames )
		{
			builder.append(name + " ");
		}
		final String combinedNames = builder.toString();

		Waiter<String> indexWaiter = new Waiter<String>("").withTimeout(20, TimeUnit.SECONDS);
		indexWaiter.until(new Predicate<String>()
		{
			@Override
			public boolean apply(String driver)
			{
				try
				{
					HttpGet get = new HttpGet(context.getBaseUrl() + "api/search?showall=" + String.valueOf(all)
						+ "&length=50&q=" + URLUtils.urlEncode(combinedNames));
					HttpResponse response = execute(get, false, token);

					int i = 0;

					JsonNode all = mapper.readTree(response.getEntity().getContent());
					for( JsonNode item : all.get("results") )
					{
						for( String uuid : uuids )
						{
							if( item.get("uuid").asText().equals(uuid) )
							{
								i++;
							}
						}
					}
					return i == uuids.size();
				}
				catch( Exception e )
				{
					e.printStackTrace();
					Throwables.propagate(e);
				}
				return false;
			}
		});
	}
}
