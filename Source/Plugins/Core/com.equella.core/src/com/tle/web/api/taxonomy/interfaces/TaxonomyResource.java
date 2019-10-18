/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.taxonomy.interfaces;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.beans.taxonomy.TermBean;
import com.tle.web.api.interfaces.BaseEntityResource;
import com.tle.web.api.interfaces.beans.EntityLockBean;
import com.tle.web.api.interfaces.beans.PagingBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@NonNullByDefault
@Path("taxonomy")
@Api(value = "Taxonomies", description = "taxonomy")
@Produces({"application/json"})
public interface TaxonomyResource extends BaseEntityResource<TaxonomyBean, BaseEntitySecurityBean> {
  @GET
  @Path("/")
  @ApiOperation(value = "List all taxonomies", notes = "Retrieve a list of all taxonomies")
  PagingBean<TaxonomyBean> list(
      @Context UriInfo uriInfo,
      @ApiParam("Search name and description") @QueryParam("q") String q,
      @ApiParam("Privilege(s) to filter by") @QueryParam("privilege") List<String> privilege,
      @QueryParam("resumption") @ApiParam("Resumption token for paging") String resumptionToken,
      @QueryParam("length") @ApiParam("Number of results") @DefaultValue("10") int length,
      @QueryParam("full") @ApiParam("Return full entity (needs VIEW or EDIT privilege)")
          boolean full);

  @GET
  @Path("/acl")
  @ApiOperation(value = "List global taxonomy acls", notes = "Manage global taxonomy ACLs")
  BaseEntitySecurityBean getAcls(@Context UriInfo uriInfo);

  @PUT
  @Path("/acl")
  @ApiOperation(value = "Edit global taxonomy acls")
  Response editAcls(
      @Context UriInfo uriInfo,
      @ApiParam(value = "The global taxonomy acls") BaseEntitySecurityBean security);

  @GET
  @Path("/{uuid}")
  @ApiOperation(value = "Get a taxonomy", notes = "Manage taxonomies")
  TaxonomyBean get(
      @Context UriInfo uriInfo, @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid);

  @DELETE
  @Path("/{uuid}")
  @ApiOperation("Delete a taxonomy")
  Response delete(
      @Context UriInfo uriInfo, @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid);

  @POST
  @ApiOperation(value = "Create a new taxonomy", notes = "Create new or list existing taxonomies")
  @ApiResponses({@ApiResponse(code = 201, message = "Location: {newtaxonomy uri}")})
  Response create(
      @Context UriInfo uriInfo,
      @ApiParam(value = "Taxonomy") TaxonomyBean bean,
      @ApiParam(required = false, value = "Staging area UUID") @QueryParam(value = "file")
          String stagingUuid);

  @PUT
  @Path("/{uuid}")
  @ApiOperation("Edit a taxonomy")
  @ApiResponses({@ApiResponse(code = 200, message = "Location: {taxonomy uri}")})
  Response edit(
      @Context UriInfo uriInfo,
      @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid,
      @ApiParam TaxonomyBean bean,
      @ApiParam(required = false, value = "Staging area UUID") @QueryParam("file")
          String stagingUuid,
      @ApiParam(required = false, value = "The lock UUID if locked") @QueryParam("lock")
          String lockId,
      @ApiParam(value = "Unlock taxonomy after edit") @QueryParam("keeplocked") boolean keepLocked);

  @GET
  @Path("/{uuid}/lock")
  @ApiOperation(value = "Read the lock for a taxonomy", response = EntityLockBean.class)
  Response getLock(
      @Context UriInfo uriInfo, @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid);

  @POST
  @Path("/{uuid}/lock")
  @ApiOperation(
      value = "Lock a taxonomy",
      notes = "A taxonomy lock will prevent others from editing it while locked",
      response = EntityLockBean.class)
  Response lock(
      @Context UriInfo uriInfo, @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid);

  @DELETE
  @Path("/{uuid}/lock")
  @ApiOperation("Unlock a taxonomy")
  Response unlock(
      @Context UriInfo uriInfo, @ApiParam(value = "Taxonomy UUID") @PathParam("uuid") String uuid);

  @GET
  @Path("/{uuid}/search")
  @ApiOperation(value = "Search terms")
  Response searchTaxonomyTerms(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String uuid,
      @ApiParam(value = "Term query", required = true) @QueryParam("q") String query,
      @ApiParam(
              value = "Selection restriction",
              required = false,
              defaultValue = "UNRESTRICTED",
              allowableValues = "TOP_LEVEL_ONLY,LEAF_ONLY,UNRESTRICTED")
          @QueryParam("restriction")
          String restriction,
      @ApiParam(value = "Limit number of results", required = false, defaultValue = "20")
          @QueryParam("limit")
          int limit,
      @ApiParam(value = "search fullterm", required = false, defaultValue = "false")
          @QueryParam("searchfullterm")
          boolean searchfullterm);

  /**
   * Returns terms
   *
   * @return Response encapsulating TermBeans
   */
  @GET
  @Path("/{uuid}/term")
  @ApiOperation(value = "Get taxonomy terms")
  Response getTaxonomyTerms(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "Term path", required = false) @QueryParam("path") String path);

  /**
   * Insert new term
   *
   * @param taxonomyUuid
   * @param termBean
   * @return
   */
  @POST
  @Path("/{uuid}/term")
  @ApiOperation(value = "Create taxonomy term")
  Response createTaxonomyTerm(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "Taxonomy term") TermBean termBean);

  /**
   * Update term
   *
   * @param taxonomyUuid
   * @param termUuid
   * @param termBean
   * @return
   */
  @PUT
  @Path("/{uuid}/term/{termUuid}")
  @ApiOperation(value = "Update term")
  Response updateTaxonomyTerm(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "Term uuid", required = true) @PathParam("termUuid") String termUuid,
      @ApiParam(value = "Taxonomy term") TermBean termBean);

  /**
   * Remove term
   *
   * @param taxonomyUuid
   * @param termUuid
   * @return
   */
  @DELETE
  @Path("/{uuid}/term/{termUuid}")
  @ApiOperation(value = "Remove term")
  Response rmTaxonomyTerm(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "Term uuid", required = true) @PathParam("termUuid") String termUuid);

  /**
   * Returns terms
   *
   * @return Response encapsulating TermBeans
   */
  @GET
  @Path("/{uuid}/term/{termUuid}")
  @ApiOperation(value = "Get term by UUID")
  Response getTermByUuid(
      @ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid);

  /**
   * Returns term's all data
   *
   * @param taxonomyUuid
   * @param termUuid
   * @return
   */
  @GET
  @Path("/{uuid}/term/{termUuid}/data")
  @ApiOperation(value = "Data")
  Response getAllTermData(
      @ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid);

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
  @ApiOperation(value = "Get term data")
  Response getTermDataByKey(
      @ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
      @ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey);

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
  @ApiOperation(value = "Set term data")
  Response setTermData(
      @ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
      @ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey,
      @ApiParam(value = "data value", required = true) @PathParam("datakey") String dataValue);

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
  @ApiOperation(value = "Delete term data")
  Response deleteTermData(
      @ApiParam(value = "Taxonomy uuid", required = false) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "term uuid", required = true) @PathParam("termUuid") String termUuid,
      @ApiParam(value = "data key", required = true) @PathParam("datakey") String dataKey);

  /**
   * Returns terms
   *
   * @return OK
   */
  @POST
  @Path("/{uuid}/sortchildren")
  @ApiOperation(value = "Sort child terms")
  Response sortChildTerms(
      @ApiParam(value = "Taxonomy uuid", required = true) @PathParam("uuid") String taxonomyUuid,
      @ApiParam(value = "Term path", required = false) @QueryParam("path") String path);
}
