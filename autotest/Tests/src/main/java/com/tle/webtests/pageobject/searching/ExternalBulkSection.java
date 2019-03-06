package com.tle.webtests.pageobject.searching;

import com.tle.webtests.pageobject.AbstractPage;

public class ExternalBulkSection extends AbstractPage<ExternalBulkSection>
{
	private final BulkSection bulk;

	public ExternalBulkSection(BulkSection bulk)
	{
		super(bulk.getContext());
		this.bulk = bulk;
	}

	public ExternalMoveDialog move()
	{
		BulkActionDialog dialog = bulk.executeCommandPage("move");
		return new ExternalMoveDialog(dialog).get();
	}

}
