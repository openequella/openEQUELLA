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

import java.util.Collections;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.remoting.rest.service.UrlLinkService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ActivationSerializer
{
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private CourseBeanSerializer courseSerializer;
	@Inject
	private UrlLinkService urlLinkService;


	public ActivationBean serialize(ActivateRequest request)
	{
		ActivationBean activation = new ActivationBean();
		activation.setUuid(request.getUuid());
		activation.setCitation(request.getCitation());
		activation.setDescription(request.getDescription());
		activation.setLocationId(request.getLocationId());
		if( request.getCourse() != null )
		{
			CourseBean courseBean = courseSerializer.serialize(request.getCourse(), null, false);
			courseBean.set("links", Collections.singletonMap("self", urlLinkService.getMethodUriBuilder(CourseResource.class, "get")
				.build(courseBean.getUuid()).toString()));
			activation.setCourse(courseBean);
		}
		activation.setFrom(request.getFrom());
		activation.setUntil(request.getUntil());
		activation.setLocationName(request.getLocationName());
		switch( request.getStatus() )
		{
			case ActivateRequest.TYPE_ACTIVE:
				activation.setStatus("active");
				break;
			case ActivateRequest.TYPE_INACTIVE:
				activation.setStatus("expired");
				break;
			case ActivateRequest.TYPE_PENDING:
				activation.setStatus("pending");
				break;
			default:
				break;
		}
		activation.setType(request.getType());

		if( request.getItem() != null )
		{
			EquellaItemBean itemBean = new EquellaItemBean();
			Item item = request.getItem();
			ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(
				Collections.singletonList(item.getId()), Sets.newHashSet("basic", "attachment"), false);
			itemBean.setUuid(item.getUuid());
			itemBean.setVersion(item.getVersion());
			serializer.writeItemBeanResult(itemBean, item.getId());
			activation.setAttachment(request.getAttachment());

			itemLinkService.addLinks(itemBean);
			activation.setItem(itemBean);
		}
		return activation;
	}
}
