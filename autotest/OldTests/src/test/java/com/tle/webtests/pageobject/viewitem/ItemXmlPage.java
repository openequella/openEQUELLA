package com.tle.webtests.pageobject.viewitem;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.dytech.devlib.PropBagEx.ValueThoroughIterator;
import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ItemXmlPage extends AbstractPage<ItemXmlPage> {
  private final ItemId itemId;
  private PropBagEx xml;

  public ItemXmlPage(PageContext context, ItemId itemId) {
    super(context);
    this.itemId = itemId;
  }

  @Override
  protected void checkLoadedElement() {
    String xmlString = driver.getPageSource();
    try {
      xml = new PropBagEx(xmlString);
    } catch (Exception e) {
      throw new Error("Not loaded", e);
    }
    if (context.getTestConfig().isChromeDriverSet()) {
      xml = new PropBagEx(xml.getSubtree("/body/div/xml").toString());
    }

    if (!"xml".equals(xml.getNodeName())) {
      throw new Error("Not loaded");
    }
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "items/" + itemId.toString() + "/<XML>");
  }

  public String nodeValue(String path) {
    return xml.getNode(path, null);
  }

  public boolean nodeHasValue(String path, String value) {
    return nodeHasValue(path, null, value);
  }

  public int getNodeCount(String path) {
    return xml.getNodeList(path).size();
  }

  public boolean nodeHasValue(String path, String attribute, String value) {
    if (!Check.isEmpty(attribute)) {
      PropBagThoroughIterator iterateAll = xml.iterateAll(path);
      while (iterateAll.hasNext()) {
        PropBagEx node = (PropBagEx) iterateAll.next();
        String[] attributes = node.getAttributes("", attribute);
        for (String att : attributes) {
          if (value.equals(att)) {
            return true;
          }
        }
      }
      String[] attributes = xml.getAttributes(path, attribute);
      for (String att : attributes) {
        if (value.equals(att)) {
          return true;
        }
      }
    } else {
      ValueThoroughIterator allValues = xml.iterateAllValues(path);
      while (allValues.hasNext()) {
        String node = (String) allValues.next();
        if (value.equals(node)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean nodeIsEmpty(String path) {
    return xml.getNode(path).isEmpty();
  }
}
