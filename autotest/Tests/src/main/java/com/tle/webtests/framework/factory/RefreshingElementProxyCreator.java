package com.tle.webtests.framework.factory;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public interface RefreshingElementProxyCreator {
  WebElement proxyForLocator(ClassLoader loader, ElementLocator locator);
}
