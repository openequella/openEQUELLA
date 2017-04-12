package com.tle.web.api.payment.store.resource;

import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.NotFoundException;
import com.google.inject.Singleton;
import com.tle.common.payment.entity.Sale;
import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanDeserializer;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.service.SaleService;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * @author Aaron
 */
@Bind
@Singleton
@Path("store/checkout")
@Api(value = "/store/checkout", description = "store-checkout")
@Produces("application/json")
public class StoreCheckoutResource extends AbstractStoreResource
{
	@Inject
	private SaleService saleService;
	@Inject
	private StoreBeanSerializer serializer;
	@Inject
	private StoreBeanDeserializer deserializer;
	@Inject
	private UrlLinkService urlLinkService;

	@ApiOperation(value = "This is the post for Store checkout resource")
	@POST
	@Path("")
	@Consumes("application/json")
	public Response checkout(StoreCheckoutBean cart)
	{
		final Sale sale = deserializer.convertCartBeanToSale(getStoreFront(), cart);
		saleService.checkout(getStoreFront(), sale);

		return Response.status(Status.CREATED).location(getSelfLink(sale.getUuid()))
			.entity(serializer.convertSaleToCheckoutBean(sale)).build();
	}

	@ApiOperation(value = "This is the get for Store checkout resource")
	@GET
	@Path("/{uuid}")
	public StoreCheckoutBean getCheckout(@PathParam("uuid") String uuid)
	{
		final StoreCheckoutBean checkout = serializer.convertSaleToCheckoutBean(saleService.getSale(getStoreFront(),
			uuid));
		if( checkout == null )
		{
			throw new NotFoundException(uuid);
		}
		return checkout;
	}

	private URI getSelfLink(String uuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getCheckout").build(uuid); //$NON-NLS-1$
	}
}
