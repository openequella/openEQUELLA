package com.tle.webtests.pageobject.connectors;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.common.Check;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public abstract class AbstractConnectorEditPage<THIS extends AbstractConnectorEditPage<THIS>>
	extends
		AbstractEditEntityPage<THIS, ShowConnectorsPage>
{
	private WebElement getLmsType()
	{
		return findWithId(getContributeSectionId(), "_ct");
	}
	@FindBy(id = "testdiv")
	private WebElement testAjaxDiv;
	@FindBy(xpath = "id('testdiv')//span[contains(@class, 'status')]")
	private WebElement testStatus;
	@FindBy(name = "{id}_viewableSelector_es.e")
	private WebElement viewExpressionInput;
	@FindBy(name = "{id}_exportableSelector_es.e")
	private WebElement exportExpressionInput;

	protected AbstractConnectorEditPage(ShowConnectorsPage connectorsPage)
	{
		super(connectorsPage);
	}

	@Override
	public String getContributeSectionId()
	{
		return "cc";
	}

	public THIS setType(String type)
	{
		new EquellaSelect(context, getLmsType()).selectByVisibleText(type);
		waitForElement(getSaveButton());
		return get();
	}

	public THIS setUsername(String username)
	{
		getUsernameField().clear();
		getUsernameField().sendKeys(username);
		return get();
	}

	public THIS setAllowSummary(boolean allow)
	{
		if( allow == Check.isEmpty(getAllowSummaryCheckbox().getAttribute("checked")) )
		{
			getAllowSummaryCheckbox().click();
		}
		return get();
	}

	public boolean testConnection()
	{
		WaitingPageObject<THIS> waiter = ajaxUpdateExpect(testAjaxDiv, testStatus);
		getTestButton().click();
		waiter.get();
		return testStatus.getAttribute("class").contains("ok");
	}

	abstract public WebElement getUsernameField();

	abstract public WebElement getTestButton();

	abstract public String getId();

	abstract public WebElement getAllowSummaryCheckbox();

	public THIS viewableForAll()
	{
		return viewableForExpression("* ");
	}

	public THIS viewableForExpression(final String expression)
	{
		((JavascriptExecutor) driver).executeScript("_subev('" + getId() + ".expression', '" + getId()
			+ "_viewableSelector', '" + expression + "');");

		return ExpectWaiter.waiter(ExpectedConditions2.elementAttributeToBe(viewExpressionInput, "value", expression),
			this).get();
	}

	public THIS exportableForAll()
	{
		return exportableForExpression("* ");
	}

	public THIS exportableForExpression(String expression)
	{
		((JavascriptExecutor) driver).executeScript("_subev('" + getId() + ".expression', '" + getId()
			+ "_exportableSelector', '" + expression + "');");

		return ExpectWaiter.waiter(
			ExpectedConditions2.elementAttributeToBe(exportExpressionInput, "value", expression), this).get();
	}

	@Override
	protected String getEntityName()
	{
		return "connector";
	}
}
