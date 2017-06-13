package com.tle.webtests.pageobject.institution;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ServerAdminLogonPage extends AbstractPage<ServerAdminLogonPage>
{

    public ServerAdminLogonPage(PageContext context)
    {
        super(context);
    }

    @FindBy(id = "islm_password")
    private WebElement passwordField;
    @FindBy(id = "islm_loginButton")
    private WebElement loginButton;


    @Override
    protected void loadUrl() {
        driver.get(context.getTestConfig().getAdminUrl() + "institutions.do?method=admin");
    }

    @Override
    protected WebElement findLoadedElement() {
        return loginButton;
    }

    public <T extends PageObject> T logon(String password, WaitingPageObject<T> page)
    {
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
        return page.get();
    }
}
