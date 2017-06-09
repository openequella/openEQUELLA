package com.tle.webtests.test.cal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.CALWizardPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;

public class CALViolationTest extends AbstractCALTest
{
	private static final String CAL_VIOLATIONS = "CAL Violations Test";
	private static final String DEFAULT_COURSE = "A Simple Course";

	@Test
	public void testCALViolations()
	{
		logon("cal_COPYRIGHT_ITEM", "``````");
		String bookName = CAL_VIOLATIONS;
		createBook(bookName);
		createPortion("1", "Portion 1", bookName, 1, 5, 1);

		CALSummaryPage summary = searchAndView(bookName);
		assertTrue(summary.activateDefault(1, "page.html", DEFAULT_COURSE).isActive(1, "page.html"));
		createPortion("2", "Portion 2", bookName, 6, 10, 1);

		summary = searchAndView(bookName);
		assertTrue(summary.activateDefault(2, "page.html", DEFAULT_COURSE).isActive(2, "page.html"));

		// Now edit the item
		AdminTabPage adminTab = searchAndViewAdmin("Portion 2");
		CALWizardPage calWizardPage = new CALWizardPage(context, adminTab.edit());
		calWizardPage.setRange(0, "6-15");
		calWizardPage.saveWithViolation();
	}

}
