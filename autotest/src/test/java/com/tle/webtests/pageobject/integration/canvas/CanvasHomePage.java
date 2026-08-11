package com.tle.webtests.pageobject.integration.canvas;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.integration.canvas.course.AbstractCanvasPage;
import org.openqa.selenium.By;

public class CanvasHomePage extends AbstractCanvasPage<CanvasHomePage> {
  public CanvasHomePage(PageContext context) {
    super(context, By.id("dashboard"));
  }
}
