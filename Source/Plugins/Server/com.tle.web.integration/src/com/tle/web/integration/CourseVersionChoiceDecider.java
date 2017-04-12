package com.tle.web.integration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.VersionSelection;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.section.VersionChoiceDecider;

@Bind
@Singleton
public class CourseVersionChoiceDecider implements VersionChoiceDecider
{
	@Inject
	private IntegrationService integrationService;
	@Inject
	private CourseInfoService courseInfoService;

	@Override
	public VersionSelection getVersionSelection(SectionInfo info)
	{
		IntegrationInterface integration = integrationService.getIntegrationInterface(info);
		if( integration != null )
		{
			String courseCode = integration.getCourseInfoCode();
			if( courseCode != null && courseInfoService.getByCode(courseCode) != null )
			{
				VersionSelection vs = courseInfoService.getByCode(courseCode).getVersionSelection();

				// Once INSTITUTION_DEFAULT has been deleted and the course
				// data migrated to be 'null', we can safely return the data
				// from the course without this check.
				return vs == VersionSelection.INSTITUTION_DEFAULT ? null : vs;
			}
		}
		return null;
	}
}
