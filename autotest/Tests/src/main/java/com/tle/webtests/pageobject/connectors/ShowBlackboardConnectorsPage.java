package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;

/**
 * Note: this is the exact same page as ShowMoodleConnectorsPage and
 * ShowEquellaConnectorsPage. The magic of generics required these be seperate
 * classes
 * 
 * @author Aaron
 */
public class ShowBlackboardConnectorsPage
{

	public static ShowConnectorsPage addBlackboardConnection(ShowConnectorsPage showConnectorsPage, PrefixedName name,
		String url, String username, String password)
	{
		EditBlackboardConnectorPage editPage = showConnectorsPage.createConnector(new EditBlackboardConnectorPage(
			showConnectorsPage));
		editPage.setType("Blackboard");
		editPage.setName(name);
		editPage.setUsername("return '" + username + "';");
		editPage.setUrl(url, username, password);
		return editPage.save();
	}

	public static ShowConnectorsPage registerProxyTool(ShowConnectorsPage showConnectorsPage, String url)
	{
		EditBlackboardConnectorPage editPage = showConnectorsPage.createConnector(new EditBlackboardConnectorPage(
			showConnectorsPage));
		editPage.setType("Blackboard");
		editPage.registerProxy(url);
		return editPage.cancel();
	}
}
