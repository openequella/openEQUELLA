package com.tle.beans.item.cal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.beans.item.Item;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.cal.service.CALValidation;

public class CALJournalEvalTest
{
	@Test
	public void testJournalPerCourse()
	{
		CALHolding holding = createMockHolding(false);
		CALValidation evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setPerCourseValidation(true);
		assertTrue(evaluator.isValid());
		holding = createMockHolding(true);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setPerCourseValidation(true);
		assertTrue(evaluator.isValid());
	}

	@Test
	public void testJournalInstWide()
	{
		CALHolding holding = createMockHolding(false);
		CALValidation evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		assertFalse(evaluator.isValid());
		holding = createMockHolding(true);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		assertTrue(evaluator.isValid());
	}

	private List<ActivateRequest> createMockActivateRequests(CALHolding holding)
	{
		List<ActivateRequest> activateRequests = new ArrayList<ActivateRequest>();

		Date fromDate = new Date();
		Date toDate = new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24));
		ActivateRequest request1 = new ActivateRequest();
		request1.setId(1234);
		List<CALPortion> portions = holding.getCALPortions();
		request1.setItem(portions.get(0).getItem());
		request1.setCourse(createNewCourse(1));
		request1.setFrom(fromDate);
		request1.setUntil(toDate);
		activateRequests.add(request1);

		ActivateRequest request2 = new ActivateRequest();
		request2.setId(5678);
		request2.setItem(portions.get(1).getItem());
		request2.setCourse(createNewCourse(2));
		request2.setFrom(fromDate);
		request2.setUntil(toDate);
		activateRequests.add(request2);
		return activateRequests;
	}

	private CourseInfo createNewCourse(long courseId)
	{
		CourseInfo courseInfo = new CourseInfo();
		courseInfo.setId(courseId);
		courseInfo.setCode(Long.toString(courseId));
		return courseInfo;
	}

	private Item createMockItem1()
	{
		Item item1 = new Item();
		item1.setId(12345678);
		return item1;
	}

	private Item createMockItem2()
	{
		Item item1 = new Item();
		item1.setId(87654321);
		return item1;
	}

	private CALHolding createMockHolding(boolean commonTopic)
	{
		CALHolding holding = new CALHolding();
		holding.setType("journal");

		CALPortion portion1 = new CALPortion();
		portion1.setItem(createMockItem1());
		portion1.setTopics(Arrays.asList("Frogs"));
		CALSection section1 = new CALSection();
		section1.setPortion(portion1);
		portion1.setSections(Arrays.asList(section1));

		CALPortion portion2 = new CALPortion();
		portion2.setItem(createMockItem2());
		CALSection section2 = new CALSection();

		if( commonTopic )
		{
			portion2.setTopics(Arrays.asList("Frogs", "Not Frogs"));
		}
		else
		{
			portion2.setTopics(Arrays.asList("Not Frogs"));
		}
		section2.setPortion(portion2);
		portion2.setSections(Arrays.asList(section2));

		holding.setPortions(Arrays.asList(portion1, portion2));
		return holding;
	}

}
