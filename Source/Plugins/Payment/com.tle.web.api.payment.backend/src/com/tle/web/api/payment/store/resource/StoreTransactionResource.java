package com.tle.web.api.payment.store.resource;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.payment.SaleSearchResults;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.beans.store.StoreTransactionsBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.service.SaleService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * @author Aaron
 */
@Bind
@Singleton
@Path("store/transaction")
@Api(value = "/store/transaction", description = "store-transaction")
@Produces("application/json")
public class StoreTransactionResource extends AbstractStoreResource
{
	@Inject
	private SaleService saleService;
	@Inject
	private StoreBeanSerializer serialiser;

	@ApiOperation(value = "Get a history of your store transactions")
	@GET
	@Path("")
	public StoreTransactionsBean getTransactionsForCurrentStoreFront(
		@ApiParam(value = "The first record of the search results to return", required = false, defaultValue = "0") @QueryParam("start") int start,
		@ApiParam(value = "The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,100]") @QueryParam("length") int length,
		@ApiParam(value = "Reverse the order of the search results", allowableValues = "true,false", defaultValue = "false", required = false) @QueryParam("reverse") boolean reverse,
		@ApiParam(value = "Customer reference number", required = false) @QueryParam("customerReference") String customerReference)
	{
		// ApiParam annotations and their defaults don't have any meaning for
		// store calls - manually apply
		int offset = (start < 0 ? 0 : start);
		int count = (length <= 0 ? 10 : length);

		StoreFront sf = getStoreFront();

		SaleSearchResults searchResults = saleService.search(sf, offset, count, customerReference);

		final List<StoreTransactionBean> transactionBeans = Lists.newArrayList();

		for( Sale sale : searchResults.getResults() )
		{
			StoreTransactionBean transactionBean = serialiser.convertSaleToTransactionBean(sale);
			transactionBeans.add(transactionBean);
		}

		StoreTransactionsBean returnBean = new StoreTransactionsBean();
		returnBean.setTransactions(transactionBeans);
		returnBean.setStart(searchResults.getOffset());
		returnBean.setLength(searchResults.getCount());
		returnBean.setAvailable(searchResults.getAvailable());
		return returnBean;
	}

	@ApiOperation(value = "Get an individual transaction")
	@GET
	@Path("/{uuid}")
	public StoreTransactionBean getTransaction(@PathParam("uuid") String uuid)
	{
		return serialiser.convertSaleToTransactionBean(saleService.getSale(getStoreFront(), uuid));
	}
}
