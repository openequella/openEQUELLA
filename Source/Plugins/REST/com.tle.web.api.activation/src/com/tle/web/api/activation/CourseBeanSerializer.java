package com.tle.web.api.activation;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.ValidationError;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.AbstractEntityService;
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
			//FIXME: i18n
			errors.add(new ValidationError("code", "code field must not be blank"));
		}
	}

	@Override
	protected void copyCustomFields(CourseInfo entity, CourseBean bean, Object data)
	{
		super.copyCustomFields(entity, bean, data);
		bean.setArchived(entity.isDisabled());
		bean.setCode(entity.getCode());
		bean.setType(CourseInfo.getTypeStringFromChar(entity.getCourseType()));
		bean.setCitation(entity.getCitation());
		bean.setDepartmentName(entity.getDepartmentName());
		bean.setFrom(entity.getFrom());
		bean.setUntil(entity.getUntil());
		bean.setStudents(entity.getStudents());
	}

	@Override
	protected AbstractEntityService<?, CourseInfo> getEntityService()
	{
		return courseService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.COURSE_INFO;
	}
}
