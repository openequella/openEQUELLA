package com.tle.webtests.pageobject.adminconsole.schema;

import org.fest.swing.fixture.WindowFixture;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.adminconsole.AbstractAppletWindow;

public abstract class AbstractSchemaTab extends AbstractAppletWindow<AbstractSchemaTab>
{

	public AbstractSchemaTab(PageContext context, WindowFixture<?> windowHolder)
	{
		super(context);
		this.windowHolder = windowHolder;
	}

	public SchemaEditorTab editor()
	{
		clickTab("Editor");
		return new SchemaEditorTab(context, windowHolder);
	}

	public SchemaDetailsTab details()
	{
		clickTab("Details");
		return new SchemaDetailsTab(context, windowHolder);
	}

	public void save()
	{
		clickButton("Save");
		clickButton("OK");
		clickButton("Close");
	}

	public void close()
	{
		clickButton("Close");
	}
}
