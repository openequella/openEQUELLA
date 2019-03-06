package com.tle.webtests.pageobject.externaltools;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public class EditExternalToolPage extends AbstractEditEntityPage<EditExternalToolPage, ShowExternalToolsPage>
{
	private WebElement getBaseUrlField()
	{
		return findByEditorSectionId("_baseUrl");
	}

	private WebElement findByEditorSectionId(String postfix)
	{
		return findWithId(getEditorSectionId(), postfix);
	}

	private WebElement getConsumerKeyField()
	{
		return findByEditorSectionId("_consumerKey");
	}
	private WebElement getSharedSecretField()
	{
		return findByEditorSectionId("_sharedSecret");
	}
	private WebElement getCustomParamsField()
	{
		return findByEditorSectionId("_customParams");
	}
	private WebElement getShareNameCheckbox()
	{
		return findByEditorSectionId("_shareName");
	}
	private WebElement getShareEmailCheckbox()
	{
		return findByEditorSectionId("_shareEmail");
	}

	protected EditExternalToolPage(ShowExternalToolsPage showToolsPage)
	{
		super(showToolsPage);
	}

	public void setBaseUrl(String url)
	{
		getBaseUrlField().clear();
		getBaseUrlField().sendKeys(url);
	}

	public void setKeySecret(String key, String secret)
	{
		getConsumerKeyField().clear();
		getConsumerKeyField().sendKeys(key);
		getSharedSecretField().clear();
		getSharedSecretField().sendKeys(secret);
	}

	@Override
	protected String getTitle(boolean create)
	{
		return (create ? "Create " : "Edit ");
	}

	// TODO could change input to a list of key/value
	public void setCustomParams(String params)
	{
		getCustomParamsField().clear();
		getCustomParamsField().sendKeys(params);
	}

	public void setShareOptions(boolean shareName, boolean shareEmail)
	{
		if( shareName ^ getShareNameCheckbox().isSelected() )
		{
			getShareNameCheckbox().click();
		}
		if( shareEmail ^ getShareEmailCheckbox().isSelected() )
		{
			getShareEmailCheckbox().click();
		}
	}

	@Override
	protected String getEntityName()
	{
		return "external tool provider";
	}

	@Override
	protected String getContributeSectionId()
	{
		return "etc";
	}

	@Override
	protected String getEditorSectionId()
	{
		return "ete";
	}

}
