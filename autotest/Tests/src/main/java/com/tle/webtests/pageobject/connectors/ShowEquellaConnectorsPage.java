package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;

public class ShowEquellaConnectorsPage {
  public static ShowConnectorsPage addEquellaConnection(
      ShowConnectorsPage showConnectorsPage, PrefixedName name) {
    EditEquellaConnectorPage editPage =
        showConnectorsPage.createConnector(new EditEquellaConnectorPage(showConnectorsPage));
    editPage.setType("Local resources");
    editPage.setName(name);
    editPage.viewableForAll();
    return editPage.save();
  }
}
