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

package com.tle.web.api.activation;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.web.api.activation.impl.CourseEditorImpl.CourseEditorFactory;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@Singleton
public class CourseBeanSerializer extends AbstractEquellaBaseEntitySerializer<CourseInfo, CourseBean, CourseEditor>
{
	@Inject
	private CourseInfoService courseService;
	@Inject
	private CourseEditorFactory editorFactory;

	@Override
	protected CourseBean createBean()
	{
		return new CourseBean();
	}

	@Override
	protected CourseInfo createEntity()
	{
		return new CourseInfo();
	}

	@Override
	protected CourseEditor createNewEditor(CourseInfo entity, @Nullable String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected CourseEditor createExistingEditor(CourseInfo entity, @Nullable String stagingUuid,
		@Nullable String lockId, boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected void validateCustom(CourseBean bean, boolean create, List<ValidationError> errors)
	{
		super.validateCustom(bean, create, errors);
		if( Check.isEmpty(bean.getCode()) )
		{
			errors.add(new ValidationError("code", CurrentLocale.get("course.edit.validation.codeempty")));
		}
	}

	@Override
	protected void copyCustomLightweightFields(CourseInfo entity, CourseBean bean, Object data)
	{
		bean.setCode(entity.getCode());
	}

	@Override
	protected void copyCustomFields(CourseInfo entity, CourseBean bean, Object data)
	{
		super.copyCustomFields(entity, bean, data);
		bean.setArchived(entity.isDisabled());
		bean.setType(CourseInfo.getTypeStringFromChar(entity.getCourseType()));
		bean.setCitation(entity.getCitation());
		bean.setDepartmentName(entity.getDepartmentName());
		bean.setFrom(entity.getFrom());
		bean.setUntil(entity.getUntil());
		bean.setStudents(entity.getStudents());
	}

	@Override
	protected CourseInfoService getEntityService()
	{
		return courseService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.COURSE_INFO;
	}
}
