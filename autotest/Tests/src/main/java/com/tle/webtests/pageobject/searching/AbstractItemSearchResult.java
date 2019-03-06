package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.viewitem.ModifyKeyResourcePage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public abstract class AbstractItemSearchResult<SR extends AbstractItemSearchResult<SR>> extends SearchResult<SR>
{

	protected AbstractItemSearchResult(AbstractResultList<?, ?> page, SearchContext relativeTo, By by)
	{
		super(page, relativeTo, by);
	}

	public <T extends PageObject> T viewSummary(WaitingPageObject<T> targetPage)
	{
		clickTitle();
		return targetPage.get();
	}

	public SummaryPage viewSummary()
	{
		clickTitle();
		return new SummaryPage(context).get();
	}

	public FavouriteItemDialog<SR> addToFavourites()
	{
		resultDiv.findElement(By.xpath(".//a[@title='Add to favourites']")).click();
		return new FavouriteItemDialog<SR>(context, updateWaiter()).get();
	}

	public ModifyKeyResourcePage addToHierarchy()
	{
		resultDiv.findElement(By.xpath(".//a[@title='Add to hierarchy']")).click();
		return new ModifyKeyResourcePage(context).get();
	}

	public AbstractResultList<?, ?> removeFavourite()
	{
		return removeFavourite(resultPage.getUpdateWaiter());
	}

	public <T extends PageObject> T removeFavourite(WaitingPageObject<T> returnTo)
	{
		WaitingPageObject<T> waiter = ReceiptPage.waiter("Successfully removed from favourites", returnTo);
		resultDiv.findElement(By.xpath(".//a[@title='Remove from favourites']")).click();
		acceptConfirmation();
		return waiter.get();
	}

	public boolean isFavouriteItem()
	{
		try
		{
			resultDiv.findElement(By.xpath(".//a[@title='Remove from favourites']"));
			return true;
		}
		catch( NotFoundException nfe )
		{

			return false;
		}
	}

	/**
	 * Expect to be true of scrapbook items in a result set, but not otherwise.
	 * 
	 * @return
	 */
	public boolean isEditDeletableItem()
	{
		try
		{
			resultDiv.findElement(EBy.buttonText("Edit"));
			resultDiv.findElement(EBy.buttonText("Delete"));
			return true;
		}
		catch( NotFoundException nfe )
		{

			return false;
		}
	}

	public boolean hasResubscribeButton()
	{
		try
		{
			resultDiv.findElement(By.xpath(".//button[text() = 'Resubscribe']"));
			return true;
		}
		catch( NotFoundException e )
		{
			return false;
		}
	}

	public boolean attachmentListOpen()
	{
		return isPresent(resultDiv, By.className("attcontainer"));
	}

}
