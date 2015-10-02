package com.tle.web.api.activation.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.InUseException;
import com.google.common.base.Strings;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.item.ItemService;
import com.tle.web.api.activation.ActivationBean;
import com.tle.web.api.activation.ActivationResource;
import com.tle.web.api.activation.ActivationSerializer;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

/**
 * @author Dongsheng Cai
 */
@SuppressWarnings("nls")
@Bind(ActivationResource.class)
@Singleton
public class ActivationResourceImpl implements ActivationResource
{
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
	@Override
	public SearchBean<ActivationBean> search(String courseUuid, String status)
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
					throw new WebApplicationException("Status not in [active,pending,expired,any]", Status.BAD_REQUEST);
			}
		}

		final List<ActivateRequest> results;
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
			final CourseInfo course = courseInfoService.getByUuid(courseUuid);
			if( course == null )
			{
				throw new WebApplicationException("No course found with UUID " + courseUuid, Status.NOT_FOUND);
			}
			final List<ActivateRequest> byCourse = activationService.getByCourse(course);
			results.retainAll(byCourse);
		}

		final List<ActivationBean> resultsOfBeans = new ArrayList<ActivationBean>(results.size());
		for( ActivateRequest request : results )
		{
			resultsOfBeans.add(serialize(request));
		}

		final SearchBean<ActivationBean> retBean = new SearchBean<ActivationBean>();
		retBean.setStart(0);
		retBean.setLength(resultsOfBeans.size());
		retBean.setAvailable(results.size());
		retBean.setResults(resultsOfBeans);
		return retBean;
	}

	/**
	 * @param bean
	 * @return
	 */
	@Override
	public Response create(ActivationBean bean)
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

	@Override
	public Response edit(String requestUuid, boolean disable, ActivationBean bean)
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

	@Override
	public ActivationBean get(String uuid)
	{
		final ActivateRequest entity = activationService.getRequest(uuid);
		if( entity == null )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if( !aclService.checkPrivilege("VIEW_ACTIVATION_ITEM", entity) )
		{
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return serialize(entity);
	}

	/**
	 * Get activations
	 * 
	 * @param uuid
	 * @param version
	 * @return
	 */
	@Override
	public Response getActivatedItems(String uuid, int version)
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

	@Override
	public Response delete(String uuid)
	{
		ActivateRequest entity = activationService.getRequest(uuid);
		if( entity == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		try
		{
			activationService.delete(entity.getType(), entity);
			return Response.noContent().build();
		}
		catch( InUseException inUse )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
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
			return Response.status(Status.BAD_REQUEST).entity("type must be either 'cla' or 'cal'").build();
		}

		if( Check.isEmpty(bean.getAttachment()) )
		{
			return Response.status(Status.BAD_REQUEST).entity("must have attachment uuid").build();
		}
		else
		{
			String itemUuid = bean.getItem() != null ? bean.getItem().getUuid() : null;
			Integer itemVersion = bean.getItem() != null ? bean.getItem().getVersion() : null;
			if( !Check.isEmpty(itemUuid) && itemVersion != null )
			{
				try
				{
					ItemId key = new ItemId(itemUuid, itemVersion);
					itemService.getAttachmentForUuid(key, bean.getAttachment()); // throws
																					// not
																					// found
																					// exception
					itemService.getItemPack(key).getItem(); // likewise
				}
				catch( Exception e )
				{
					return Response.status(Status.BAD_REQUEST).entity(e.getLocalizedMessage()).build();
				}
			}
			if( bean.getCourse() == null )
			{
				return Response.status(Status.BAD_REQUEST).entity("course not specified").build();
			}
			else
			{
				course = findCourseFromBean(bean);
				if( course == null )
				{
					return Response.status(Status.NOT_FOUND).entity("course not found").build();
				}
			}
		}
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
		final ActivationBean outcome;
		if( activate )
		{
			List<ActivateRequest> requests = activationService.activate(type, activateRequest.getItem(),
				Collections.singletonList(activateRequest), true);
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
			throw new RuntimeException("Dates provided result in 'from' after 'until'");
		}

		activation.setFrom(from);
		activation.setUntil(until);
		activation.setDescription(bean.getDescription());
		activation.setLocationId(bean.getLocationId());
		activation.setLocationName(bean.getLocationName());
		activation.setUuid(bean.getUuid());
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
		return course;
	}

	private URI getSelfLink(String courseUuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getActivation").build(courseUuid);
	}
}
