package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;

/**
 * Note: this is the exact same page as ShowMoodleConnectorsPage and
 * ShowEquellaConnectorsPage. The magic of generics required these be seperate
 * classes
 * 
 * @author Aaron
 */
public class ShowMoodleConnectorsPage
{
	public static ShowConnectorsPage addMoodleConnection(ShowConnectorsPage showConnectorsPage, PrefixedName name,
		String url, String token, String username)
	{
		return addMoodleConnection(showConnectorsPage, name, url, token, username, true);
	}

	public static ShowConnectorsPage addMoodleConnection(ShowConnectorsPage showConnectorsPage, PrefixedName name,
		String url, String token, String username, boolean allowSummary)
	{
		EditMoodleConnectorPage editPage = showConnectorsPage.createConnector(new EditMoodleConnectorPage(
			showConnectorsPage));
		editPage.get();
		editPage.setType("Moodle");
		editPage.setName(name);
		editPage.setAllowSummary(allowSummary);
		editPage.setUrl(url);
		editPage.setToken(token);
		editPage.exportableForAll();
		editPage.viewableForAll();
		editPage.setUsername("return '" + username + "';");
		editPage.testConnection();
		return editPage.save();
	}
}
