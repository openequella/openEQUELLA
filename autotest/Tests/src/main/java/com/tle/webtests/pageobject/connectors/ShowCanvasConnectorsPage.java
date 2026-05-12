package com.tle.webtests.pageobject.connectors;

import com.tle.webtests.pageobject.PrefixedName;

public final class ShowCanvasConnectorsPage {

  private ShowCanvasConnectorsPage() {
    throw new UnsupportedOperationException();
  }

  public static ShowConnectorsPage createConnector(
      ShowConnectorsPage showConnectorsPage, PrefixedName name, String token) {
    EditCanvasConnectorPage canvasEditor =
        showConnectorsPage.createConnector(new EditCanvasConnectorPage(showConnectorsPage));
    canvasEditor.get();
    canvasEditor.setType("Canvas");
    canvasEditor.createConnector(name, token);

    return canvasEditor.save();
  }
}
