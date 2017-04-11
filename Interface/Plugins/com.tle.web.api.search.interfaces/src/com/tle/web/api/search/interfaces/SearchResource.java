package com.tle.web.api.search.interfaces;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.tle.common.interfaces.CsvList;
import com.tle.web.api.search.interfaces.beans.FacetSearchBean;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("search")
@Api(value = "/search", description = "search")
@Produces(MediaType.APPLICATION_JSON)
public interface SearchResource
{
	@GET
	@Path("/facet")
	@ApiOperation(value = "Perform a facet search")
	// @formatter:off
	public FacetSearchBean searchFacets(
		@ApiParam(value="Comma seperated list of schema nodes to facet over", required = true)
		@QueryParam("nodes")
			CsvList nodes,
		@ApiParam(value="The level at which to nest the facet search, the selected node must be flagged as nested in the schema definition", required = false)
		@QueryParam("nest")
			String nestLevel,
		@ApiParam(value="Query string", required = false) @QueryParam("q")
			String q,
		@ApiParam(value="The number of term combinations to search for, a higher number will return more results and more accurate counts, but will take longer", required = false, defaultValue = "10", allowableValues = "range[0,200]")
		@QueryParam("breadth")
		@DefaultValue("10")
			int breadth,
		@ApiParam(value="List of collections", required = false)
		@QueryParam("collections")
			CsvList collections,
		@ApiParam(value="The where-clause in the same format as the old SOAP one. See http://code.pearson.com/equella/soap-api/searchitems-soapservice50",
					required = false)
		@QueryParam("where")
			String where,
		@ApiParam(value="If true then includes items that are not live", allowableValues = "true,false", defaultValue = "false", required = false)
		@QueryParam("showall")
			String showall
		);
	// @formatter:on
}