package com.tle.web.api.activation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriInfo;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.activation.CourseBean;
import com.tle.web.api.activation.CourseBeanSerializer;
import com.tle.web.api.activation.CourseResource;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind(CourseResource.class)
@Singleton
public class CourseResourceImpl extends AbstractBaseEntityResource<CourseInfo, BaseEntitySecurityBean, CourseBean>
	implements
		CourseResource
{
	@Inject
	private CourseInfoService courseService;
	@Inject
	private CourseBeanSerializer courseSerializer;
	@Inject
	private TLEAclManager aclService;

	/**
	 * Provide the full course data in the results
	 */
	@Override
	protected CourseBean serialize(CourseInfo entity, Object data, boolean heavy)
	{
		return super.serialize(entity, data, true);
	}

	@Override
	public SearchBean<CourseBean> list(UriInfo uriInfo, String code)
	{
		if( Check.isEmpty(code) )
		{
			return super.list(uriInfo);
		}

		final List<CourseBean> courseByCode = new ArrayList<>();
		// presence of 'code' parameter means we'll have a list of at most 1
		final CourseInfo oneAtMost = courseService.getByCode(code);
		if( oneAtMost != null )
		{
			Collection<CourseInfo> matchedCourses = aclService.filterNonGrantedObjects(
				Collections.singleton("LIST_COURSE_INFO"), Collections.singletonList(oneAtMost));
			for( CourseInfo matched : matchedCourses )
			{
				courseByCode.add(serialize(matched, null, true));
			}
		}

		final SearchBean<CourseBean> retBean = new SearchBean<CourseBean>();
		retBean.setStart(0);
		retBean.setLength(courseByCode.size());
		retBean.setAvailable(courseByCode.size());
		retBean.setResults(courseByCode);
		return retBean;
	}

	@Override
	protected void validate(String uuid, CourseBean bean, boolean isNew) throws InvalidDataException
	{
		super.validate(uuid, bean, isNew);
		final String courseCode = bean.getCode();
		if( courseCode != null )
		{
			final CourseInfo courseSameCode = courseService.getByCode(courseCode);
			if( courseSameCode != null && !uuid.equals(courseSameCode.getUuid()) )
			{
				throw new InvalidDataException(new ValidationError("code", CurrentLocale.get(
					"com.tle.web.api.activation.course.edit.validation.codeinuse", courseCode)));
			}
		}
	}

	@Override
	protected int getSecurityPriority()
	{
		return SecurityConstants.PRIORITY_COURSE_INFO;
	}

	@Override
	protected AbstractEntityService<?, CourseInfo> getEntityService()
	{
		return courseService;
	}

	@Override
	protected BaseEntitySerializer<CourseInfo, CourseBean> getSerializer()
	{
		return courseSerializer;
	}

	@Override
	protected Class<CourseResource> getResourceClass()
	{
		return CourseResource.class;
	}

	@Override
	protected Node[] getAllNodes()
	{
		return new Node[]{Node.ALL_COURSE_INFO};
	}

	@Override
	protected BaseEntitySecurityBean createAllSecurityBean()
	{
		return new BaseEntitySecurityBean();
	}
}
