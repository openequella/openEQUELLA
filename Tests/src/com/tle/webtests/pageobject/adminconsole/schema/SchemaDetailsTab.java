package com.tle.webtests.pageobject.adminconsole.schema;

import org.fest.swing.fixture.WindowFixture;

import com.tle.webtests.framework.PageContext;

public class SchemaDetailsTab extends AbstractSchemaTab
{

	public SchemaDetailsTab(PageContext context, WindowFixture<?> windowHolder)
	{
		super(context, windowHolder);
	}

	public SchemaDetailsTab setName(String name)
	{
		setText(0, name);
		return this;
	}
	
	public SchemaDetailsTab setDescription(String description)
	{
		setText(1, description);
		return this;
	}

	public SchemaDetailsTab setNameXml(String path)
	{
		clickButton("Browse", 0);
		clickTreePath(path);
		clickButton("OK");
		return this;
	}
	
	public SchemaDetailsTab setDescriptionXml(String path)
	{
		clickButton("Browse", 1);
		clickTreePath(path);
		clickButton("OK");
		return this;
	}

}
