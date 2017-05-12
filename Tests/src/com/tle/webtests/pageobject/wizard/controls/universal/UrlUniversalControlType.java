package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.common.Check;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public class UrlUniversalControlType extends AbstractAttachmentDialogPage<UrlUniversalControlType>
	implements
		AttachmentType<UrlUniversalControlType, UrlAttachmentEditPage>
{
	@FindBy(id = "{wizid}_dialog_uh_url")
	private WebElement urlField;

	public UrlUniversalControlType(UniversalControl universalControl)
	{
		super(universalControl);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return urlField;
	}

	@Override
	public String getType()
	{
		return "URL";
	}

	public UniversalControl addUrl(String url, String name)
	{
		return addUrl(url, name, null);
	}

	public UniversalControl addUrl(String url, String name, Boolean preview)
	{
		urlField.clear();
		urlField.sendKeys(url);
		addButton.click();
		UrlAttachmentEditPage edit = edit();
		if( !Check.isEmpty(name) )
		{
			edit.setDisplayName(name);
		}
		if( preview != null )
		{
			edit.setPreview(preview);
		}
		return edit.save();
	}

	@Override
	public UrlAttachmentEditPage edit()
	{
		return new UrlAttachmentEditPage(control).get();
	}
}
