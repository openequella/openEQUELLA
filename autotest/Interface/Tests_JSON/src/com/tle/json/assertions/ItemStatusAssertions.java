package com.tle.json.assertions;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;

public class ItemStatusAssertions
{
	public enum Status
	{
		I, C, W, A
	}

	public static void assertComment(JsonNode node, int commentIndex, String message, String userId, String type)
	{
		JsonNode comment = node.get("comments").get(commentIndex);
		if( message != null )
		{
			assertEquals(comment.get("message").asText(), message);
		}
		assertEquals(comment.path("user").get("id").asText(), userId);
		assertEquals(comment.get("type").asText(), type);
	}

	public static void assertRejection(JsonNode moderation, String message, String userId)
	{
		assertEquals(moderation.path("status").asText(), "rejected");
		assertEquals(moderation.path("rejectedMessage").asText(), message);
		assertEquals(moderation.path("rejectedBy").get("id").asText(), userId);
	}

	public static Function<ObjectNode, Boolean> statusIs(final String status)
	{
		return new Function<ObjectNode, Boolean>()
		{
			@Override
			public Boolean apply(ObjectNode moderation)
			{
				return moderation.get("status").asText().equals(status);
			}
		};
	}

	public static void assertStatus(JsonNode moderation, String status)
	{
		assertEquals(moderation.get("status").asText(), status);
	}

	public static ObjectNode findStatus(JsonNode moderation, String... uuids)
	{
		JsonNode parent = moderation.get("nodes");
		for( String uuid : uuids )
		{
			parent = childStatus(parent, uuid);
			if( parent == null )
			{
				break;
			}
		}
		return (ObjectNode) parent;
	}

	public static ObjectNode childStatus(JsonNode parent, String uuid)
	{
		for( JsonNode child : parent.get("children") )
		{
			if( child.get("uuid").asText().equals(uuid) )
			{
				return (ObjectNode) child;
			}
		}
		return null;
	}
	
	public static void assertNodeStatus(ObjectNode node, Status status)
	{
		String statusTxt = null;
		switch( status )
		{
			case I:
				statusTxt = "incomplete";
				break;
			case W:
				statusTxt = "waiting";
				break;
			case C:
				statusTxt = "complete";
				break;
		}
		assertEquals(node.get("status").asText(), statusTxt);
	}
}
