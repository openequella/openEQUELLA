package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.ActivationListPage;
import com.tle.webtests.pageobject.cal.CALRolloverDialog;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.ManageActivationsPage;

public class CALActivationsRolloverTest extends AbstractActivationsTest
{
	private static final String ROLLOVER_COURSE = "Rollover Test Course";
	private static final String ORIGINAL_COURSE = "A Simple Course";

	public CALActivationsRolloverTest()
	{
		super("CAL Activations Rollover");
	}

	@Test
	public void testActivationsRollover()
	{
		logon("caladmin", "``````");
		createBook("Book");
		createPortion("1", "Portion 1", "Book", 1, 10, 1);

		CALSummaryPage summaryPage = searchAndView("Book");
		summaryPage.activateDefault(1, ATTACH1_FILENAME, ORIGINAL_COURSE);
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

		ManageActivationsPage activations = new ManageActivationsPage(context).load();

		// single item activations rollover
		String portionName = context.getFullName("Portion 1");
		activations.search('"' + portionName + '"');
		ActivationListPage activationResults = activations.results();
		assertTrue(activationResults.isActive(portionName, ORIGINAL_COURSE));
		activations.results().setChecked(portionName, true);
		CALRolloverDialog rolloverDialog = activations.bulkcal().rollover();

		rolloverDialog.selectCourse(ROLLOVER_COURSE);
		assertTrue(rolloverDialog.execute(activationResults));

		assertTrue(activationResults.isShowing(portionName, ORIGINAL_COURSE));
		assertTrue(activationResults.isShowing(portionName, ROLLOVER_COURSE));

		// bulk all activations rollover using same course + dates
		activations.get();
		activations.search('"' + portionName + '"');
		activationResults = activations.results();
		activations.bulk().selectAll();
		rolloverDialog = activations.bulkcal().rollover();
		rolloverDialog.setRolloverDates(true);
		assertTrue(rolloverDialog.execute(activations));
		
		// 2 pending, 2 for orig, 2 for rollover
		assertEquals(activationResults.getTotalAvailable(), 4);
		activations.filterByStatus("Pending");
		assertEquals(activationResults.getTotalAvailable(), 2);
		activations.resetFilters();
		activations.filterByCourse(ORIGINAL_COURSE);
		assertEquals(activationResults.getTotalAvailable(), 2);
		activations.filterByCourse(ROLLOVER_COURSE);
		assertEquals(activationResults.getTotalAvailable(), 2);
	}

}
