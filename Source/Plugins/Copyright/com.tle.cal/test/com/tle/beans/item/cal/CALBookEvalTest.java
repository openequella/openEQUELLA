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

public class CALBookEvalTest
{
	@Test
	public void testBookPerCourse()
	{
		CALHolding holding = createMockHolding(false);
		CALValidation evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setPerCourseValidation(true);
		assertTrue(evaluator.isValid());
		holding = createMockHolding(false);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setPerCourseValidation(true);
		assertTrue(evaluator.isValid());
	}

	@Test
	public void testBookInstWide()
	{
		CALHolding holding = createMockHolding(false);
		CALValidation evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		assertFalse(evaluator.isValid());
		holding = createMockHolding(true);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		assertTrue(evaluator.isValid());
	}

	@Test
	public void testRestrictiveBook()
	{
		CALHolding holding = createMockHolding(false);
		CALValidation evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setRestrictiveValidation(true);
		assertFalse(evaluator.isValid());
		holding = createMockHolding(true);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setRestrictiveValidation(true);
		assertFalse(evaluator.isValid());
		holding = createMockHolding(true, true);
		evaluator = new CALValidation(holding, createMockActivateRequests(holding), null);
		evaluator.setRestrictiveValidation(true);
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

	private CALHolding createMockHolding(boolean totalUnder10)
	{
		return createMockHolding(totalUnder10, false);
	}

	@SuppressWarnings("nls")
	private CALHolding createMockHolding(boolean totalUnder10, boolean nonCopyright)
	{
		CALHolding holding = new CALHolding();
		holding.setType("book");
		holding.setLength("100");

		CALPortion portion1 = new CALPortion();
		portion1.setItem(createMockItem1());
		CALSection section1 = new CALSection();
		if( totalUnder10 )
		{
			section1.setRange("1-5");
		}
		else
		{
			section1.setRange("1-10");
		}
		section1.setPortion(portion1);
		portion1.setSections(Arrays.asList(section1));

		CALPortion portion2 = new CALPortion();
		portion2.setItem(createMockItem2());
		CALSection section2 = new CALSection();
		if( nonCopyright )
		{
			section2.setCopyrightStatus("free");
		}

		section2.setPortion(portion2);
		if( totalUnder10 )
		{
			section2.setRange("6-10");
		}
		else
		{
			section2.setRange("11-20");
		}
		portion2.setSections(Arrays.asList(section2));

		holding.setPortions(Arrays.asList(portion1, portion2));
		return holding;
	}
}