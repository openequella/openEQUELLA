package com.tle.webtests.pageobject.wizard.controls;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class HTMLEditBoxControl extends AbstractWizardControl<HTMLEditBoxControl>
{
	@FindBy(id = "{wizid}_mct_html_parent")
	private WebElement editor;
	@FindBy(xpath = "id('{wizid}')//div[@class='lockedHtml']")
	private WebElement lockededitor;
	@FindBy(id = "{wizid}_editLink")
	private WebElement editlink;
	@FindBy(id = "{wizid}_mct_html_ifr")
	private WebElement iframe;
	@FindBy(id = "{wizid}_mct_html_code")
	private WebElement code;
	@FindBy(id = "{wizid}_mct_html_spellchecker_action")
	private WebElement spellcheckbutton;

	private final boolean isOnDemand;
	private boolean isLocked;

	public HTMLEditBoxControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		this(context, ctrlnum, page, false);
	}

	public HTMLEditBoxControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page, boolean isOnDemand)
	{
		super(context, ctrlnum, page);
		this.isOnDemand = isOnDemand;
		this.isLocked = isOnDemand;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return isLocked ? lockededitor : editor;
	}

	public boolean isLocked()
	{
		return isLocked;
	}

	public HTMLEditBoxControl setLocked(boolean isLocked)
	{
		if( isOnDemand )
		{
			this.isLocked = isLocked;
		}
		return get();
	}

	public HTMLEditBoxControl toggleEdit()
	{
		if( isOnDemand )
		{
			editlink.click();
			isLocked = !isLocked;
		}
		return get();
	}

	public boolean isEditable()
	{
		return !isVisible(lockededitor) && isVisible(editor);
	}

	public void setBodyContent(String content)
	{
		driver.switchTo().frame(iframe);
		driver.switchTo().activeElement();
		((JavascriptExecutor) driver).executeScript("document.body.innerHTML = " + quoteXPath(content));
		driver.switchTo().defaultContent();
	}

	public void setHtmlContent(String html)
	{
		code.click();
		driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@src, 'source_editor.htm')]")));
		waitForElement(By.id("htmlSource"));
		WebElement htmlTextArea = driver.findElement(By.id("htmlSource"));
		htmlTextArea.clear();
		htmlTextArea.sendKeys(html);
		driver.findElement(By.id("insert")).click();
		driver.switchTo().defaultContent();
	}

	public String getBodyContent()
	{
		if( !isLocked() )
		{
			WebElement body = driver.switchTo().frame(iframe).findElement(By.xpath("//body[@id='tinymce']"));
			String content = body.getText();
			driver.switchTo().defaultContent();
			return content;
		}
		return lockededitor.getText();
	}

	public String getHtmlContent()
	{
		code.click();
		driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@src, 'source_editor.htm')]")));
		waitForElement(By.id("htmlSource"));
		WebElement htmlTextArea = driver.findElement(By.id("htmlSource"));
		String html = htmlTextArea.getAttribute("value");
		driver.findElement(By.id("cancel")).click();
		driver.switchTo().defaultContent();
		return html;
	}

	public void invokeSpellChecker()
	{
		spellcheckbutton.click();
		getWaiter().until(new ExpectedCondition<Boolean>()
		{
			@Override
			public Boolean apply(WebDriver driver)
			{
				driver.switchTo().frame(iframe);

				try
				{
					if( driver.findElement(By.className("mceItemHiddenSpellWord")).isDisplayed() )
					{
						return true;
					}
				}
				catch( NoSuchElementException nse )
				{
					return false;
				}
				finally
				{
					driver.switchTo().defaultContent();
				}

				return false;
			}
		});
	}

	public boolean hasMispeltWords()
	{
		WebElement body = driver.switchTo().frame(iframe).findElement(By.xpath("//body[@id='tinymce']"));
		List<WebElement> redUnderlinedWords = body.findElements(By.className("mceItemHiddenSpellWord"));
		return redUnderlinedWords.size() > 0;
	}

	public void toggleSpellingSuggestionsContextMenu(boolean switchToDefault)
	{
		List<WebElement> redUnderlinedWords = driver.findElements(By.className("mceItemHiddenSpellWord"));
		redUnderlinedWords.get(0).click();
		if( switchToDefault )
		{
			driver.switchTo().defaultContent();
		}
	}

	// assuming the elements of this class are the spans which wrap the
	// highlighted text
	public boolean confirmMisspeltWord(String misspelt)
	{
		List<WebElement> redUnderlinedWords = driver.findElements(By.className("mceItemHiddenSpellWord"));
		for( WebElement spannedWord: redUnderlinedWords)
		{
			if( misspelt.equals(spannedWord.getText()) )
			{
				return true;
			}
		}
		return false;
	}
}
