package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;

public class SubWizardPage extends AbstractWizardControlPage<SubWizardPage>
{
	private final int treenum;
	private final int ctrlOffset;

	public SubWizardPage(PageContext context, AbstractWizardControlPage<?> page, int treenum, int ctrlOffset)
	{
		super(context, null, page.getPageNum());
		this.treenum = treenum;
		this.ctrlOffset = ctrlOffset;
	}

	@Override
	protected void checkLoadedElement()
	{
		// nothing
	}

	@Override
	public String getControlId(int ctrlNum)
	{
		return "p" + pageNum + "t" + treenum + "c" + getControlNum(ctrlNum);
	}

	@Override
	protected int getControlNum(int ctrlNum)
	{
		return ctrlNum + ctrlOffset;
	}

}
