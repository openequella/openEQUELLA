package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

public class ShowConnectorsPage extends AbstractShowEntitiesPage<ShowConnectorsPage>
{
	public ShowConnectorsPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getSectionId()
	{
		return "sc";
	}

	@Override
	protected String getH2Title()
	{
		return "External system connectors";
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/connectors.do");
	}

	@Override
	protected String getEmptyText()
	{
		return "There a no available connectors to edit";
	}

	public <T extends AbstractConnectorEditPage<T>> T createConnector(T editor)
	{
		return createEntity(editor);
	}

	public <T extends AbstractConnectorEditPage<T>> T editConnector(T editor, PrefixedName name)
	{
		return editEntity(editor, name);
	}
}
