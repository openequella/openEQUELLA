package com.tle.web.api.activation;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Lists;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author larry
 */
@Bind
@Path("course")
@Api(value = "/course", description = "course")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("nls")
@Singleton
public class CourseResource
{
	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private CourseSerializer courseSerializer;
	@Inject
	private TLEAclManager aclService;

	/**
	 * SOAP had enumerateCourseCodes
	 * 
	 * @return
	 */
	@GET
	@ApiOperation(value = "Get a list of EQUELLA courses")
	public Response getCourses(
		@ApiParam(value = "Course code, unique so only returns a single", required = false) @QueryParam("code") String code)
	{
		final List<CourseBean> resultsOfBeans = Lists.newArrayList();

		Collection<CourseInfo> rawResults = Collections.emptyList();
		if( Check.isEmpty(code) )
		{
			rawResults = courseInfoService.enumerateListable();
		}
		else
		{
			// presence of 'code' parameter means we'll have a list of at most 1
			CourseInfo oneAtMost = courseInfoService.getByCode(code);
			if( oneAtMost != null )
			{
				rawResults = aclService.filterNonGrantedObjects(Collections.singleton("LIST_COURSE_INFO"),
					Collections.singletonList(oneAtMost));
			}
		}
		for( CourseInfo courseInfo : rawResults )
		{
			CourseBean bean = serialize(courseInfo, false);
			resultsOfBeans.add(bean);
		}

		SearchBean<CourseBean> retBean = new SearchBean<CourseBean>();

		retBean.setStart(0);
		retBean.setLength(resultsOfBeans.size());
		retBean.setAvailable(rawResults.size());
		retBean.setResults(resultsOfBeans);

		return Response.ok(retBean).build();
	}

	/**
	 * SOAP getCourse
	 * 
	 * @param uuid
	 * @return
	 */
	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Get details on a single EQUELLA course")
	public Response getCourse(@ApiParam("course uuid") @PathParam("uuid") String uuid)
	{
		final CourseInfo courseInfo = courseInfoService.getByUuid(uuid);
		if( courseInfo == null )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if( !courseInfoService.canView(courseInfo) )
		{
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		CourseBean bean = serialize(courseInfo, true);
		return Response.ok(bean).build();
	}

	/**
	 * SOAP addCourse
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create a new EQUELLA course")
	public Response createCourse(@ApiParam("course in json representation") CourseBean originalBean)
	{
		CourseInfo info = null;
		try
		{
			info = courseInfoFromBean(originalBean);
		}
		catch( Exception iae )
		{
			return Response.status(Status.BAD_REQUEST).entity(iae.getMessage()).build();
		}
		EntityPack<CourseInfo> entityPack = new EntityPack<CourseInfo>(info, null);
		String resultantUuid = null;
		try
		{
			resultantUuid = courseInfoService.add(entityPack, false).getUuid();
		}
		catch( Exception e )
		{
			return Response.serverError().entity(e.getLocalizedMessage()).build();
		}
		return Response.status(Status.CREATED).location(getSelfLink(resultantUuid)).build();
	}

	/**
	 * From the SOAP call editCourse. Also covers archive / unArchive (by
	 * setting the 'archived' property)
	 * 
	 * @param uuid
	 * @param bean
	 * @return
	 */
	@PUT
	@Path("/{uuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "edit an EQUELLA course")
	public Response editCourse(
			// @formatter:off
			@ApiParam("the uuid of the course being edited")
				@PathParam("uuid")
				String uuid,
			@ApiParam("course in json representation")
				CourseBean bean
			// @formatter:on
	)
	{
		// first ensure the caller has sent a valid id
		CourseInfo courseInfo = courseInfoService.getByUuid(uuid);

		if( courseInfo == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}

		String outcomeCode = bean.getCode();
		if( Check.isEmpty(outcomeCode) )
		{
			return Response.status(Status.BAD_REQUEST).entity("code cannot be null").build();
		}
		CourseInfo courseInfoSameCode = courseInfoService.getByCode(outcomeCode);
		// enforce uniqueness of the code - lest the 'edit' be a backdoor means
		// of editing/overwriting a course info other than the one identified
		// reinitialise the CourseInfo with the bean's values. So insist that if
		// the code already exists, it cannot be in a courseInfo with a
		// different
		// uuid than the courseInfo we're editing
		if( courseInfoSameCode != null && !uuid.equals(courseInfoSameCode.getUuid()) )
		{
			return Response.status(Status.BAD_REQUEST).entity("code: '" + outcomeCode + "' already in use").build();
		}

		try
		{
			courseInfo = courseInfoFromBean(bean);
		}
		catch( Exception iae )
		{
			return Response.status(Status.BAD_REQUEST).entity(iae.getMessage()).build();
		}

		courseInfoService.edit(courseInfo);
		return Response.ok(bean).build();
	}

	/**
	 * SOAP call delete
	 * 
	 * @param uuid
	 * @return
	 */
	@DELETE
	@Path("/{uuid}")
	@ApiOperation(value = "Delete an EQUELLA Course")
	public Response deleteCourse(@ApiParam("uuid of course to delete") @PathParam("uuid") String uuid)
	{
		final CourseInfo courseInfo = courseInfoService.getByUuid(uuid);
		if( courseInfo == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		// throws an InUseException if it comes to that
		courseInfoService.delete(courseInfo, true);
		return Response.ok().build();
	}

	private CourseBean serialize(CourseInfo courseInfo, boolean full)
	{
		CourseBean bean = courseSerializer.serialize(courseInfo, full);
		Map<String, String> links = Collections.singletonMap("self", getSelfLink(bean.getUuid()).toString());
		bean.set("links", links);
		return bean;
	}

	private CourseInfo courseInfoFromBean(CourseBean bean)
	{
		String uuid = bean.getUuid();
		if( Check.isEmpty(uuid) )
		{
			uuid = UUID.randomUUID().toString();
			bean.setUuid(uuid);
		}

		CourseInfo courseInfo = new CourseInfo();

		courseInfo.setUuid(uuid);
		courseInfo.setCode(bean.getCode());
		// NB - If bench-testing, Chances are that the Firefox REST tool will
		// get you a
		// Locale object full of null/empty values - use the Google Postman or
		// other.
		Locale currentLocale = CurrentLocale.getLocale();

		courseInfo.setName(LangUtils.createTextTempLangugageBundle(bean.getName().toString(), currentLocale));
		if( bean.getDescription() != null && !Check.isEmpty(bean.getDescription().toString()) )
		{
			courseInfo.setDescription(LangUtils.createTextTempLangugageBundle(bean.getDescription().toString(),
				currentLocale));
		}
		courseInfo.setCitation(bean.getCitation());
		if( !Check.isEmpty(bean.getType()) )
		{
			courseInfo.setCourseType(bean.getType().toLowerCase().charAt(0));
		}
		courseInfo.setDepartmentName(bean.getDepartmentName());
		courseInfo.setFrom(bean.getFrom());
		courseInfo.setUntil(bean.getUntil());
		courseInfo.setStudents(bean.getStudents());
		courseInfo.setCitation(bean.getCitation());
		String versionSelectionAsString = bean.getVersionSelection();
		if( !Check.isEmpty(versionSelectionAsString) )
		{
			VersionSelection versionSelection = VersionSelection.valueOf(versionSelectionAsString);
			courseInfo.setVersionSelection(versionSelection);
		}
		// else null considered equivalent to
		// VersionSelection.INSTITUTION_DEFAULT

		if( bean.getOwner() != null )
		{
			courseInfo.setOwner(bean.getOwner().getId());
		}
		return courseInfo;
	}

	private URI getSelfLink(String courseUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getCourse").build(courseUuid);
	}
}
