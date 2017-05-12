package com.tle.webtests.test.webservices.rest;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

import com.tle.common.Pair;
import com.tle.webtests.pageobject.viewitem.ItemId;

public class CommentApiTest extends AbstractItemApiTest
{

	private static final String OAUTH_CLIENT_ID = "CommentApiTestClient12";

	private ObjectNode createCommentJson(String commentText, int rating, boolean anonymous)
	{
		ObjectNode comment = mapper.createObjectNode();
		comment.put("comment", commentText);
		comment.put("rating", rating);
		comment.put("anonymous", anonymous);
		return comment;
	}
	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
	}

	private ItemId createSimpleItem() throws IOException
	{
		String token = getToken();
		ObjectNode item = createItemJson(COLLECTION_ATTACHMENTS);
		item = createItem(item.toString(), token);
		asserter.assertStatus(item, "live");
		return addDeletable(item);
	}

	private JsonNode getComments(String uuid, int version)
	{
		String apiUrl = context.getBaseUrl() + "api/item/" + uuid + "/" + version + "/comment";
		InputStream content = null;

		try
		{
			HttpResponse response = execute(new HttpGet(apiUrl), false, getToken());
			content = response.getEntity().getContent();
			return mapper.readTree(content);
		}
		catch( Exception ex )
		{
			System.out.println(ex.toString());
			System.out.println(ex.getMessage());
			return null;
		}
		finally
		{
			if( content != null )
			{
				// content.close();
			}
		}
	}

	private HttpResponse getComment(String uuid, int version, String commentUuid) throws IOException
	{
		String apiUrl = context.getBaseUrl() + "api/item/" + uuid + "/" + version + "/comment/" + commentUuid;

		final HttpGet request = new HttpGet(apiUrl);
		return execute(request, true, getToken());
	}

	private HttpResponse createComment(ItemId itemid, String commentText, int commentRating, boolean isAnonymous)
		throws ClientProtocolException, IOException
	{
		String apiUrl = context.getBaseUrl() + "api/item/" + itemid.getUuid() + "/" + itemid.getVersion() + "/comment";
		return postEntity(createCommentJson(commentText, commentRating, isAnonymous).toString(), apiUrl, getToken(), true);
	}

	private HttpResponse deleteComment(ItemId itemid, String uuid) throws ClientProtocolException, IOException
	{
		String apiUrl = context.getBaseUrl() + "api/item/" + itemid.getUuid() + "/" + itemid.getVersion() + "/comment";
		HttpResponse response = deleteResource(apiUrl, getToken(), "commentuuid", uuid);
		EntityUtils.consume(response.getEntity());
		return response;
	}

	@Test
	public void createCommentTest() throws Exception
	{
		ItemId itemid = createSimpleItem();

		final String commentText = "This is my commment";
		final int commentRating = 2;
		final boolean isAnonymous = true;
		assertResponse(createComment(itemid, commentText, commentRating, isAnonymous), 201, "error");
		JsonNode comments = getComments(itemid.getUuid(), itemid.getVersion());
		JsonNode comment = comments.get(0);

		assertEquals(comment.get("comment").getTextValue(), commentText);
		assertEquals(comment.get("rating").getIntValue(), commentRating);
		assertEquals(comment.get("anonymous").getBooleanValue(), isAnonymous);
	}

	@Test
	public void deleteCommentTest() throws Exception
	{
		ItemId itemid = createSimpleItem();
		final String commentText = "This is my commment2";
		final int commentRating = 2;
		final boolean isAnonymous = true;
		String commentUuid = "";

		assertResponse(createComment(itemid, commentText, commentRating, isAnonymous), 201, "error");

		JsonNode comments = getComments(itemid.getUuid(), itemid.getVersion());
		JsonNode comment = comments.get(0);
		commentUuid = comment.get("uuid").getTextValue();

		HttpResponse deleteResponse = deleteComment(itemid, commentUuid);
		assertResponse(deleteResponse, 200, "error");

		HttpResponse resp = getComment(itemid.getUuid(), itemid.getVersion(), commentUuid);
		assertResponse(resp, 404, "error");

		assertEquals(true, true);

	}

}
