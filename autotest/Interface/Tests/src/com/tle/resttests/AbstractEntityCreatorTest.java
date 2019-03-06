package com.tle.resttests;

import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.WorkflowRequests;
import com.tle.resttests.util.RequestsBuilder;

public class AbstractEntityCreatorTest extends AbstractRestAssuredTest
{
	protected ItemRequests items;
	protected CollectionRequests collections;
	protected WorkflowRequests workflows;
	protected SchemaRequests schemas;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		RequestsBuilder builder = builder();
		items = builder.items();
		collections = builder.collections();
		workflows = builder.workflows();
		schemas = builder.schemas();
	}
}
