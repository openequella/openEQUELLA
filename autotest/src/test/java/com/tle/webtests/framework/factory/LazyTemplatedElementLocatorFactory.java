package com.tle.webtests.framework.factory;

import com.tle.webtests.pageobject.PageObject;
import java.lang.reflect.Field;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class LazyTemplatedElementLocatorFactory implements ElementLocatorFactory {
  private PageObject pageObject;

  public LazyTemplatedElementLocatorFactory(PageObject pageObject) {
    this.pageObject = pageObject;
  }

  @Override
  public ElementLocator createLocator(Field field) {
    return new LazyTemplatedElementLocator(pageObject, field);
  }
}
