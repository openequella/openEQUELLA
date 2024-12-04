/** */
package com.tle.webtests.pageobject.settings;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ActiveCacheTreeNode extends AbstractPage<ActiveCacheTreeNode> {
  @FindBy(xpath = "div/a")
  private WebElement treeNode;

  @FindBy(xpath = "ul")
  private WebElement childNodes;

  private ActiveCachingPage acp;

  public ActiveCacheTreeNode(ActiveCachingPage acp, SearchContext searchContext, String name) {
    super(acp.getContext(), searchContext, By.xpath("li[div/a/text()=" + quoteXPath(name) + "]"));
    this.acp = acp;
  }

  @Override
  public SearchContext getSearchContext() {
    return loadedElement;
  }

  public ActiveCacheTreeNode addSubGroup(String subGroupName) {
    WaitingPageObject<ActiveCachingPage> waiter = acp.collectionsUpdate();
    treeNode.click();
    waiter.get();
    AddGroupPage agd = acp.clickAddGroupButton();
    agd.setGroupname(subGroupName);
    ActiveCacheTreeNode newNode = new ActiveCacheTreeNode(acp, childNodes, subGroupName);
    return agd.ok(newNode);
  }

  public void removeNode() {
    WaitingPageObject<ActiveCachingPage> waiter = acp.collectionsUpdate();
    treeNode.click();
    waiter.get();
    if (acp.removeEnabled()) {
      acp.clickRemoveButton();
    }
  }
}
