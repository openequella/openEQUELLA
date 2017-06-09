package com.tle.webtests.pageobject.externaltools;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public class EditExternalToolPage extends AbstractEditEntityPage<EditExternalToolPage, ShowExternalToolsPage>
{
	@FindBy(id = "{editorSectionId}_baseUrl")
	private WebElement baseUrlField;
	@FindBy(id = "{editorSectionId}_consumerKey")
	private WebElement consumerKeyField;
	@FindBy(id = "{editorSectionId}_sharedSecret")
	private WebElement sharedSecretField;
	@FindBy(id = "{editorSectionId}_customParams")
	private WebElement customParamsField;
	@FindBy(id = "{editorSectionId}_shareName")
	private WebElement shareNameCheckbox;
	@FindBy(id = "{editorSectionId}_shareEmail")
	private WebElement shareEmailCheckbox;

	protected EditExternalToolPage(ShowExternalToolsPage showToolsPage)
	{
		super(showToolsPage);
	}

	public void setBaseUrl(String url)
	{
		baseUrlField.clear();
		baseUrlField.sendKeys(url);
	}

	public void setKeySecret(String key, String secret)
	{
		consumerKeyField.clear();
		consumerKeyField.sendKeys(key);
		sharedSecretField.clear();
		sharedSecretField.sendKeys(secret);
	}

	@Override
	protected String getTitle(boolean create)
	{
		return (create ? "Create " : "Edit ");
	}

	// TODO could change input to a list of key/value
	public void setCustomParams(String params)
	{
		customParamsField.clear();
		customParamsField.sendKeys(params);
	}

	public void setShareOptions(boolean shareName, boolean shareEmail)
	{
		if( shareName ^ shareNameCheckbox.isSelected() )
		{
			shareNameCheckbox.click();
		}
		if( shareEmail ^ shareEmailCheckbox.isSelected() )
		{
			shareEmailCheckbox.click();
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
