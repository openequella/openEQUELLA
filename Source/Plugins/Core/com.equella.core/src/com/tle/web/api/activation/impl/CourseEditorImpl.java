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

package com.tle.web.api.activation.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.BindFactory;
import com.tle.web.api.activation.CourseBean;
import com.tle.web.api.activation.CourseEditor;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public class CourseEditorImpl extends AbstractBaseEntityEditor<CourseInfo, CourseBean> implements CourseEditor
{
	@Inject
	private CourseInfoService courseService;

	@AssistedInject
	public CourseEditorImpl(@Assisted CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(course, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public CourseEditorImpl(@Assisted CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(course, stagingUuid, null, false, importing);
	}

	@Override
	public void copyCustomFields(CourseBean bean)
	{
		super.copyCustomFields(bean);

		Boolean archived = bean.isArchived();
		if( archived != null )
		{
			entity.setDisabled(archived);
		}
		entity.setDisabled(editing);
		entity.setCode(bean.getCode());
		entity.setCitation(bean.getCitation());
		if( !Check.isEmpty(bean.getType()) )
		{
			entity.setCourseType(bean.getType().toLowerCase().charAt(0));
		}
		entity.setDepartmentName(bean.getDepartmentName());
		entity.setFrom(bean.getFrom());
		entity.setUntil(bean.getUntil());
		Integer students = bean.getStudents();
		if( students != null )
		{
			entity.setStudents(students);
		}
		entity.setCitation(bean.getCitation());
		String versionSelectionAsString = bean.getVersionSelection();
		if( !Check.isEmpty(versionSelectionAsString) )
		{
			VersionSelection versionSelection = VersionSelection.valueOf(versionSelectionAsString);
			entity.setVersionSelection(versionSelection);
		}
	}

	@Override
	protected CourseInfoService getEntityService()
	{
		return courseService;
	}

	@BindFactory
	public interface CourseEditorFactory
	{
		CourseEditorImpl createExistingEditor(@Assisted CourseInfo course,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		CourseEditorImpl createNewEditor(CourseInfo course, @Assisted("stagingUuid") @Nullable String stagingUuid,
			@Assisted("importing") boolean importing);
	}
}
