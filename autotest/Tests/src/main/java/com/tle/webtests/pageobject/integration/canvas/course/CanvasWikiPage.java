package com.tle.webtests.pageobject.integration.canvas.course;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

// called canvasWikiPage so it wasn't CanvasPagesPage...
public class CanvasWikiPage extends AbstractCanvasCoursePage<CanvasWikiPage>
{
	@FindBy(className = "new_page")
	private WebElement newPageButton;

	public CanvasWikiPage(PageContext context)
	{
		super(context, By.className("new_page"));
	}

	public CanvasTextEditor newPage()
	{
		newPageButton.click();
		return new CanvasTextEditor(context).get();
	}

}
