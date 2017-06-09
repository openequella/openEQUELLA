package com.tle.webtests.pageobject.adminconsole.schema;

import org.fest.swing.fixture.WindowFixture;

import com.tle.webtests.framework.PageContext;

public class SchemaEditorTab extends AbstractSchemaTab
{

	public SchemaEditorTab(PageContext context, WindowFixture<?> windowHolder)
	{
		super(context, windowHolder);
	}

	public SchemaEditorTab addChild(String parent, String name)
	{
		return addChild(parent, name, false, false);
	}

	public SchemaEditorTab addChild(String parent, String name, boolean freetext, boolean powerSearch)
	{
		clickTreePath(parent);
		clickButton("Child");
		setText(0, name);
		if( freetext )
		{
			clickCheckBox("Searchable by Free Text");
		}
		if( powerSearch )
		{
			clickCheckBox("Index for Advanced Searches");
		}
		clickTreePath("xml");
		return this;
	}

	public SchemaEditorTab addSibling(String parent, String name)
	{
		clickTreePath(parent);
		clickButton("Sibling");
		setText(0, name);
		clickTreePath("xml");
		return this;
	}

}
