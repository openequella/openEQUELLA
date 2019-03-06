package com.tle.resttests.test.collection;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.CollectionJson;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.resttests.AbstractEntityApiEditTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

@Test
public class CollectionApiEditTest extends AbstractEntityApiEditTest
{

	@Override
	protected BaseEntityRequests createRequestsWithBuilder(RequestsBuilder builder)
	{
		return builder.collections();
	}

	@Override
	protected ObjectNode createJsonForPrivs(String fullName)
	{
		return CollectionJson.json(fullName, RestTestConstants.SCHEMA_BASIC, null);
	}

	@Override
	protected ObjectNode createJsonForEdit(String fullName)
	{
		return CollectionJson.json(fullName, RestTestConstants.SCHEMA_BASIC, null);
	}

	@Override
	protected String getEditPrivilege()
	{
		return "EDIT_COLLECTION";
	}

	@Override
	protected void assertExtraEdits(ObjectNode edited)
	{
		// nothing
	}

	@Override
	protected void extraEdits(ObjectNode client)
	{
		// nothing
	}

}
