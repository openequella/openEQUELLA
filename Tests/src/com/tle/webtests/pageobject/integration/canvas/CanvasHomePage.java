package com.tle.webtests.pageobject.integration.canvas;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.integration.canvas.course.AbstractCanvasPage;

public class CanvasHomePage extends AbstractCanvasPage<CanvasHomePage>
{
	public CanvasHomePage(PageContext context)
	{
		super(context, By.id("dashboard"));
	}


}
