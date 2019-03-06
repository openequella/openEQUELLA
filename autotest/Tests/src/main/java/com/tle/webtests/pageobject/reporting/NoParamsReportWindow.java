package com.tle.webtests.pageobject.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractReport;

public class NoParamsReportWindow<R extends AbstractReport<R>> extends AbstractReportWindow<R, NoParamsReportWindow<R>>
{
	public NoParamsReportWindow(PageContext context, R report)
	{
		super(context, report);
	}
}
