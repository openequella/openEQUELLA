package com.tle.webtests.framework.factory;

import com.tle.webtests.pageobject.PageObject;
import java.lang.reflect.Field;
import org.openqa.selenium.support.pagefactory.Annotations;

public class TemplatedAnnotations extends Annotations {
  private PageObject pageObject;
  private boolean dontCache;

  public TemplatedAnnotations(Field field, PageObject pageObject) {
    super(field);
    this.pageObject = pageObject;
    this.dontCache = (field.getAnnotation(DontCache.class) != null);
  }

  public boolean isDontCache() {
    return dontCache;
  }

  //	@Override
  //	protected By buildByFromFindBy(FindBy findBy)
  //	{
  //		Object proxiedFindBy = Proxy.newProxyInstance(getClass().getClassLoader(), new
  // Class<?>[]{FindBy.class},
  //			new ByInvocationHandler(pageObject, findBy));
  //		return super.buildByFromFindBy((FindBy) proxiedFindBy);
  //	}
}
