package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class CALInactiveViolation extends AbstractPage<CALInactiveViolation>
{

	public CALInactiveViolation(PageContext context)
	{
		super(context, By.xpath("//h2[normalize-space(text())='Copyright problem']"));
	}

	public boolean isInactive()
	{
		return isPresent(loadedBy);
	}

}
