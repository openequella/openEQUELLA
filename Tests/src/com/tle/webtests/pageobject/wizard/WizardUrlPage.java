package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.viewitem.ItemId;

public class WizardUrlPage extends AbstractPage<WizardPageTab>
{
	private final ItemId itemId;

	public WizardUrlPage(PageContext context, ItemId itemId)
	{
		super(context);
		this.itemId = itemId;
	}

	public WizardPageTab edit()
	{
		get("access/runwizard.do", "method", "loaditem", "edit", "true", "uuid", itemId.getUuid(), "version",
			itemId.getVersion());
		return new WizardPageTab(context, 0).get();
	}
}
