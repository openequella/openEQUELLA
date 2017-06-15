package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.cal.ManageActivationsPage;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;

public class CALActivationsMetadataTest extends AbstractActivationsTest
{

	private static final String BOOK = "metadata_book";
	private static final String ATTACHMENT = "page.html";
	private static final String ACTIVATIONS_NODE = "item/activations/activation";
	private static final String COURSE_NAME_NODE = ACTIVATIONS_NODE + "/coursename";
	private static final String ATTACHMENT_UUID_NODE = ACTIVATIONS_NODE + "/attachment";
	private static final String SIMPLE_COURSE = "A Simple Course";
	private static final String ROLLOVER_COURSE = "Rollover Test Course";

	public CALActivationsMetadataTest(String subPrefix)
	{
		super("activationMetadata");
	}

	@Test
	public void testActivationNodes()
	{
		logon("caladmin", "``````");
		createBook(BOOK);
		createPortion("1", "Portion 1", BOOK, 1, 5, 1);
		createPortion("2", "Portion 2", BOOK, 6, 10, 1);

		// activate ch 1
		CALSummaryPage summary = searchAndView(BOOK);
		summary = summary.activateDefault(1, ATTACHMENT, SIMPLE_COURSE);
		// activate then delete ch 2
		summary = searchAndView(BOOK);
		summary = summary.activateDefault(2, ATTACHMENT, ROLLOVER_COURSE);

		String portionTwo = context.getFullName("Portion 2");
		ManageActivationsPage map = new ManageActivationsPage(context).load();
		map.search(portionTwo);
		map.results().setChecked(portionTwo, true);
		assertTrue(map.bulk().executeCommand("delete"));
		// activate then deactivate ch 2
		summary = searchAndView(BOOK);
		summary = summary.activateDefault(2, ATTACHMENT, ROLLOVER_COURSE);
		map = new ManageActivationsPage(context).load();
		map.search(portionTwo);
		map.results().setChecked(portionTwo, true);
		assertTrue(map.bulk().executeCommand("deactivate"));
		summary = searchAndView("Portion 1");
		String attachmentUuid = summary.getAttachmentUuid(1, ATTACHMENT);
		// check xml
		summary = searchAndView(BOOK);

		ItemXmlPage xml = new ItemXmlPage(context, summary.getItemId()).load();
		assertEquals(xml.getNodeCount(ACTIVATIONS_NODE), 2);
		assertTrue(xml.nodeHasValue(ACTIVATIONS_NODE, "status", "Inactive"));
		assertTrue(xml.nodeHasValue(ACTIVATIONS_NODE, "status", "Active"));
		assertTrue(xml.nodeHasValue(COURSE_NAME_NODE, SIMPLE_COURSE));
		assertTrue(xml.nodeHasValue(COURSE_NAME_NODE, ROLLOVER_COURSE));
		assertTrue(xml.nodeHasValue(ATTACHMENT_UUID_NODE, attachmentUuid));
	}
}
