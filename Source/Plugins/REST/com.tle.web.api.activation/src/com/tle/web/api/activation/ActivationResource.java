package com.tle.web.api.activation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.common.base.Strings;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.ItemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Dongsheng Cai
 */
@Bind
@Path("activation")
@Api(value = "/activation", description = "activation")
@Produces({"application/json"})
@SuppressWarnings("nls")
@Singleton
public class ActivationResource
{
	private static PluginResourceHelper resources = ResourcesService.getResourceHelper(ActivationResource.class);

	// Duplicated from constants in the cla & cal plugins
	public static final String ACTIVATION_TYPE_CAL = "cal";
	public static final String ACTIVATION_TYPE_CLA = "cla";

	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private ActivationService activationService;
	@Inject
	private ActivationSerializer activationSerializer;
	@Inject
	private ItemService itemService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private TLEAclManager aclService;

	/**
	 * Retrieve all activations, or if ...?course={courseUuid} param appended,
	 * retrieve just those activations associated with the specified course.
	 * 
	 * @param courseUuid
	 * @return
	 */
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve activations")
	public Response searchForActivations(
			// @formatter:off
			@ApiParam(value = "Course uuid", required = false) @QueryParam("course") String courseUuid,
			@ApiParam(value = "status", required = false, allowableValues = "active,pending,expired,any") @QueryParam("status") String status
			// @formatter:on
	)
	{
		int state = -1;
		if( !Check.isEmpty(status) )
		{
			switch( status.toLowerCase() )
			{
				case "active":
					state = ActivateRequest.TYPE_ACTIVE;
					break;
				case "pending":
					state = ActivateRequest.TYPE_PENDING;
					break;
				case "expired":
					state = ActivateRequest.TYPE_INACTIVE;
					break;
				case "any":
					// same as no status specified, so leave state as negative
					break;
				default:
					throw new BadRequestException(resources.getString("activation.badrequest.status"));
			}
		}

		List<ActivateRequest> results;
		if( state < 0 )
		{
			results = activationService.listAll();
		}
		else
		{
			results = activationService.getAllByStatus(state);
		}

		if( !Strings.isNullOrEmpty(courseUuid) )
		{
			CourseInfo course = courseInfoService.getByUuid(courseUuid);
			if( course == null )
			{
				throw new BadRequestException(resources.getString("activation.notfound.course.uuid", courseUuid));
			}
			List<ActivateRequest> byCourse = activationService.getByCourse(course);
			results.retainAll(byCourse);
		}

		List<ActivationBean> resultsOfBeans = new ArrayList<ActivationBean>(results.size());
		for( ActivateRequest request : results )
		{
			ActivationBean bean = serialize(request);
			resultsOfBeans.add(bean);
		}

		SearchBean<ActivationBean> retBean = new SearchBean<ActivationBean>();
		retBean.setStart(0);
		retBean.setLength(resultsOfBeans.size());
		retBean.setAvailable(results.size());
		retBean.setResults(resultsOfBeans);

		return Response.ok(retBean).build();
	}

	/**
	 * @param bean
	 * @return
	 */
	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation("Create an activation")
	public Response createActivation(@ApiParam("Activation request in JSON format") ActivationBean bean)
	{
		// validate the settings
		Response invalidity = validateBean(bean);
		if( invalidity != null )
		{
			return invalidity;
		}
		final Item item = itemService.getItemPack(new ItemId(bean.getItem().getUuid(), bean.getItem().getVersion()))
			.getItem();
		final CourseInfo course = findCourseFromBean(bean);
		final ActivateRequest activateRequest = composeActivateRequest(new ActivateRequest(), item, course, bean);
		final ActivationBean outcome = activate(true, activateRequest, bean.getType());
		return Response.status(Status.CREATED).location(getSelfLink(activateRequest.getUuid())).build();
	}

	@PUT
	@Path("/{requestuuid}")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation("Update/edit an activation")
	public Response updateActivation(
			// @formatter:off
			@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid,
			@ApiParam(value = "to disable or not", allowableValues = "true,false", defaultValue = "false", required = false)
			@QueryParam("disable")
				boolean disable,
			@ApiParam("Activation request in JSON format") ActivationBean bean
			// @formatter:on
	)
	{
		// parameter uuid valid?
		ActivateRequest request = activationService.getRequest(requestUuid);
		if( request == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		// body of request coherent?
		Response invalidity = validateBean(bean);
		if( invalidity != null )
		{
			return invalidity;
		}

		// push the button
		int oldStatus = request.getStatus();

		Item item = itemService.getItemPack(new ItemId(bean.getItem().getUuid(), bean.getItem().getVersion()))
			.getItem();
		CourseInfo course = findCourseFromBean(bean);

		ActivateRequest reconstituted = composeActivateRequest(request, item, course, bean);
		activationService.updateActivation(reconstituted);

		if( disable && (oldStatus == ActivateRequest.TYPE_ACTIVE || oldStatus == ActivateRequest.TYPE_PENDING) )
		{
			activate(false, request, bean.getType());
		}
		else if( !disable && oldStatus == ActivateRequest.TYPE_INACTIVE )
		{
			activate(true, request, bean.getType());
		}
		else
		{
			serialize(activationService.getRequest(reconstituted.getUuid()));
		}

		return Response.ok().build();
	}

	@GET
	@Path("/{requestuuid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation("Retrieve an activation request by uuid")
	public Response getActivation(
		@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid)
	{
		ActivateRequest request = activationService.getRequest(requestUuid);
		if( request == null )
		{
			throw new NotFoundException(resources.getString("activation.notfound.activation", requestUuid));
		}
		ActivationBean activationBean = serialize(request);
		return Response.ok(activationBean).build();
	}

	/**
	 * Get activations
	 * 
	 * @param uuid
	 * @param version
	 * @return
	 */
	@GET
	@Path("/item/{uuid}/{version}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get activations for a particular item")
	public Response getActivatedItems(
			// @formatter:off
			@ApiParam(value = "Item uuid", required = true)
				@PathParam("uuid")
				String uuid,
			@ApiParam(value = "Item version", required = true)
				@PathParam("version")
				int version
			// @formatter:on
	)
	{
		Item item = itemService.get(new ItemId(uuid, version));

		List<ActivateRequest> calRequests = activationService.getAllRequests(ACTIVATION_TYPE_CAL, item);
		List<ActivateRequest> claRequests = activationService.getAllRequests(ACTIVATION_TYPE_CLA, item);
		List<ActivationBean> requestBeans = new ArrayList<ActivationBean>();
		for( ActivateRequest request : calRequests )
		{
			ActivationBean activation = serialize(request);
			requestBeans.add(activation);
		}
		for( ActivateRequest request : claRequests )
		{
			ActivationBean activation = serialize(request);
			requestBeans.add(activation);
		}

		return Response.ok(requestBeans).build();
	}

	@DELETE
	@Path("/{requestuuid}")
	@ApiOperation("Delete an activation request")
	public Response deleteActivation(
		@ApiParam(value = "Activation request uuid") @PathParam("requestuuid") String requestUuid)
	{
		ActivateRequest request = activationService.getRequest(requestUuid);
		if( request == null )
		{
			throw new NotFoundException(resources.getString("activation.notfound.activation", requestUuid));
		}
		activationService.delete(request.getType(), request);

		return Response.status(Status.NO_CONTENT).build();
	}

	private ActivationBean serialize(ActivateRequest request)
	{
		ActivationBean activationBean = activationSerializer.serialize(request);

		Map<String, String> links = Collections.singletonMap("self", getSelfLink(activationBean.getUuid()).toString());
		activationBean.set("links", links);

		return activationBean;
	}

	/**
	 * @param bean
	 * @return null if validation successful, else a server error response
	 */
	private Response validateBean(ActivationBean bean)
	{
		CourseInfo course = null;
		String type = bean.getType();
		if( Check.isEmpty(type) || !(ACTIVATION_TYPE_CLA.equals(type) || ACTIVATION_TYPE_CAL.equals(type)) )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.type"));
		}

		if( Check.isEmpty(bean.getAttachment()) )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.attachment"));
		}
		String itemUuid = bean.getItem() != null ? bean.getItem().getUuid() : null;
		Integer itemVersion = bean.getItem() != null ? bean.getItem().getVersion() : null;
		if( !Check.isEmpty(itemUuid) && itemVersion != null )
		{
			ItemId key = new ItemId(itemUuid, itemVersion);
			itemService.getAttachmentForUuid(key, bean.getAttachment());
			itemService.getItemPack(key).getItem();
		}
		if( bean.getCourse() == null )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.course"));
		}
		course = findCourseFromBean(bean);
		return null;
	}

	/**
	 * perform activation
	 * 
	 * @param bean
	 * @return re-serialized ActivationBean
	 */
	private ActivationBean activate(boolean activate, ActivateRequest activateRequest, String type)
	{
		ActivationBean outcome = null;
		if( activate )
		{
			List<ActivateRequest> requests;
			try
			{
				requests = activationService.activate(type, activateRequest.getItem(),
					Collections.singletonList(activateRequest), false);
			}
			catch( CopyrightViolationException we )
			{
				if( we.isCALBookPercentageException() )
				{
					if( aclService.filterNonGrantedPrivileges(ActivationConstants.COPYRIGHT_OVERRIDE).isEmpty() )
					{
						throw new AccessDeniedException(resources.getString("activation.access.denied.percent"));
					}
					if( Check.isEmpty(activateRequest.getOverrideReason()) )
					{
						throw new AccessDeniedException(resources.getString("activation.access.denied.message"));
					}
					requests = activationService.activate(type, activateRequest.getItem(),
						Collections.singletonList(activateRequest), true);
				}
				else
				{
					throw we; // some other kind of CopyrightViolation
				}

			}
			outcome = serialize(requests.get(0));
		}
		else
		{
			activationService.deactivate(type, activateRequest);
			outcome = serialize(activationService.getRequest(activateRequest.getUuid()));
		}
		return outcome;
	}

	private ActivateRequest composeActivateRequest(ActivateRequest activation, Item item, CourseInfo course,
		ActivationBean bean)
	{
		String attachmentUuid = bean.getAttachment();

		activation.setItem(item);
		activation.setCourse(course);
		activation.setAttachment(attachmentUuid);
		// Course date(s) apply unless activation dates supplied
		Date from = course.getFrom();
		Date until = course.getUntil();
		if( bean.getFrom() != null )
		{
			from = bean.getFrom();
		}
		if( bean.getUntil() != null )
		{
			until = bean.getUntil();
		}
		if( from != null && until != null && from.after(until) )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.dates"));
		}

		activation.setFrom(from);
		activation.setUntil(until);
		activation.setDescription(bean.getDescription());
		activation.setLocationId(bean.getLocationId());
		activation.setLocationName(bean.getLocationName());
		activation.setUuid(bean.getUuid());
		activation.setOverrideReason(bean.getOverrideMessage());
		if( bean.getUser() != null )
		{
			activation.setUser(bean.getUser().getUniqueID());
		}
		activation.setCitation(!Check.isEmpty(bean.getCitation()) ? bean.getCitation() : course.getCitation());

		return activation;
	}

	/**
	 * look for course uuid, else course code
	 * 
	 * @param bean
	 * @return
	 */
	private CourseInfo findCourseFromBean(ActivationBean bean)
	{
		CourseInfo course = bean.getCourse().getUuid() != null ? courseInfoService
			.getByUuid(bean.getCourse().getUuid()) : (bean.getCourse().getCode() != null ? courseInfoService
			.getByCode(bean.getCourse().getCode()) : null);
		if( course == null )
		{
			throw new NotFoundException(resources.getString("activation.notfound.course.general"));
		}
		return course;
	}

	private URI getSelfLink(String courseUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getActivation").build(courseUuid);
	}
}
