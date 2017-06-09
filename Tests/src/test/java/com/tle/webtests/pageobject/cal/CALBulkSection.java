package com.tle.webtests.pageobject.cal;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkSection;

public class CALBulkSection extends AbstractPage<CALBulkSection>
{
	private final BulkSection bulk;

	public CALBulkSection(BulkSection bulk)
	{
		super(bulk.getContext());
		this.bulk = bulk;
	}

	public CALRolloverDialog rollover()
	{
		BulkActionDialog dialog = bulk.executeCommandPage("rollover");
		return new CALRolloverDialog(dialog).get();
	}

}
