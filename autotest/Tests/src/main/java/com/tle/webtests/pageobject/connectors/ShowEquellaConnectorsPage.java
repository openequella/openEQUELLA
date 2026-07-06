package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;

public final class ShowEquellaConnectorsPage {

  private ShowEquellaConnectorsPage() {
    throw new UnsupportedOperationException();
  }

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
