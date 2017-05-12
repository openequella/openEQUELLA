package com.tle.webtests.pageobject.payment.backend;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

/**
 * @author Aaron
 */
public class ShowRegionsPage extends AbstractShowEntitiesPage<ShowRegionsPage>
{
	private final static String ALERT_DELETE_FAIL = "This region is in use and cannot be deleted";

	public ShowRegionsPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getSectionId()
	{
		return "sr";
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/region.do");
	}

	@Override
	protected String getH2Title()
	{
		return "Regions";
	}

	@Override
	protected String getEmptyText()
	{
		return "There are no editable regions";
	}

	public void deleteRegionFail(PrefixedName name)
	{
		deleteEntityFail(name, ALERT_DELETE_FAIL);
	}

	public EditRegionPage createRegion()
	{
		return createEntity(new EditRegionPage(this));
	}

	public EditRegionPage editRegion(PrefixedName region)
	{
		return editEntity(new EditRegionPage(this), region);
	}

	public EditRegionPage cloneRegion(PrefixedName region)
	{
		return cloneEntity(new EditRegionPage(this), region);
	}
}
