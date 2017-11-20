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

package com.tle.web.connectors.api.search;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.connectors.api.ConnectorBeanSerializer;
import com.tle.web.connectors.api.bean.ConnectorBean;
import com.tle.web.connectors.api.bean.ConnectorContentBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author larry
 */
@Bind
@Path("connector")
@Api(value = "/connector", description = "connector")
@Produces({"application/json"})
@Singleton
@SuppressWarnings("nls")
public class ConnectorResource
{
	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService connectorRepositoryService;
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private ConnectorBeanSerializer serializer;

	/**
	 * Provided the user has at least the basic CONNECTOR privilege, get a list
	 * of all connectors, and from that list exclude any of those Connectors for
	 * which the user lacks the PRIV_VIEWCONTENT_VIA_CONNECTOR privilege.
	 * 
	 * @param uuid Item uuid, not relevant to the query per se, but used in
	 *            composing result set
	 * @param version Item version, not relevant to the query per se, but used
	 *            in composing result set
	 * @return
	 */
	@GET
	@Path("/{uuid}/{version}")
	@ApiOperation(value = "List of connectors and usage links")
	public List<ConnectorBean> getConnectorQueriesForItem(@ApiParam("Item UUID") @PathParam("uuid") String uuid,
		@ApiParam("Item version") @PathParam("version") int version)
	{
		List<ConnectorBean> connectorBeans = Lists.newArrayList();
		Iterable<BaseEntityLabel> connectorDescriptors = connectorService.listForViewing();
		UriBuilder uriBuilder = urlLinkService.getMethodUriBuilder(getClass(), "getUsagesOfItemForConnector");
		for( BaseEntityLabel bent : connectorDescriptors )
		{
			Connector connector = connectorService.get(bent.getId());
			ConnectorBean bean = serializer.serialize(connector, null, true);
			Map<String, URI> links = Maps.newHashMap();
			URI uri = uriBuilder.build(uuid, version, bent.getUuid());
			links.put("usage", uri);
			bean.set("links", links);
			connectorBeans.add(bean);
		}
		return connectorBeans;
	}

	/**
	 * For a given item (identified by its UUID), version and Connector
	 * (identified by its UUID), retrieve the list of usages, ie the courses and
	 * related details
	 * 
	 * @param uuid
	 * @param version
	 * @param connectoruuid
	 * @return
	 */
	@GET
	@Path("/{uuid}/{version}/use/{connectoruuid}")
	@ApiOperation(value = "List usages of item within connector")
	public SearchBean<ConnectorContentBean> getUsagesOfItemForConnector(
		// @ApiParam(value="The first record of the search results to return",
		// required = false, defaultValue="0") @QueryParam("start")
		// int start,
		// @ApiParam(value="The number of results to return", required = false,
		// defaultValue = "10", allowableValues = "range[1,100]")
		// @QueryParam("length")
		// int length,
		@ApiParam("Item UUID") @PathParam("uuid") String uuid,
		@ApiParam("Item version") @PathParam("version") int version,
		@ApiParam("Connector UUID") @PathParam("connectoruuid") String connectoruuid)
	{
		List<ConnectorContentBean> allUsageBeans = new ArrayList<ConnectorContentBean>();
		List<ConnectorContent> allUsages = null;
		Connector connector = connectorService.get(connectorService.identifyByUuid(connectoruuid));
		String username = CurrentUser.getUsername();
		try
		{
			allUsages = connectorRepositoryService.findUsages(connector, username, uuid, version, false, false);
		}
		catch( LmsUserNotFoundException lunfe )
		{
			throw new RuntimeException(
				"Failed to query course for " + connector.getLmsType() + " by " + username, lunfe); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if( !Check.isEmpty(allUsages) )
		{
			for( ConnectorContent usageContent : allUsages )
			{
				ConnectorContentBean bean = new ConnectorContentBean();
				bean.setCourse(usageContent.getCourse());
				bean.setCourseCode(usageContent.getCourseCode());
				bean.setCourseId(usageContent.getCourseId());
				bean.setId(usageContent.getId());
				bean.setDateAdded(usageContent.getDateAdded());
				bean.setFolder(usageContent.getFolder());
				bean.setExternalTitle(usageContent.getExternalTitle());
				bean.setExternalDescription(usageContent.getExternalDescription());
				allUsageBeans.add(bean);
			}
		}
		SearchBean<ConnectorContentBean> results = new SearchBean<ConnectorContentBean>();
		results.setResults(allUsageBeans);
		results.setAvailable(allUsageBeans.size());
		results.setLength(allUsageBeans.size());
		return results;
	}
}
