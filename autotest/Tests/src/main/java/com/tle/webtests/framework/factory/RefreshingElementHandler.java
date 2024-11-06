package com.tle.webtests.framework.factory;

import com.google.common.base.Function;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.FluentWait;

public class RefreshingElementHandler implements InvocationHandler {
  private static final FluentWait<Object> waiter =
      new FluentWait<Object>(Void.class)
          .withTimeout(Duration.ofSeconds(10))
          .pollingEvery(Duration.ofMillis(50));
  private LazyTemplatedElementLocator locator;
  private RefreshingElementProxyCreator proxyCreator;

  public RefreshingElementHandler(
      RefreshingElementProxyCreator proxyCreator, LazyTemplatedElementLocator locator) {
    this.locator = locator;
    this.proxyCreator = proxyCreator;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    try {
      return waiter.until(
              new Function<Object, InvokeResponse>() {
                @Override
                public InvokeResponse apply(Object arg0) {
                  if (method.getName().equals("toString")) {
                    return new InvokeResponse(locator.toString());
                  }
                  if (method.getName().equals("findNonWrapped")) {
                    return new InvokeResponse(locator.findNonWrapped());
                  }
                  WebElement element = locator.findElement();
                  try {
                    Object returnVal;
                    if (method.getName().equals("getWrappedElement")) {
                      if (element instanceof WrapsElement) {
                        returnVal = ((WrapsElement) element).getWrappedElement();
                      } else {
                        returnVal = element;
                      }
                    } else {
                      returnVal = method.invoke(element, args);
                      if (method.getName().equals("findElement")
                          && locator.isWrapChildElements()
                          && !(returnVal instanceof RefreshableElement)) {
                        returnVal =
                            proxyCreator.proxyForLocator(
                                getClass().getClassLoader(),
                                new LazyTemplatedElementLocator(
                                    locator.getPageObject(),
                                    (WebElement) returnVal,
                                    (SearchContext) proxy,
                                    (By) args[0]));
                      }
                    }
                    return new InvokeResponse(returnVal);
                  } catch (InvocationTargetException ite) {
                    if (ite.getCause() instanceof StaleElementReferenceException) {
                      locator.invalidateCache();
                      return null;
                    } else {
                      throw new InvokeException(ite.getCause());
                    }
                  } catch (Throwable t) {
                    throw new InvokeException(t);
                  }
                }
              })
          .returnValue;
    } catch (InvokeException e) {
      throw e.getCause();
    }
  }

  public static class InvokeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvokeException(Throwable t) {
      super(t);
    }
  }

  public static class InvokeResponse {
    final Object returnValue;

    public InvokeResponse(Object returnValue) {
      this.returnValue = returnValue;
    }
  }
}
