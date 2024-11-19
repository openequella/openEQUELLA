package com.tle.webtests.framework.factory;

import com.tle.webtests.pageobject.PageObject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.WrapsElement;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;
import org.openqa.selenium.support.pagefactory.ElementLocator;

public class RefreshingFieldDecorator extends DefaultFieldDecorator
    implements RefreshingElementProxyCreator {
  public RefreshingFieldDecorator(PageObject pageObject) {
    super(new LazyTemplatedElementLocatorFactory(pageObject));
  }

  @Override
  public WebElement proxyForLocator(ClassLoader loader, ElementLocator locator) {
    InvocationHandler handler =
        new RefreshingElementHandler(this, (LazyTemplatedElementLocator) locator);
    WebElement proxy;
    proxy =
        (WebElement)
            Proxy.newProxyInstance(
                loader,
                new Class[] {RefreshableElement.class, WebElement.class, WrapsElement.class},
                handler);
    return proxy;
  }
}
