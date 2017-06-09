package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.ValueThoroughIterator;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.soap.SoapInterfaceV1;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.test.cal.soap.CALSoapActivationService;
import com.tle.webtests.test.cal.soap.SoapCourseService;
import com.tle.webtests.test.files.Attachments;

public class CALSoapInterfaceTest extends AbstractCALTest
{
	public CALSoapInterfaceTest()
	{
		super("CAL SoapTest");
	}

	private SoapInterfaceV1 soapService;
	private SoapCourseService calCourseService;
	private CALSoapActivationService calActivationService;

	@Override
	protected void prepareBrowserSession()
	{
		logon("caladmin", "``````");
	}

	@Test
	public void activateAttachments() throws Exception
	{
		String[] attachments;

		createBook("Book");
		String defaultPortion = "Portion 1";
		createPortion("1", defaultPortion, "Book", 1, 10, 2);

		// activate attachments using soap service - successful activation
		String itemUuid = searchSoapItem(soapService, defaultPortion);
		attachments = getItemAndAttachments(itemUuid);
		calActivationService.activateItemAttachments(itemUuid, 1, "1234", attachments);

		CALSummaryPage summaryPage = searchAndView("Book");
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));
		assertTrue(summaryPage.isActive(1, ATTACH2_FILENAME));

		createPortion("2", "Portion 2", "Book", 1, 20, 2);

		// activate attachments using soap service - successful inactive because
		// of CALViolationException
		itemUuid = searchSoapItem(soapService, "Portion 2");
		attachments = getItemAndAttachments(itemUuid);
		try
		{
			calActivationService.activateItemAttachments(itemUuid, 1, "1234", attachments);
			throw new Error("Should be violation");
		}
		catch( Exception fault )
		{
			searchAndView("Book");
			assertTrue(summaryPage.isInactive(2, ATTACH1_FILENAME));
			assertTrue(summaryPage.isInactive(2, ATTACH2_FILENAME));
		}
	}

	@Test
	public void importCourses() throws Exception
	{
		// enumerate courseIds
		List<String> courseCodes = getCalCourseEnumerationCodes();

		HashMap<String, String> courseCodeMap = new HashMap<String, String>();

		courseCodeMap.put("1234", "A Simple Course");
		courseCodeMap.put("5678", "Rollover Test Course");

		for( String courseCode : courseCodes )
		{
			// get a course by code
			String course = calCourseService.getCourse(courseCode);
			assertNotNull(course);
			PropBagEx course_prop = new PropBagEx(course);
			assertEquals(course_prop.getNode("code"), courseCode);
			String courseName = courseCodeMap.get(courseCode);
			if( courseName != null )
			{
				assertEquals(course_prop.getNode("name/strings/entry/com.tle.beans.entity.LanguageString/text"),
					courseName);
			}
		}

		// bulk import
		calCourseService.bulkImport(readFileAsString("courses.csv"));

		// get the course delete it if existing
		courseCodes = getCalCourseEnumerationCodes();
		if( calCourseService.getCourse("C001") != null )
		{
			assertTrue(courseCodes.contains("C001"));
			calCourseService.delete("C001"); // added using bulkImport
			courseCodes = getCalCourseEnumerationCodes();
			assertFalse(courseCodes.contains("C001"));
		}

		// add the course
		calCourseService.addCourse(readFileAsString("sampleAddCourse.xml"));
		PropBagEx course9999 = new PropBagEx(calCourseService.getCourse("9999")); // added
		// using
		// add
		// course
		assertEquals("9999", course9999.getNode("code"));
		String departmentName = "Department of Test Courses using - has been added";
		assertEquals(departmentName, course9999.getNode("departmentName"));

		// edit the course
		calCourseService.editCourse(readFileAsString("sampleEditCourse.xml"));
		course9999 = new PropBagEx(calCourseService.getCourse("9999"));
		assertEquals("9999", course9999.getNode("code"));
		departmentName = "Department of Test Courses using - has been edited";
		assertEquals(departmentName, course9999.getNode("departmentName"));

		// get the course delete it if existing
		courseCodes = getCalCourseEnumerationCodes();
		if( calCourseService.getCourse("9999") != null )
		{
			assertTrue(courseCodes.contains("9999"));
			calCourseService.delete("9999"); // added using addCourse
			courseCodes = getCalCourseEnumerationCodes();
			assertFalse(courseCodes.contains("9999"));
		}
	}

	private String readFileAsString(String filename)
	{

		InputStreamReader reader;
		StringWriter writer = new StringWriter();
		try
		{
			InputStream stream = new FileInputStream(new File(Attachments.get(filename).toURI()));
			reader = new InputStreamReader(stream, "UTF-8");
			char[] buf = new char[1024];
			int len;
			while( (len = reader.read(buf)) != -1 )
			{
				writer.write(buf, 0, len);
			}
			reader.close();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	private List<String> getCalCourseEnumerationCodes()
	{
		return Arrays.asList(calCourseService.enumerateCourseCodes());
	}

	private String[] getItemAndAttachments(String itemUuid) throws Exception
	{
		String item = soapService.getItem("", itemUuid, 1, "", "*");
		PropBagEx prop = new PropBagEx(item);

		List<String> attachmentList = new ArrayList<String>();

		ValueThoroughIterator iter = prop
			.iterateAllValues("item/copyright/portions/portion/sections/section/attachment");
		while( iter.hasNext() )
		{
			attachmentList.add(iter.next());
		}
		return attachmentList.toArray(new String[attachmentList.size()]);
	}

	private String searchSoapItem(SoapInterfaceV1 soapService, String defaultPortion) throws Exception
	{
		String searchReq = "<com.dytech.edge.common.valuebean.SearchRequest>" + "	<query>" + "&quot;"
			+ context.getFullName(defaultPortion) + "&quot;" + "	</query>" + "	<select>" + "		*" + "	</select>"
			+ "	<orderby>" + "		/xml/item/name" + "	</orderby>" + "	<where>" + "	</where>" + "	<onlyLive>" + "		true"
			+ "	</onlyLive>" + "</com.dytech.edge.common.valuebean.SearchRequest>";
		String searchItems = soapService.searchItems("", searchReq, 0, 5);
		PropBagEx searchProp = new PropBagEx(searchItems);

		return searchProp.getNode("result/xml/item/@id");
	}

	@BeforeClass
	public void initialSetup()
	{
		SoapHelper soapHelper = new SoapHelper(context);
		soapService = soapHelper.createSoap(SoapInterfaceV1.class, "services/SoapInterfaceV1",
			"http://remoting.core.tle.com", null);
		soapService.login("caladmin", "``````");
		calCourseService = soapHelper.createSoap(SoapCourseService.class, "services/calcourses.service",
			"http://soap.remoting.web.tle.com", soapService);
		calActivationService = soapHelper.createSoap(CALSoapActivationService.class, "services/calactivation.service",
			"http://service.web.cal.tle.com", soapService);
	}

}
