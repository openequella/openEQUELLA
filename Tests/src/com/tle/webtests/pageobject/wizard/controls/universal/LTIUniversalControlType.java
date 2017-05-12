/**
 * 
 */
package com.tle.webtests.pageobject.wizard.controls.universal;

import org.openqa.selenium.WebElement;

import com.tle.common.Check;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

/**
 * @author larry
 *
 */
public class LTIUniversalControlType extends AbstractUniversalControlType<LTIUniversalControlType>
{

	private final LTICommonControlContainerImpl ccc;

	public LTIUniversalControlType(UniversalControl control)
	{
		super(control);
		ccc = new LTICommonControlContainerImpl(control, getWizid());
	}

	public UniversalControl addPage(int indexToolProvider, String launchUrl, String attachmentName)
	{
		openPage(indexToolProvider, launchUrl);
		onwardToEditPage();
		ccc.setName(attachmentName);
		GenericAttachmentEditPage editingPage = edit();
		return editingPage.save();
	}

	public LTIUniversalControlType openPage(int indexToolProvider, String launchUrl)
	{
		getConfiguredToolSelector().selectByIndex(indexToolProvider);
		if( !Check.isEmpty(launchUrl) )
		{
			ccc.getLaunchUrlEditBox().sendKeys(launchUrl);
		}
		return this;
	}

	/**
	 * @see LTICommonControlContainerImpl#enterValues(String, int, String)
	 * @param attachmentNewName
	 * @param selectionIndex
	 * @param launchUrl
	 */
	public void enterValues(String attachmentNewName, int selectionIndex, String launchUrl)
	{
		ccc.enterValues(attachmentNewName, selectionIndex, launchUrl);
	}

	/**
	 * @see LTICommonControlContainerImpl#enterAdvancedValues(String, String,
	 *      String, Boolean, Boolean, Boolean)
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
		ccc.enterAdvancedValues(consumerKey, sharedSecret, customParams, defaultPrivacy, shareName, shareEmail);
	}

	protected LTIUniversalControlType onwardToEditPage()
	{
		addButton.click();
		waitForElement(ccc.getNameField());
		return get();
	}

	private EquellaSelect getConfiguredToolSelector()
	{
		return new EquellaSelect(context, ccc.getSelectProvider());
	}

	public UniversalControl add(String newPage)
	{
		addButton.click();
		return control.get();
	}

	@Override
	public String getType()
	{
		return "External tool provider (LTI)";
	}

	@Override
	public WebElement getFindElement()
	{
		return ccc.getMainDiv();
	}

	@Override
	public WebElement getNameField()
	{
		return ccc.getNameField();
	}
}
