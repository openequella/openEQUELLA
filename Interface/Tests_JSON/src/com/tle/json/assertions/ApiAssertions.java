package com.tle.json.assertions;

import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tle.common.Utils;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.PageContext;

public class ApiAssertions
{
	public ApiAssertions(PageContext context)
	{
		// whatevs
	}

	public void assertStatus(ObjectNode item, String status)
	{
		Assert.assertEquals(item.get("status").asText(), status);
	}

	public void assertBasic(ObjectNode tree, String name, String description)
	{
		Assert.assertEquals(tree.get("name").asText(), name);
		JsonNode descriptionNode = tree.get("description");
		if( description != null )
		{
			Assert.assertEquals(descriptionNode.asText(), description);
		}
		else
		{
			Assert.assertNull(descriptionNode);
		}
	}

	public JsonNode assertNavNode(JsonNode parentNode, int index, String uuid, String name, String[][] tabs)
	{
		JsonNode childNode = parentNode.get(index);
		Assert.assertEquals(childNode.get("uuid").textValue(), uuid);
		Assert.assertEquals(childNode.get("name").textValue(), name);
		JsonNode tabsNode = childNode.get("tabs");
		int tabIndex = 0;
		for( String[] tab : tabs )
		{
			JsonNode tab1 = tabsNode.get(tabIndex);
			Assert.assertEquals(tab1.get("name").textValue(), tab[0]);
			Assert.assertEquals(tab1.get("attachment").get("$ref").textValue(), tab[1]);
			JsonNode viewer = tab1.get("viewer");
			if( viewer == null && tab[2] != null )
			{
				Assert.assertTrue(tab[2].isEmpty());
			}
			else
			{
				Assert.assertEquals(viewer.textValue(), tab[2]);
			}
			tabIndex++;
		}
		return childNode.get("nodes");
	}

	public JsonNode assertNavNode(JsonNode parentNode, int index, String uuid, String name, String tabName,
		String attachment, String viewer)
	{
		return assertNavNode(parentNode, index, uuid, name, new String[][]{{tabName, attachment, viewer}});
	}

	public void assertDetails(ObjectNode tree, String status, double rating, Object created, Object modified,
		String ownerId, String collectionUuid)
	{
		Assert.assertEquals(tree.get("status").textValue(), status);
		// TODO: Not supported by CreateFromScratchTest
		/*
		 * assertEquals(tree.get("rating").getDoubleValue(), rating);
		 * assertDate(tree.get("createdDate"), created);
		 * assertDate(tree.get("modifiedDate"), modified);
		 */
		assertUser(tree.get("owner"), ownerId);
		assertCollection(tree.get("collection"), collectionUuid);
	}

	public void assertHistory(JsonNode historyNode, String userId, Object time, String type, String state)
	{
		assertUser(historyNode.path("user"), userId);
		assertDate(historyNode.path("date"), time);
		Assert.assertEquals(historyNode.path("type").textValue(), type);
		Assert.assertEquals(historyNode.path("state").textValue(), state);
	}

	public void assertAttachmentBasics(JsonNode attachment, ItemId itemId, String type, String uuid, String description)
	{
		Assert.assertEquals(attachment.get("type").textValue(), type);
		String actualUuid = attachment.get("uuid").asText();
		if( uuid != null )
		{
			Assert.assertEquals(actualUuid, uuid);
		}
		Assert.assertEquals(attachment.get("description").textValue(), description);
		JsonNode linksNode = attachment.get("links");
		// assertLink(linksNode, "view", context.getBaseUrl() + "items/" +
		// itemId + "/?attachment.uuid=" + actualUuid);
	}

	public void assertViewerAndPreview(JsonNode attachment, String viewer, boolean preview)
	{
		JsonNode viewerNode = attachment.get("viewer");
		if( viewer == null )
		{
			Assert.assertNull(viewerNode);
		}
		else
		{
			Assert.assertEquals(viewerNode.asText(), viewer);
		}
		Assert.assertEquals(attachment.get("preview").asBoolean(), preview);
	}

	public void assertLink(JsonNode linksNode, String rel, String href)
	{
		// url may end in a harmless "/"
		String url = linksNode.get(rel).textValue();
		if( url.endsWith("/") )
		{
			url = Utils.safeSubstring(url, 0, -1);
		}
		Assert.assertEquals(url, href);
	}

	public void assertUser(JsonNode userNode, String ownerId)
	{
		Assert.assertEquals(userNode.get("id").textValue(), ownerId);
	}

	public void assertCollection(JsonNode userNode, String collectionUuid)
	{
		Assert.assertEquals(userNode.get("uuid").textValue(), collectionUuid);
	}

	public void assertDate(JsonNode dateNode, Object time)
	{
		Date parsed = ISO8601Utils.parse(dateNode.textValue());
		long timeMillis;
		if( time instanceof String )
		{
			timeMillis = ISO8601Utils.parse((String) time).getTime();
		}
		else if( time instanceof Date )
		{
			timeMillis = ((Date) time).getTime();
		}
		else
		{
			timeMillis = ((Number) time).longValue();
		}
		if( timeMillis / 1000 != parsed.getTime() / 1000 )
		{
			Assert.assertEquals(dateNode.textValue(),
				ISO8601Utils.format(new Date(timeMillis), true, TimeZone.getTimeZone("America/Chicago")));
		}
	}

	public void assertComment(JsonNode commentNode, String uuid, int rating, String userId, boolean anonymous,
		String comment, Object time)
	{
		Assert.assertEquals(commentNode.get("uuid").textValue(), uuid);
		Assert.assertEquals(commentNode.get("rating").intValue(), rating);
		if( userId != null )
		{
			assertUser(commentNode.get("postedBy"), userId);
		}
		Assert.assertEquals(commentNode.get("anonymous").booleanValue(), anonymous);
		Assert.assertEquals(commentNode.get("comment").textValue(), comment);
		assertDate(commentNode.get("postedDate"), time);
	}

	public void assertLock(JsonNode lock, String owner, String uuid)
	{
		Assert.assertEquals(lock.path("uuid").asText(), uuid);
		Assert.assertEquals(lock.path("owner").path("id").asText(), owner);
	}
}
