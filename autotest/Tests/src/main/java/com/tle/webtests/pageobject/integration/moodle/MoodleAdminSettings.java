package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class MoodleAdminSettings extends AbstractPage<MoodleAdminSettings>
{
	@FindBy(xpath = "//input[@value='Save changes']")
	private WebElement save;

	public MoodleAdminSettings(PageContext context)
	{
		super(context, By.id("maincontent"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "/admin/search.php");
	}

	public void enableWebServiceAccessForUser(String user)
	{
		ExternalServiceUsersPage users = new ExternalServicesPage(context).load().editUsers();
		users.selectUser(user);
	}

	public void enableWebServices()
	{
		driver.get(context.getIntegUrl() + "/admin/search.php?query=enablewebservices");

		WebElement check = waitForElement(By.id("id_s__enablewebservices"));
		if( !check.isSelected() )
		{
			check.click();
			save.click();
			waitForElement(By.className("notifysuccess"));
		}
	}

	public void enableRest()
	{
		driver.get(context.getIntegUrl() + "/admin/settings.php?section=webserviceprotocols");
		WebElement check = waitForElement(By.xpath("//span[text()='REST protocol']/../../td/a/img"));
		if( check.getAttribute("alt").equals("Enable") )
		{
			clickAndRemove(check);
		}
	}

	public String addToken(String fullUser)
	{
		TokenListPage tokenConfig = new TokenListPage(context).load();

		if( !tokenConfig.isTokenPresent() )
		{
			tokenConfig.addToken(fullUser);
		}
		return tokenConfig.getToken();
	}

	private static class TokenListPage extends AbstractPage<TokenListPage>
	{
		@FindBy(xpath = "//h2[text()='Manage tokens']")
		private WebElement title;
		@FindBy(xpath = "//tr[td[3]/text()='equellaservice']")
		private WebElement serviceRow;
		@FindBy(linkText = "Add")
		private WebElement addButton;

		public TokenListPage(PageContext context)
		{
			super(context);
		}

		public TokenListPage addToken(String fullUser)
		{
			addButton.click();
			CreateTokenPage createPage = new CreateTokenPage(this).get();
			createPage.selectUser(fullUser);
			createPage.selectService("equellaservice");
			return createPage.save();
		}

		public boolean isTokenPresent()
		{
			return isPresent(serviceRow);
		}

		public String getToken()
		{
			return serviceRow.findElement(By.xpath("td[1]")).getText();
		}

		@Override
		protected void loadUrl()
		{
			driver.get(context.getIntegUrl() + "/admin/settings.php?section=webservicetokens");
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return title;
		}
	}

	private static class CreateTokenPage extends AbstractPage<CreateTokenPage>
	{
		@FindBy(id = "id_user")
		private WebElement userSelectElem;
		@FindBy(id = "id_service")
		private WebElement serviceSelectElem;
		@FindBy(xpath = "//input[@value='Save changes']")
		private WebElement save;
		private TokenListPage tokenList;

		public CreateTokenPage(TokenListPage tokenList)
		{
			super(tokenList);
			this.tokenList = tokenList;
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return userSelectElem;
		}

		public void selectService(String string)
		{
			new Select(serviceSelectElem).selectByVisibleText("equellaservice");
		}

		public void selectUser(String fullUser)
		{
			new Select(userSelectElem).selectByVisibleText(fullUser);
		}

		public TokenListPage save()
		{
			save.click();
			return tokenList.get();
		}
	}

	private static class ExternalServicesPage extends AbstractPage<ExternalServicesPage>
	{
		@FindBy(xpath = "//h2[text()='External services']")
		private WebElement title;
		@FindBy(xpath = "//tr[td[1]/span/text() = 'equellaservice']")
		private WebElement serviceRow;

		public ExternalServicesPage(PageContext context)
		{
			super(context);
		}

		public ExternalServiceUsersPage editUsers()
		{
			serviceRow.findElement(By.linkText("Authorised users")).click();
			return new ExternalServiceUsersPage(this).get();
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return title;
		}

		@Override
		protected void loadUrl()
		{
			driver.get(context.getIntegUrl() + "/admin/settings.php?section=externalservices");
		}
	}

	private static class ExternalServiceUsersPage extends AbstractPage<ExternalServiceUsersPage>
	{
		@FindBy(id = "addselect")
		private WebElement addSelectElem;
		@FindBy(id = "add")
		private WebElement addButton;

		public ExternalServiceUsersPage(ExternalServicesPage servicesPage)
		{
			super(servicesPage);
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return addSelectElem;
		}

		public void selectUser(String user)
		{
			By option = By.xpath(".//option[contains(text(), " + quoteXPath(user) + ")]");
			if( isPresent(addSelectElem, option) )
			{
				addSelectElem.findElement(option).click();
				WaitingPageObject<ExternalServiceUsersPage> waiter = updateWaiter();
				addButton.click();
				waiter.get();
			}
		}
	}
}
