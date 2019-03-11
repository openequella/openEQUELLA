package com.tle.web.api.search.interfaces;

import com.tle.common.interfaces.CsvList;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.search.interfaces.beans.FacetSearchBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("search")
@Api(value = "/search", description = "search")
@Produces(MediaType.APPLICATION_JSON)
public interface SearchResource {
  /**
   * @param q The text query
   * @param start The index of the first record to return. The first available record is at 0
   * @param length The number of results to return
   * @param collections A comma separated list of collection UUIDs (optional). If omitted, all
   *     collections will be searched.
   * @param order One of: "relevance", "modified", "name", "rating"
   * @param reverse Reverse the order of the results.
   * @param where A complex where query.
   * @param info A comma separated list of field groups to return.
   * @param showall If true then includes draft items in the search results.
   * @return JSON format
   */
  @GET
  @Path("/")
  @ApiOperation(
      value = "Search for items",
      notes = "Search for items that the current user can discover")
  @ApiResponses({
    @ApiResponse(
        code = 200,
        message = "{\n  \"start\":0,\n  \"length\":0,\n  \"available\":0,\n  \"results\":[]\n}",
        response = SearchBean.class)
  })
  //
  // @formatter:off
  public SearchBean<ItemBean> searchItems(
      @Context UriInfo uriInfo,
      @ApiParam(value = "Query string", required = false) @QueryParam("q") String q,
      @ApiParam(
              value = "The first record of the search results to return",
              required = false,
              defaultValue = "0")
          @QueryParam("start")
          int start,
      @ApiParam(
              value = "The number of results to return",
              required = false,
              defaultValue = "10",
              allowableValues = "range[0,50]")
          @QueryParam("length")
          @DefaultValue("10")
          int length,
      @ApiParam(value = "List of collections", required = false) @QueryParam("collections")
          CsvList collections,
      @ApiParam(
              value = "The order of the search results",
              allowableValues = ",relevance,modified,name,rating",
              required = false)
          @QueryParam("order")
          String order,
      @ApiParam(
              value = "Reverse the order of the search results",
              allowableValues = ",true,false",
              defaultValue = "false",
              required = false)
          @QueryParam("reverse")
          String reverse,
      @ApiParam(
              value =
                  "The where-clause in the same format as the old SOAP one. See http://code.pearson.com/equella/soap-api/searchitems-soapservice50",
              required = false)
          @QueryParam("where")
          String where,
      @ApiParam(
              value = "How much information to return for the results",
              required = false,
              allowableValues = ItemResource.ALL_ALLOWABLE_INFOS,
              allowMultiple = true)
          @QueryParam("info")
          CsvList info,
      @ApiParam(
              value = "If true then includes items that are not live",
              allowableValues = ",true,false",
              defaultValue = "false",
              required = false)
          @QueryParam("showall")
          String showall);
  // @formatter:on

  @GET
  @Path("/facet")
  @ApiOperation(value = "Perform a facet search")
  // @formatter:off
  public FacetSearchBean searchFacets(
      @ApiParam(value = "Comma seperated list of schema nodes to facet over", required = true)
          @QueryParam("nodes")
          CsvList nodes,
      @ApiParam(
              value =
                  "The level at which to nest the facet search, the selected node must be flagged as nested in the schema definition",
              required = false)
          @QueryParam("nest")
          String nestLevel,
      @ApiParam(value = "Query string", required = false) @QueryParam("q") String q,
      @ApiParam(
              value =
                  "The number of term combinations to search for, a higher number will return more results and more accurate counts, but will take longer",
              required = false,
              defaultValue = "10",
              allowableValues = "range[0,200]")
          @QueryParam("breadth")
          @DefaultValue("10")
          int breadth,
      @ApiParam(value = "List of collections", required = false) @QueryParam("collections")
          CsvList collections,
      @ApiParam(
              value =
                  "The where-clause in the same format as the old SOAP one. See http://code.pearson.com/equella/soap-api/searchitems-soapservice50",
              required = false)
          @QueryParam("where")
          String where,
      @ApiParam(
              value = "If true then includes items that are not live",
              allowableValues = "true,false",
              defaultValue = "false",
              required = false)
          @QueryParam("showall")
          String showall);
  // @formatter:on
}
