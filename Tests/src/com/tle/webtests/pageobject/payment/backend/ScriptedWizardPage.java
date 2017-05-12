/**
 * 
 */
package com.tle.webtests.pageobject.payment.backend;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

/**
 * @author larry
 */
public class ScriptedWizardPage extends AbstractWizardControlPage<ScriptedWizardPage>
{

	@FindBy(id = "c1")
	private WebElement nameInputEditBox;

	@FindBy(id = "c2")
	private WebElement descriptionInputEditBox;

	@FindBy(className = "ptieruuid")
	private WebElement purchaseTierUuidInputEditBox;

	@FindBy(className = "substieruuid")
	private WebElement subscriptionTierUuidInputEditBox;

	@FindBy(className = "isfreeenabled")
	private WebElement freeEnabled;

	@FindBy(className = "ispurchaseenabled")
	private WebElement purEnabled;

	@FindBy(className = "issubscriptionenabled")
	private WebElement subEnabled;

	public ScriptedWizardPage(PageContext context)
	{
		super(context, By.id("wizard-controls"), 0);
	}

	public String getInputName()
	{
		return nameInputEditBox.getText();
	}

	public void setInputName(String name)
	{
		nameInputEditBox.sendKeys(name);
	}

	public String getInputDescription()
	{
		return descriptionInputEditBox.getText();
	}

	public void setInputDescription(String description)
	{
		descriptionInputEditBox.sendKeys(description);
	}

	public String getInputPurchaseTierUuid()
	{
		return purchaseTierUuidInputEditBox.getText();
	}

	public void setInputPurchaseTierUuid(String tierUuid)
	{
		purchaseTierUuidInputEditBox.clear();
		purchaseTierUuidInputEditBox.sendKeys(tierUuid);
	}

	public String getInputSubscriptionTierUuid()
	{
		return subscriptionTierUuidInputEditBox.getText();
	}

	public void setInputSubscriptionTierUuid(String tierUuid)
	{
		subscriptionTierUuidInputEditBox.clear();
		subscriptionTierUuidInputEditBox.sendKeys(tierUuid);
	}

	public boolean isFreeEnabled()
	{
		return freeEnabled.getText().equalsIgnoreCase("yes");
	}

	public boolean isPurchaseEnabled()
	{
		return purEnabled.getText().equalsIgnoreCase("yes");
	}

	public boolean isSubscriptionEnabled()
	{
		return subEnabled.getText().equalsIgnoreCase("yes");
	}

	@Override
	public String getControlId(int ctrlNum)
	{
		throw new RuntimeException("Not appropriate for this subclass");
	}

	/**
	 * The wizard in the associated collection is scripted to present details
	 * for all available purchase tiers
	 * 
	 * @param index
	 * @return
	 */
	public String getPurchaseTierUuidByName(String tierName)
	{
		WebElement purchaseTierProperties = driver.findElement(By.className("purchasetierproperties"));
		List<WebElement> tierNames = purchaseTierProperties.findElements(By
			.xpath("descendant::li/font[@class='tierName']"));
		for( WebElement elem : tierNames )
		{
			if( tierName.equals(elem.getText()) )
			{
				String retval = elem.findElement(By.xpath("parent::li/parent::ul/li/font[@class='tierUuid']"))
					.getText();
				return retval;
			}
		}
		return null;
	}

	public String getSubscriptionTierUuidByName(String tierName)
	{
		WebElement purchaseTierProperties = driver.findElement(By.className("purchasetierproperties"));
		List<WebElement> tierNames = purchaseTierProperties.findElements(By
			.xpath("descendant::li/font[@class='tierName']"));
		for( WebElement elem : tierNames )
		{
			if( tierName.equals(elem.getText()) )
			{
				String retval = elem.findElement(By.xpath("parent::li/parent::ul/li/font[@class='tierUuid']"))
					.getText();
				return retval;
			}
		}
		return null;
	}
}
