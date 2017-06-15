package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

/**
 * The LTIAttachment creation and attachment edit duplicate almost all the inner
 * controls, so this class acts as a common inner component for the respective
 * test classes.
 * 
 * @author larry
 */
public class LTICommonControlContainerImpl extends AbstractWizardControlPage<LTICommonControlContainerImpl>
{
	@FindBy(xpath = "id('{wizid}_dialog')//div[contains(@class, 'ltiHandler')]")
	private WebElement mainDiv;

	@FindBy(id = "{wizid}_dialog_lh_displayName")
	private WebElement nameField;

	@FindBy(xpath = "id('{wizid}_dialog')//select[@id='{wizid}_dialog_lh_ltisel']")
	private WebElement selectProvider;

	@FindBy(id = "{wizid}_dialog_lh_launchUrl")
	private WebElement launchUrlEditBox;

	@FindBy(id = "{wizid}_dialog_lh_consumerKey")
	private WebElement consumerKeyEditBox;

	@FindBy(id = "{wizid}_dialog_lh_sharedSecret")
	private WebElement sharedSecretEditBox;

	@FindBy(id = "{wizid}_dialog_lh_customParams")
	private WebElement customParamsEditBox;

	@FindBy(id = "{wizid}_dialog_lh_iconUrl")
	private WebElement iconUrlEditBox;

	@FindBy(id = "{wizid}_dialog_lh_useDefaultPrivacy")
	private WebElement useDefaultPrivacyCheckBox;

	@FindBy(id = "{wizid}_dialog_lh_shareName")
	private WebElement shareNameCheckBox;

	@FindBy(id = "{wizid}_dialog_lh_shareEmail")
	private WebElement shareEmailCheckBox;

	private final String wizid;

	public LTICommonControlContainerImpl(UniversalControl control, String wizid)
	{
		super(control.getContext(), By.id("wizard-controls"), 0);
		this.wizid = wizid;
	}

	public void setName(String attachmentNewName)
	{
		getNameField().clear();
		getNameField().sendKeys(attachmentNewName);
	}

	/**
	 * if null, leave unchanged, else wipe and send keys; hence empty string
	 * means 'clear'. index values <= 0 means leave unchanged.
	 * 
	 * @param attachmentNewName
	 * @param selectionIndex
	 * @param launchUrl
	 */
	public void enterValues(String attachmentNewName, int selectionIndex, String launchUrl)
	{
		if( attachmentNewName != null )
		{
			getNameField().clear();
			getNameField().sendKeys(attachmentNewName);
		}

		if( selectionIndex >= 1 )
		{
			getConfiguredToolSelector().selectByIndex(selectionIndex);
		}

		if( launchUrl != null )
		{
			getLaunchUrlEditBox().clear();
			getLaunchUrlEditBox().sendKeys(launchUrl);
		}
	}

	/**
	 * if null, leave unchanged, else wipe and send keys; hence empty string
	 * means 'clear'. Likewise Boolean objects being null means ignore.
	 * 
	 * @param consumerKey
	 * @param sharedSecret
	 * @param customParams
	 * @param defaultPrivacy
	 * @param shareName
	 * @param shareEmail
	 */
	public void enterAdvancedValues(String consumerKey, String sharedSecret, String customParams,
		Boolean defaultPrivacy, Boolean shareName, Boolean shareEmail)
	{
		if( consumerKey != null )
		{
			getConsumerKeyEditBox().clear();
			getConsumerKeyEditBox().sendKeys(consumerKey);
		}

		if( sharedSecret != null )
		{
			getSharedSecretEditBox().clear();
			getSharedSecretEditBox().sendKeys(sharedSecret);
		}

		if( customParams != null )
		{
			getCustomParamsEditBox().clear();
			getCustomParamsEditBox().sendKeys(customParams);
		}

		if( defaultPrivacy != null )
		{
			boolean defaultPrivacyBool = defaultPrivacy.booleanValue();
			if( defaultPrivacyBool != getUseDefaultPrivacyCheckBox().isSelected() )
			{
				getUseDefaultPrivacyCheckBox().click();
			}
		}

		if( shareName != null )
		{
			boolean shareNameBool = shareName.booleanValue();
			if( shareNameBool != getShareNameCheckBox().isSelected() )
			{
				getShareNameCheckBox().click();
			}
		}

		if( shareEmail != null )
		{
			boolean shareEmailBool = shareEmail.booleanValue();
			if( shareEmailBool != getShareEmailCheckBox().isSelected() )
			{
				getShareEmailCheckBox().click();
			}
		}
	}

	private EquellaSelect getConfiguredToolSelector()
	{
		return new EquellaSelect(context, getSelectProvider());
	}

	public String getWizid()
	{
		return wizid;
	}

	@Override
	public String getControlId(int ctrlNum)
	{
		return "p" + pageNum + "c" + ctrlNum;
	}

	protected WebElement getMainDiv()
	{
		return mainDiv;
	}

	protected WebElement getNameField()
	{
		return nameField;
	}

	protected WebElement getSelectProvider()
	{
		return selectProvider;
	}

	protected WebElement getLaunchUrlEditBox()
	{
		return launchUrlEditBox;
	}

	protected WebElement getCustomParamsEditBox()
	{
		return customParamsEditBox;
	}

	protected WebElement getConsumerKeyEditBox()
	{
		return consumerKeyEditBox;
	}

	protected WebElement getSharedSecretEditBox()
	{
		return sharedSecretEditBox;
	}

	protected WebElement getIconUrlEditBox()
	{
		return iconUrlEditBox;
	}

	protected WebElement getUseDefaultPrivacyCheckBox()
	{
		return useDefaultPrivacyCheckBox;
	}

	protected WebElement getShareNameCheckBox()
	{
		return shareNameCheckBox;
	}

	protected WebElement getShareEmailCheckBox()
	{
		return shareEmailCheckBox;
	}
}
