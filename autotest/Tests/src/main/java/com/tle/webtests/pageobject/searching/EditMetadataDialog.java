package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class EditMetadataDialog extends AbstractPage<EditMetadataDialog>
{
	@FindBy(id = "bemo_ca")
	private WebElement chooseAction;
	@FindBy(id = "bemo_am")
	private WebElement addMod;
	@FindBy(id = "bemo_scd")
	private WebElement schemaSelect;
	@FindBy(id = "bemo_al")
	private WebElement actionSelect;
	@FindBy(id = "bemo_fnd")
	private WebElement findTextfield;
	@FindBy(id = "bemo_rpc")
	private WebElement replaceTextfield;
	@FindBy(id = "bemo_sa")
	private WebElement saveAction;
	@FindBy(id = "bemo_stf")
	private WebElement setTextField;
	@FindBy(id = "bemo_xml")
	private WebElement addXmlTextField;
	@FindBy(id = "bemo_mdt")
	private WebElement modificationTable;

	@FindBy(id = "bemo_to_0")
	private WebElement alwaysSetRadio;
	@FindBy(id = "bemo_to_1")
	private WebElement setExistsRadio;
	@FindBy(id = "bemo_to_2")
	private WebElement setNotExistRadio;

	@FindBy(id = "metadata-edit")
	private WebElement ajaxDiv;
	@FindBy(id = "selected-nodes")
	private WebElement treeAjaxDiv;
	@FindBy(id = "action-form")
	private WebElement actionAjaxDiv;
	@FindBy(className = "schema-selection")
	private WebElement schemaSelectionDiv;

	@FindBy(xpath = "//h3[normalize-space(text())='Choose an action to perform']")
	private WebElement chooseActionTitle;
	@FindBy(xpath = "//*[@id='action-form']/div[contains(@class, 'settingRow')]")
	private WebElement firstActionSetting;

	private BulkActionDialog dialog;

	public EditMetadataDialog(BulkActionDialog dialog)
	{
		super(dialog.getContext(), By.id("metadata-edit"));
		this.dialog = dialog;
	}

	public EditMetadataDialog addModification()
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdateExpect(ajaxDiv, schemaSelectionDiv);
		addMod.click();
		return ajaxUpdate.get();
	}

	public EditMetadataDialog selectSchema(String schemaName)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(ajaxDiv);
		new EquellaSelect(context, schemaSelect).selectByVisibleText(schemaName);
		return ajaxUpdate.get();
	}

	public EditMetadataDialog selectNode(String node)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(treeAjaxDiv);
		WebElement findElement = waitForElement(By.xpath("//li/div[text() = " + quoteXPath(node) + "]/a"));
		findElement.click();
		return ajaxUpdate.get();
	}

	public EditMetadataDialog selectRepeatedNode(String node, int which)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(treeAjaxDiv);
		By xpath = By.xpath("//li/div[text() = " + quoteXPath(node) + "]/a");
		waitForElement(xpath);
		WebElement webElement = driver.findElements(xpath).get(which - 1);
		webElement.click();
		return ajaxUpdate.get();
	}

	public EditMetadataDialog chooseAction()
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdateExpect(ajaxDiv, chooseActionTitle);
		chooseAction.click();
		return ajaxUpdate.get();
	}

	private void selectAction(String action)
	{

		EquellaSelect actionSel = new EquellaSelect(context, actionSelect);
		if (!actionSel.getSelectedValue().equals(action))
		{
			WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdateExpect(actionAjaxDiv, firstActionSetting);
			actionSel.selectByValue(action);
			ajaxUpdate.get();
		}
	}

	private void saveAction()
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdateExpect(ajaxDiv, modificationTable);
		saveAction.click();
		ajaxUpdate.get();
	}

	public void findAndReplace(String find, String replace)
	{
		selectAction("replace");

		findTextfield.sendKeys(find);
		replaceTextfield.sendKeys(replace);
		saveAction();
	}

	/**
	 * @param text text to set
	 * @param option 1: always set text <br/>
	 *            2: set if node exists <br/>
	 *            3: set if node doesn't exist <br/>
	 */
	public void setText(String text, int option)
	{
		selectAction("set");

		waiter.until(ExpectedConditions2.presenceOfElement(alwaysSetRadio));
		switch( option )
		{
			case 1:
				alwaysSetRadio.click();
				break;
			case 2:
				setExistsRadio.click();
				break;
			case 3:
				setNotExistRadio.click();
				break;
			default:
				break;
		}

		setTextField.sendKeys(text);
		saveAction();
	}

	public void addNode(String xml)
	{
		selectAction("add");
		addXmlTextField.sendKeys(xml);
		saveAction();
	}

	public void chooseActionNoNodes()
	{
		chooseAction.click();
		waiter.until(ExpectedConditions2.acceptAlert());
	}

	public BulkResultsPage execute()
	{
		return dialog.preview().execute();
	}

	public EditMetadataDialog changeActionOrder(int index, boolean moveUp)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(ajaxDiv);
		int buttonPos = moveUp ? 3 : 4;
		modificationTable.findElement(By.xpath("tbody/tr[" + index + "]/td[3]/a[" + buttonPos + "]")).click();
		return ajaxUpdate.get();

	}

	public EditMetadataDialog editAction(int index)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(ajaxDiv);
		modificationTable.findElement(By.xpath("tbody/tr[" + index + "]/td[3]/a[1]")).click();
		return ajaxUpdate.get();
	}

	public EditMetadataDialog deleteModification(int index)
	{
		WaitingPageObject<EditMetadataDialog> ajaxUpdate = ajaxUpdate(ajaxDiv);
		modificationTable.findElement(By.xpath("tbody/tr[" + index + "]/td[3]/a[2]")).click();
		acceptAlert();
		return ajaxUpdate.get();
	}

	public BulkPreviewPage preview()
	{
		return dialog.preview();
	}
}
