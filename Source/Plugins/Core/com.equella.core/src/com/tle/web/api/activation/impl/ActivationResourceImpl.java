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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.BadRequestException;
import com.google.common.base.Strings;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.activation.ActivationBean;
import com.tle.web.api.activation.ActivationResource;
import com.tle.web.api.activation.ActivationSerializer;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

// TODO: anything that returns an error response here should be changed to throw the exception instead,
// the RestEasyExceptionMapper will turn that into a JSON object
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

	private final PluginResourceHelper resources = ResourcesService.getResourceHelper(ActivationResourceImpl.class);

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
					throw new BadRequestException(resources.getString("activation.badrequest.status"));
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
				throw new NotFoundException(resources.getString("activation.notfound.course.uuid", courseUuid));
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
		validateBean(bean);

		final Item item = itemService.getItemPack(new ItemId(bean.getItem().getUuid(), bean.getItem().getVersion()))
			.getItem();
		final CourseInfo course = findCourseFromBean(bean);
		final ActivateRequest activateRequest = composeActivateRequest(new ActivateRequest(), item, course, bean);

		activate(true, activateRequest, bean.getType());
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
		validateBean(bean);

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
			throw new NotFoundException(resources.getString("activation.notfound.activation", uuid));
		}
		if( !aclService.checkPrivilege("VIEW_ACTIVATION_ITEM", entity) )
		{
			throw new AccessDeniedException(
				resources.getString("activation.notfound.activation", "VIEW_ACTIVATION_ITEM"));
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
		ActivateRequest request = activationService.getRequest(uuid);
		if( request == null )
		{
			throw new NotFoundException(resources.getString("activation.notfound.activation", uuid));
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
	private void validateBean(ActivationBean bean)
	{
		final String type = bean.getType();
		if( Check.isEmpty(type) || !(ACTIVATION_TYPE_CLA.equals(type) || ACTIVATION_TYPE_CAL.equals(type)) )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.type"));
		}

		if( Check.isEmpty(bean.getAttachment()) )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.attachment"));
		}

		if( bean.getCourse() == null )
		{
			throw new BadRequestException(resources.getString("activation.badrequest.course"));
		}
		findCourseFromBean(bean);

		final String itemUuid = bean.getItem() != null ? bean.getItem().getUuid() : null;
		final Integer itemVersion = bean.getItem() != null ? bean.getItem().getVersion() : null;
		if( !Check.isEmpty(itemUuid) && itemVersion != null )
		{
			//Will throw NotFoundException as appropriate
			ItemId key = new ItemId(itemUuid, itemVersion);
			itemService.getAttachmentForUuid(key, bean.getAttachment());
			itemService.getItemPack(key).getItem();
		}
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
		activation.setItem(item);
		activation.setCourse(course);
		activation.setAttachment(bean.getAttachment());
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
			activation.setUser(bean.getUser().getId());
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
		CourseInfo course = bean.getCourse().getUuid() != null ? courseInfoService.getByUuid(bean.getCourse().getUuid())
			: (bean.getCourse().getCode() != null ? courseInfoService.getByCode(bean.getCourse().getCode()) : null);
		if( course == null )
		{
			throw new NotFoundException(resources.getString("activation.notfound.course.general"));
		}
		return course;
	}

	private URI getSelfLink(String requestUuid)
	{
		return urlLinkService.getMethodUriBuilder(ActivationResource.class, "get").build(requestUuid);
	}
}
