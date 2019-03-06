package com.tle.webtests.pageobject.integration.canvas.course;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.selection.SelectionSession;

public class CanvasTextEditor extends AbstractCanvasCoursePage<CanvasTextEditor>
{
	// FIXME wildcards for the unique_id
	@FindBy(id = "title")
	private WebElement titleField;
	@FindBy(xpath = "id('editor_box_unique_id_1_tbl')//td/a[@title='EQUELLA']")
	private WebElement addEqContentButton;
	@FindBy(xpath = "//div[contains(@class,'form-actions')]/div/a")
	private WebElement cancelButton;

	public CanvasTextEditor(PageContext context)
	{
		super(context, By.id("editor_box_unique_id_1_ifr"));
	}

	public void setTitle(String title)
	{
		titleField.clear();
		titleField.sendKeys(title);
	}

	public SelectionSession startSelectionSession()
	{
		addEqContentButton.click();
		driver.switchTo().frame("external_tool_button_frame");
		return new SelectionSession(context).get();
	}

	public boolean picEmedded(String image)
	{
		driver.switchTo().frame("editor_box_unique_id_1_ifr");
		boolean present = isPresent(By.xpath("id('tinymce')//p/img[@alt = " + quoteXPath(image) + "]"));
		driver.switchTo().defaultContent();
		return present;
	}

	public CanvasWikiPage cancel(CanvasWikiPage returnTo)
	{
		cancelButton.click();
		acceptConfirmation();
		return returnTo.get();
	}

}
