/**
 * 
 */
package com.tle.web.api.activation;

import java.util.Collections;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.guice.Bind;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.usermanagement.EquellaUserResource;
import com.tle.web.remoting.rest.service.UrlLinkService;

/**
 * Common serialization task for Activations and Courses
 * 
 * @author larry
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class CourseSerializer
{
	@Inject
	private UrlLinkService urlLinkService;

	public CourseBean serialize(CourseInfo courseInfo, boolean full)
	{
		CourseBean bean = new CourseBean();
		bean.setUuid(courseInfo.getUuid());
		if( bean.getName() != null && !Check.isEmpty(bean.getName().toString()) )
		{
			courseInfo.setName(LangUtils.createTextTempLangugageBundle(bean.getName().toString(),
				CurrentLocale.getLocale()));
		}
		bean.setCode(courseInfo.getCode());

		if( full )
		{
			if( bean.getDescription() != null && !Check.isEmpty(bean.getDescription().toString()) )
			{
				courseInfo.setDescription(LangUtils.createTextTempLangugageBundle(bean.getDescription().toString(),
					CurrentLocale.getLocale()));
			}
			bean.setType(CourseInfo.getTypeStringFromChar(courseInfo.getCourseType()));
			bean.setCitation(courseInfo.getCitation());
			bean.setDepartmentName(courseInfo.getDepartmentName());
			bean.setFrom(courseInfo.getFrom());
			bean.setUntil(courseInfo.getUntil());
			bean.setStudents(courseInfo.getStudents());
			if( !Check.isEmpty(courseInfo.getOwner()) )
			{
				UserBean userBean = new UserBean(courseInfo.getOwner());
				userBean.set(
					"links",
					Collections.singletonMap(
						"self",
						urlLinkService.getMethodUriBuilder(EquellaUserResource.class, "getUser").build(
							courseInfo.getOwner())));
				bean.setOwner(userBean);
			}
			bean.setArchived(courseInfo.isDisabled());
		}

		return bean;
	}
}
