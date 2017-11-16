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

package com.tle.web.api.taxonomy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.WebException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.beans.taxonomy.TermBean;
import com.tle.common.Pair;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.entity.service.EntityLockingService;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.TermService;
import com.tle.web.api.interfaces.beans.SearchBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@NonNullByDefault
@Bind
@Path("taxonomy")
@Api(value = "/taxonomy", description = "taxonomy")
@Produces({"application/json"})
@SuppressWarnings("nls")
@Singleton
public class TaxonomyResource
{
	private enum PrivCheck
	{
		VIEW, EDIT, DELETE
	}

	@Inject
	private EntityLockingService lockingService;
	@Inject
	private TaxonomyService taxonomyService;
	@Inject
	private TermService termService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private TaxonomyBeanSerializer taxonomySerializer;

	/**
	 * Returns all taxonomies.
	 * 
	 * @return Response encapsulating SearchBean of List of TaxonomyBean
	 */
	@GET
	@Path("/")
	@Produces("application/json")
	@ApiOperation(value = "List all taxonomies")
	public Response listAllTaxonomies()
	{
		SearchBean<TaxonomyBean> results = new SearchBean<TaxonomyBean>();
		List<TaxonomyBean> beans = Lists.newArrayList();
		List<Taxonomy> taxonomies = taxonomyService.enumerateListable();
		for( Taxonomy taxonomy : taxonomies )
		{
			TaxonomyBean bean = taxonomySerializer.serialize(taxonomy, null, false);
			beans.add(bean);
		}
		results.setResults(beans);
		results.setAvailable(beans.size());
		results.setLength(beans.size());
		return Response.ok(results).build();
	}

	/**
	 * Returns terms
	 * 
	 * @return Response encapsulating TermBeans
	 */
	@GET
	@Path("/{uuid}")
	@Produces("application/json")
	@ApiOperation(value = "Get taxonomy")
	public Response getTaxonomy(@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String uuid)
	{
		final Taxonomy taxonomy = ensureTaxonomy(uuid, PrivCheck.VIEW);
		return Response.ok(taxonomySerializer.serialize(taxonomy, null, true)).build();
	}

	@DELETE
	@Path("/{uuid}")
	@ApiOperation(value = "Delete taxonomy")
	public Response delete(@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String uuid)
	{
		final Taxonomy taxonomy = ensureTaxonomy(uuid, PrivCheck.DELETE);
		taxonomyService.delete(taxonomy, false);
		return Response.noContent().build();
	}

	@GET
	@Path("/{uuid}/lock")
	@ApiOperation(value = "Get taxonomy lock status")
	public Response getTaxonomyLockStatus(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid)
	{
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);
		if( lockingService.isEntityLocked(taxonomy, CurrentUser.getUserID(), null) )
		{
			return Response.status(Status.NOT_FOUND).build();
		}

		return Response.ok().build();

	}

	@POST
	@Path("/{uuid}/lock")
	@ApiOperation(value = "Lock taxonomy")
	public Response lockTaxonomy(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid)
	{
		try
		{
			final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
			lockingService.lockEntity(taxonomy);
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new WebApplicationException(
					"Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter value of true.",
					Status.UNAUTHORIZED);
			}
			else
			{
				throw new WebApplicationException("Taxonomy is locked by another user: " + ex.getUserID(),
					Status.UNAUTHORIZED);
			}
		}
		return Response.ok().build();
	}

	@DELETE
	@Path("/{uuid}/lock")
	@Produces("application/json")
	@ApiOperation(value = "unlock taxonomy")
	public Response unlockTaxonomy(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "force unlock", required = false) @QueryParam("force") boolean force)
	{
		try
		{
			final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
			lockingService.unlockEntity(taxonomy, force);
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new WebApplicationException(
					"Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter value of true.",
					Status.UNAUTHORIZED);
			}
			else
			{
				throw new WebApplicationException(
					"You do not own the lock on this taxonomy.  It is held by user ID " + ex.getUserID(),
					Status.UNAUTHORIZED);
			}
		}
		return Response.ok().build();

	}

	/**
	 * Search terms
	 * 
	 * @param uuid
	 * @param query
	 * @param restriction
	 * @param limit
	 * @param searchfullterm
	 * @return Response
	 */
	@GET
	@Path("/{uuid}/search")
	@Produces("application/json")
	@ApiOperation(value = "Search terms")
	public Response searchTaxonomyTerms(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String uuid,
		@ApiParam(value = "Term query", required = true) @QueryParam("q") String query,
		@ApiParam(value = "Selection restriction", required = false, defaultValue = "UNRESTRICTED", allowableValues = "TOP_LEVEL_ONLY,LEAF_ONLY,UNRESTRICTED") @QueryParam("restriction") String restriction,
		@ApiParam(value = "Limit number of results", required = false, defaultValue = "20") @QueryParam("limit") int limit,
		@ApiParam(value = "search fullterm", required = false, defaultValue = "false") @QueryParam("searchfullterm") boolean searchfullterm)
	{
		final Taxonomy taxonomy = ensureTaxonomy(uuid, PrivCheck.VIEW);

		SelectionRestriction restrict = (restriction == null ? SelectionRestriction.UNRESTRICTED
			: SelectionRestriction.valueOf(restriction.toUpperCase()));
		int max = (limit <= 0 ? 20 : limit);

		Pair<Long, List<TermResult>> searchTerms = taxonomyService.searchTerms(uuid, query, restrict, max,
			searchfullterm);

		List<TermBean> beans = Lists.newArrayList();
		for( TermResult term : searchTerms.getSecond() )
		{
			TermBean bean = beanFromTaxonomyTerm(term, uuid);
			beans.add(bean);
		}

		final SearchBean<TermBean> result = new SearchBean<TermBean>();
		result.setAvailable((int) (long) searchTerms.getFirst());
		result.setLength(beans.size());
		result.setResults(beans);
		return Response.ok(result).build();
	}

	/**
	 * Returns terms
	 * 
	 * @return Response encapsulating TermBeans
	 */
	@GET
	@Path("/{uuid}/term")
	@Produces("application/json")
	@ApiOperation(value = "Get taxonomy terms")
	public Response getTaxonomyTerms(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "Term path", required = false) @QueryParam("path") String path)
	{
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);

		Collection<TermResult> terms = taxonomyService.getChildTerms(taxonomyUuid, path);
		final List<TermBean> beans = Lists.newArrayList();
		for( TermResult term : terms )
		{
			TermBean bean = beanFromTaxonomyTerm(term, taxonomyUuid);
			beans.add(bean);
		}
		return Response.ok(beans).build();
	}

	/**
	 * Insert new term
	 * 
	 * @param uuid
	 * @param termBean
	 * @return
	 */
	@POST
	@Path("/{uuid}/term")
	@Produces("application/json")
	@ApiOperation(value = "Create taxonomy term")
	public Response createTaxonomyTerm(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "Taxonomy term") TermBean termBean)
	{
		if( taxonomyService.isTaxonomyReadonly(taxonomyUuid) )
		{
			throw new WebException(Status.METHOD_NOT_ALLOWED.getStatusCode(),
				Status.METHOD_NOT_ALLOWED.getReasonPhrase(), "Taxonomy is readonly");
		}

		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);

		TermResult parentTerm = taxonomyService.getTermResultByUuid(taxonomyUuid, termBean.getParentUuid());
		int index = termBean.getIndex();

		if( index < 0 )
		{
			index = 0;
		}

		try
		{
			TermResult termResult = termService.insertTerm(taxonomy, parentTerm, termBean.getTerm(), index);
			return Response.created(getTermUrl(taxonomyUuid, termResult.getUuid())).build();
		}
		catch( Exception e )
		{
			throw new WebException(Status.NOT_ACCEPTABLE.getStatusCode(), Status.NOT_ACCEPTABLE.getReasonPhrase(),
				e.getMessage());
		}

	}

	/**
	 * Update term
	 * 
	 * @param uuid
	 * @param termBean
	 * @return
	 */
	@PUT
	@Path("/{uuid}/term/{termUuid}")
	@Produces("application/json")
	@ApiOperation(value = "Update term")
	public Response updateTaxonomyTerm(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "Term uuid", required = true) @PathParam("termUuid") String termUuid,
		@ApiParam(value = "Taxonomy term") TermBean termBean)
	{
		if( taxonomyService.isTaxonomyReadonly(taxonomyUuid) )
		{
			throw new WebException(Status.METHOD_NOT_ALLOWED.getStatusCode(),
				Status.METHOD_NOT_ALLOWED.getReasonPhrase(), "Taxonomy is readonly");
		}
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);

		TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);
		TermResult parentTerm = taxonomyService.getTermResultByUuid(taxonomyUuid, termBean.getParentUuid());

		if( term != null )
		{
			int index = termBean.getIndex();
			if( index < 0 )
			{
				index = 0;
			}
			if( term.getFullTerm() != termBean.getFullTerm() )
			{
				termService.move(taxonomy, term, parentTerm, index);
			}

			return Response.ok().location(getTermUrl(taxonomyUuid, termUuid)).build();
		}

		else
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}
	}

	/**
	 * Remove term
	 * 
	 * @param uuid
	 * @param termBean
	 * @return
	 */
	@DELETE
	@Path("/{uuid}/term/{termUuid}")
	@Produces("application/json")
	@ApiOperation(value = "Remove term")
	public Response rmTaxonomyTerm(
		@ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "Term uuid", required = true) @PathParam("termUuid") String termUuid)
	{
		if( taxonomyService.isTaxonomyReadonly(taxonomyUuid) )
		{
			throw new WebException(Status.METHOD_NOT_ALLOWED.getStatusCode(),
				Status.METHOD_NOT_ALLOWED.getReasonPhrase(), "Taxonomy is readonly");
		}

		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);

		TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);
		if( term == null )
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}
		termService.deleteTerm(taxonomy, term.getFullTerm());

		return Response.ok().build();
	}

	/**
	 * Returns terms
	 * 
	 * @return Response encapsulating TermBeans
	 */
	@GET
	@Path("/{uuid}/term/{termUuid}")
	@Produces("application/json")
	@ApiOperation(value = "Get term by UUID")
	public Response getTermByUuid(
		@ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid)
	{
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);
		TermResult term = taxonomyService.getTermResultByUuid(taxonomyUuid, termUuid);

		TermBean bean = null;
		if( term != null )
		{
			bean = beanFromTaxonomyTerm(term, taxonomyUuid);
			return Response.ok(bean).build();
		}
		else
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}
	}

	/**
	 * Returns term's all data
	 * 
	 * @param taxonomyUuid
	 * @param termUuid
	 * @return
	 */
	@GET
	@Path("/{uuid}/term/{termUuid}/data")
	@Produces("application/json")
	@ApiOperation(value = "Data")
	public Response getAllTermData(
		@ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid)
	{
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);
		try
		{
			Map<String, String> data = taxonomyService.getAllDataByTermUuid(taxonomyUuid, termUuid);

			return Response.ok(data).build();
		}
		catch( IllegalArgumentException ex )
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}
	}

	/**
	 * Returns term's data value
	 * 
	 * @param taxonomyUuid
	 * @param termUuid
	 * @param dataKey
	 * @return
	 */
	@GET
	@Path("/{uuid}/term/{termUuid}/data/{datakey}")
	@Produces("application/json")
	@ApiOperation(value = "Get term data")
	public Response getTermDataByKey(
		@ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
		@ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey)
	{
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.VIEW);

		String value = null;
		try
		{
			value = taxonomyService.getDataByTermUuid(taxonomyUuid, termUuid, dataKey);
		}
		catch( IllegalArgumentException ex )
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}
		if( value == null )
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		else
		{
			Map<String, String> data = Maps.newHashMap();
			data.put(dataKey, value);
			return Response.ok(data).build();
		}
	}

	/**
	 * Set term data value
	 * 
	 * @param taxonomyUuid
	 * @param termUuid
	 * @param dataKey
	 * @return
	 */
	@PUT
	@Path("/{uuid}/term/{termUuid}/data/{datakey}/{datavalue}")
	@Produces("application/json")
	@ApiOperation(value = "Set term data")
	public Response setTermData(
		@ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
		@ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey,
		@ApiParam(value = "data value", required = true) @PathParam("datakey") String dataValue)
	{
		if( taxonomyService.isTaxonomyReadonly(taxonomyUuid) )
		{
			throw new WebException(Status.METHOD_NOT_ALLOWED.getStatusCode(),
				Status.METHOD_NOT_ALLOWED.getReasonPhrase(), "Taxonomy is readonly");
		}
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
		try
		{
			termService.setDataByTermUuid(taxonomy, termUuid, dataKey, dataValue);
		}
		catch( IllegalArgumentException ex )
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}

		return Response.ok().build();

	}

	/**
	 * Remove term data
	 * 
	 * @param taxonomyUuid
	 * @param termUuid
	 * @param dataKey
	 * @return
	 */
	@DELETE
	@Path("/{uuid}/term/{termUuid}/data/{datakey}")
	@Produces("application/json")
	@ApiOperation(value = "Delete term data")
	public Response deleteTermData(
		@ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
		@ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
		@ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey)
	{
		if( taxonomyService.isTaxonomyReadonly(taxonomyUuid) )
		{
			throw new WebException(Status.METHOD_NOT_ALLOWED.getStatusCode(),
				Status.METHOD_NOT_ALLOWED.getReasonPhrase(), "Taxonomy is readonly");
		}
		final Taxonomy taxonomy = ensureTaxonomy(taxonomyUuid, PrivCheck.EDIT);
		try
		{
			termService.setDataByTermUuid(taxonomy, termUuid, dataKey, null);
		}
		catch( IllegalArgumentException ex )
		{
			throw new WebException(Status.NOT_FOUND.getStatusCode(), Status.NOT_FOUND.getReasonPhrase(),
				"termUuid given is not valid");
		}

		return Response.ok().build();
	}

	private TermBean beanFromTaxonomyTerm(TermResult term, String taxonomyUuid)
	{
		TermBean bean = new TermBean();
		bean.setTerm(term.getTerm());
		bean.setFullTerm(term.getFullTerm());
		bean.setUuid(term.getUuid());
		Map<String, String> links = Maps.newHashMap();
		links.put("self", getTermUrl(taxonomyUuid, term.getUuid()).toString());
		return bean;
	}

	private URI getTermUrl(String taxonomyUuid, String termUuid)
	{
		try
		{
			String url = institutionService.institutionalise("api/taxonomy/" + taxonomyUuid + "/term/" + termUuid);
			return new URI(url);
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	private Taxonomy ensureTaxonomy(String taxonomyUuid, PrivCheck pc)
	{
		final Taxonomy taxonomy = taxonomyService.getByUuid(taxonomyUuid);
		if( taxonomy == null )
		{
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if( (pc == PrivCheck.VIEW && !taxonomyService.canView(taxonomy))
			|| (pc == PrivCheck.EDIT && !taxonomyService.canEdit(taxonomy))
			|| (pc == PrivCheck.DELETE && !taxonomyService.canDelete(taxonomy)) )
		{
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return taxonomy;
	}
}
