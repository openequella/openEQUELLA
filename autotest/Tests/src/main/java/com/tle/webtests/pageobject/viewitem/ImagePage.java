package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;

public class ImagePage extends AbstractPage<ImagePage> {

  public ImagePage(PageContext context) {
    super(context, By.xpath("/html/body/img"));
  }

  public String imageSource() {
    return driver.findElement(By.xpath("/html/body/img")).getAttribute("src");
  }

  // One option for image validation, a hash would be ideal but not sure if
  // it can be done
  public Dimension getDimensions() {
    return driver.findElement(By.xpath("/html/body/img")).getSize();
  }
}
