package com.tle.webtests.pageobject.generic.entities;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;

/**
 * @author Aaron
 */
public abstract class AbstractEditEntityPage<THIS extends AbstractEditEntityPage<THIS, SHOWLISTPAGE>, SHOWLISTPAGE extends AbstractShowEntitiesPage<SHOWLISTPAGE>>
	extends
		AbstractPage<THIS>
{
	@FindBy(id = "{editorSectionId}_t")
	protected WebElement nameField;
	@FindBy(id = "{editorSectionId}_d")
	private WebElement descriptionField;

	@FindBy(id = "{contributeSectionId}_sv")
	protected WebElement saveButton;
	@FindBy(id = "{contributeSectionId}_cl")
	protected WebElement cancelButton;

	private SHOWLISTPAGE listPage;
	private boolean creating;

	protected AbstractEditEntityPage(SHOWLISTPAGE listPage)
	{
		super(listPage.getContext());
		this.listPage = listPage;
		setupLoadedBy();
	}

	private void setupLoadedBy()
	{
		final String entityName = getEntityName();
		loadedBy = By.xpath("//h2[text()='" + getTitle(creating) + entityName + "']");
	}

	protected String getTitle(boolean create)
	{
		return (create ? "Create new " : "Edit ");
	}

	@SuppressWarnings("unchecked")
	public THIS setCreating(boolean creating)
	{
		this.creating = creating;
		setupLoadedBy();
		return (THIS) this;
	}

	protected abstract String getEntityName();

	protected abstract String getContributeSectionId();

	protected abstract String getEditorSectionId();

	protected String invalidMessage(WebElement elem)
	{
		try
		{
			WebElement invalid = elem.findElement(By.xpath("..")).findElement(By.cssSelector(".ctrlinvalidmessage"));
			return invalid.getText();
		}
		catch( NoSuchElementException nse )
		{
			return null;
		}
	}

	protected boolean isInvalid(WebElement elem)
	{
		return invalidMessage(elem) != null;
	}

	// Public

	// TODO: use the WebElement
	public boolean isNameInvalid()
	{
		try
		{
			driver.findElement(By.xpath("//div[@id = '" + getEditorSectionId() + "_t']/following-sibling::p"));
			return true;
		}
		catch( NoSuchElementException e )
		{
			return false;
		}
	}

	public SHOWLISTPAGE save()
	{
		saveButton.click();
		return listPage.get();
	}

	public THIS saveWithErrors()
	{
		saveButton.click();
		return visibilityWaiter(driver, By.className("ctrlinvalidmessage")).get();
	}

	public SHOWLISTPAGE cancel()
	{
		cancelButton.click();
		return listPage.get();
	}

	public THIS setName(PrefixedName name)
	{
		return setName(name == null ? "" : name.toString());
	}

	/**
	 * Avoid using this directly
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected THIS setName(String name)
	{
		MultiLingualEditbox nameBox = new MultiLingualEditbox(context, nameField);
		if( name == null )
		{
			nameBox.setCurrentString("");
		}
		else
		{
			nameBox.setCurrentString(name);
		}
		return (THIS) this;
	}

	public String getName()
	{
		return new MultiLingualEditbox(context, nameField).getCurrentString();
	}

	public THIS setDescription(PrefixedName description)
	{
		return setDescription(description.toString());
	}

	@SuppressWarnings("unchecked")
	public THIS setDescription(String description)
	{

		MultiLingualEditbox descBox = new MultiLingualEditbox(context, descriptionField, true);
		descBox.setCurrentString(description);
		return (THIS) this;
	}

	public String getDescription()
	{

		return new MultiLingualEditbox(context, descriptionField, true).getCurrentString();
	}

	protected SHOWLISTPAGE getShowListPage()
	{
		return listPage;
	}
}
