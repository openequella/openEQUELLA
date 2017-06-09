package com.tle.webtests.test.cal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;

public class CALJournalRulesTest extends AbstractActivationsTest
{
	public CALJournalRulesTest()
	{
		super("JournalRules");
	}

	@Test
	public void testJournalSingleActivations()
	{
		context.setSubPrefix("JournalRulesSingle");
		String journalName = "First Journal";
		String journalPortion = "First Journal Portion";
		createJournal(journalName, "Volume1");
		createJournalPortion(journalPortion, "Basics of equella", journalName, 1, 5, 1);
		CALSummaryPage summary = searchAndView(journalName);
		String portionTitle = context.getFullName(journalPortion);
		CALActivatePage<CALSummaryPage> activatePage = summary.activateJournal(portionTitle, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(portionTitle, ATTACH1_FILENAME));
	}

	@Test
	public void testPerCourse()
	{
		context.setSubPrefix("JournalRulesPerCourse");
		String journalName = "First Journal";
		createJournal(journalName, "Volume1", "CAL Guide Journals Collection - Per Course");
		String journalPortion = "First Journal Portion";
		String journalPortion2 = "Second Journal Portion";
		createJournalPortion(journalPortion, "Basics of equella", journalName, 1, 5, 1);
		createJournalPortion(journalPortion2, "Different topic", journalName, 1, 5, 1);

		CALSummaryPage summary = searchAndView(journalName);
		String portionTitle = context.getFullName(journalPortion);
		String portionTitle2 = context.getFullName(journalPortion2);
		CALActivatePage<CALSummaryPage> activatePage = summary.activateJournal(portionTitle, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(portionTitle, ATTACH1_FILENAME));

		activatePage = summary.activateJournal(portionTitle2, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summary.isJournalInactive(portionTitle2, ATTACH1_FILENAME));

		activatePage = summary.activateJournal(portionTitle2, ATTACH1_FILENAME);
		activatePage.setCourse("Sample Test Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(portionTitle2, ATTACH1_FILENAME));
	}

	@Test
	public void testJournalMultipleActivations()
	{
		String journalName = "First Journal";
		createJournal(journalName, "Volume1");

		String journalPortion = "Portion 1";
		createJournalPortion(journalPortion, "Basics of equella", journalName, 1, 5, 2);
		String journalPortion2 = "Portion 2";
		createJournalPortion(journalPortion2, "Basics of equella", journalName, 1, 5, 1);
		String journalPortion3 = "Portion 3";
		createJournalPortion(journalPortion3, "Basics of CAL", journalName, 1, 5, 1);

		journalPortion = context.getFullName(journalPortion);
		journalPortion2 = context.getFullName(journalPortion2);
		journalPortion3 = context.getFullName(journalPortion3);
		CALSummaryPage summary = searchAndView(journalName);

		// activate two sections in same portion
		CALActivatePage<CALSummaryPage> activatePage = summary.activateJournal(journalPortion, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(journalPortion, ATTACH1_FILENAME));
		assertTrue(summary.isJournalInactive(journalPortion, ATTACH2_FILENAME));

		activatePage = summary.activateJournal(journalPortion, ATTACH2_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(journalPortion, ATTACH2_FILENAME));

		// activate two portions with same theme - no violations
		activatePage = summary.activateJournal(journalPortion2, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summary.isJournalActive(journalPortion2, ATTACH1_FILENAME));

		// activate three portions with different themes - violations apply of
		// no same theme
		activatePage = summary.activateJournal(journalPortion3, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summary.isJournalInactive(journalPortion3, ATTACH1_FILENAME));
	}
}
