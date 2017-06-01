/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
