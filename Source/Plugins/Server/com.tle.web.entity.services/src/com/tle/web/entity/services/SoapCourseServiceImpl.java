package com.tle.web.entity.services;

import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.EntityPack;
import com.tle.common.util.CsvReader;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.services.InitialiserService;
import com.tle.core.xstream.XmlService;

@Bind
@Singleton
public class SoapCourseServiceImpl implements SoapCourseService
{
	@Inject
	private CourseInfoService courseService;
	@Inject
	private XmlService xmlService;
	@Inject
	private InitialiserService initialiser;

	@Override
	public void addCourse(String courseXml)
	{
		final EntityPack<CourseInfo> pack = new EntityPack<CourseInfo>();
		pack.setEntity((CourseInfo) xmlService.deserialiseFromXml(getClass().getClassLoader(), courseXml));
		courseService.add(pack, false);
	}

	@Override
	public String getCourse(String courseCode)
	{
		CourseInfo courseInfo = initialiser.initialise(courseService.getByCode(courseCode));
		return xmlService.serialiseToXml(courseInfo);
	}

	@Override
	public void bulkImport(String csvFile)
	{
		courseService.bulkImport(new CsvReader(new StringReader(csvFile)), true);
	}

	@Override
	public void editCourse(String courseXml)
	{
		CourseInfo course = xmlService.deserialiseFromXml(getClass().getClassLoader(), courseXml);

		// strip the IDs from the bundles since these are deleted later
		clearBundleIds(course.getName());
		clearBundleIds(course.getDescription());

		courseService.edit(course);
	}

	private void clearBundleIds(LanguageBundle bundle)
	{
		if( bundle != null )
		{
			bundle.setId(0);
			for( final LanguageString string : bundle.getStrings().values() )
			{
				string.setId(0);
			}
		}
	}

	@Override
	public void delete(String courseCode)
	{
		courseService.delete(courseService.getByCode(courseCode), true);
	}

	@Override
	public String[] enumerateCourseCodes()
	{
		final List<CourseInfo> allCourses = courseService.enumerate();
		final String[] courseIds = new String[allCourses.size()];
		int i = 0;
		for( final CourseInfo course : allCourses )
		{
			courseIds[i] = course.getCode();
			i++;
		}
		return courseIds;
	}

	@Override
	public void archiveCourse(String courseCode)
	{
		courseService.archive(courseService.getByCode(courseCode));
	}

	@Override
	public void unarchiveCourse(String courseCode)
	{
		courseService.unarchive(courseService.getByCode(courseCode));
	}
}
