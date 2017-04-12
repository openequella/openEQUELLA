package com.tle.web.api.payment.store.resource;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.dytech.edge.exceptions.WebException;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.tle.common.Check;
import com.tle.common.interfaces.CsvList;
import com.tle.common.interfaces.SimpleI18NString;
import com.tle.common.payment.entity.PaymentGateway;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.StoreFront;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.common.payment.entity.TaxType;
import com.tle.core.filesystem.SystemFile;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentSettings;
import com.tle.core.payment.StoreSettings;
import com.tle.core.payment.beans.store.DecimalNumberBean;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanSerializer;
import com.tle.core.payment.service.PaymentGatewayService;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.payment.service.TaxService;
import com.tle.core.services.UrlService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Bind
@Singleton
@Path("store")
@Api(value = "/store", description = "store")
@Produces({"application/json"})
public class StoreResource extends AbstractStoreResource
{
	private static final String URL_DEFAULT_SMALL_ICON = resources.url("images/equella_small.png");
	private static final String URL_DEFAULT_LARGE_ICON = resources.url("images/equella.png");

	@Inject
	private ConfigurationService configService;
	@Inject
	private StoreBeanSerializer converter;
	@Inject
	private PricingTierService tierService;
	@Inject
	private PaymentService paymentService;
	@Inject
	private PaymentGatewayService paymentGatewayService;
	@Inject
	private UrlService urlService;
	@Inject
	private TaxService taxService;
	@Inject
	private UrlLinkService urlLinkService;

	@ApiOperation(value = "This is the GET for Store Resource")
	@GET
	@Path("")
	public StoreBean get()
	{
		final StoreBean bean = new StoreBean();
		final StoreSettings storeSettings = getStoreSettings();

		bean.setContactEmail(storeSettings.getContactEmail());
		bean.setContactName(storeSettings.getContactName());
		bean.setContactNumber(storeSettings.getContactNumber());
		bean.setDescription(new SimpleI18NString(storeSettings.getDescription()));
		bean.setEnabled(storeSettings.getEnabled());

		StoreSettings settings = configService.getProperties(new StoreSettings());

		String small = urlService.institutionalise("api/store/icon/small.jpg"); //$NON-NLS-1$
		String large = urlService.institutionalise("api/store/icon/large.jpg"); //$NON-NLS-1$

		if( Check.isEmpty(settings.getIconSmall()) )
		{
			small = urlService.institutionalise(resources.url("images/equella_small.png")); //$NON-NLS-1$
		}
		if( Check.isEmpty(settings.getIcon()) )
		{
			large = urlService.institutionalise(resources.url("images/equella.png")); //$NON-NLS-1$
		}

		bean.setIcon(small);
		bean.setImage(large);
		final String name = storeSettings.getName();
		bean.setName(new SimpleI18NString(name));
		// EQ-2158. There's only a simple string, with no local information
		bean.setNameStrings(null);

		final Map<String, String> linkMap = Maps.newHashMap();

		linkMap.put("self", urlLinkService.getMethodUriBuilder(getClass(), "get").build().toString());
		linkMap.put("pricing", urlLinkService.getMethodUriBuilder(getClass(), "getPricingInfo").build().toString());
		linkMap.put("cataloges", urlLinkService.getMethodUriBuilder(StoreCatalogueResource.class, "getCatalogues")
			.build().toString());

		bean.set("links", linkMap);

		return bean;
	}

	@ApiOperation(value = "Get pricing information for this store, relevant to the connected store front")
	@GET
	@Path("/pricing")
	public StorePricingInformationBean getPricingInfo(
		@ApiParam(value = "How much information to return for the results", required = false, allowableValues = ItemResource.ALL_ALLOWABLE_INFOS, allowMultiple = true) @QueryParam("info") CsvList info)
	{
		final StorePricingInformationBean bean = new StorePricingInformationBean();

		final PaymentSettings paymentSetting = getPaymentSettings();
		final StoreFront sf = getStoreFront();
		final List<String> infos = CsvList.asList(info, "basic");
		final boolean free = sf.isAllowFree() && paymentSetting.isFreeEnabled();
		final boolean purchase = sf.isAllowPurchase() && paymentSetting.isPurchaseEnabled();
		final boolean subscription = sf.isAllowSubscription() && paymentSetting.isSubscriptionEnabled();
		bean.setAllowFree(free);
		bean.setAllowPurchase(purchase);
		bean.setAllowSubscription(subscription);

		final List<TaxType> taxes = getTaxes(sf);
		final TaxCalculator taxCalculator = new TaxCalculator(taxService, taxes);
		if( purchase )
		{
			final List<StorePurchaseTierBean> purchaseTiers = Lists.newArrayList();
			final boolean purchasePerUser = !paymentSetting.isPurchaseFlatRate();

			for( PricingTier tier : tierService.enumerateEnabled(true) )
			{
				purchaseTiers.add(converter.convertPurchaseTierToBean(tier, purchasePerUser, taxCalculator, taxes));
			}

			bean.setPurchasePerUser(purchasePerUser);
			bean.setPurchaseTiers(purchaseTiers);
		}

		if( subscription )
		{
			final List<StoreSubscriptionTierBean> subscriptionTiers = Lists.newArrayList();
			final List<StoreSubscriptionPeriodBean> subscriptionPeriods = Lists.newArrayList();
			final boolean subscriptionPerUser = !paymentSetting.isSubscriptionFlatRate();

			for( PricingTier tier : tierService.enumerateEnabled(false) )
			{
				subscriptionTiers.add(converter.convertSubscriptionTierToBean(tier, subscriptionPerUser, taxCalculator,
					taxes));
			}
			for( SubscriptionPeriod period : paymentService.enumerateSubscriptionPeriods() )
			{
				subscriptionPeriods.add(converter.convertSubscriptionPeriodToBean(period));
			}

			bean.setSubscriptionPerUser(subscriptionPerUser);
			bean.setSubscriptionTiers(subscriptionTiers);
			bean.setSubscriptionPeriods(subscriptionPeriods);
		}

		if( infos.contains("all") )
		{
			final List<StorePaymentGatewayBean> gatewayBeans = Lists.newArrayList();
			final List<PaymentGateway> paymentGateways = paymentGatewayService.enumerateEnabled();
			for( PaymentGateway pg : paymentGateways )
			{
				gatewayBeans.add(converter.convertPaymentGatewayToBean(pg));
			}
			bean.setPaymentGateways(gatewayBeans);
		}
		bean.setDefaultCurrency(paymentSetting.getCurrency());
		final TaxType tax = sf.getTaxType();
		final StoreTaxBean taxBean = new StoreTaxBean();
		if( tax != null )
		{
			taxBean.setRate(new DecimalNumberBean(tax.getPercent()));
			taxBean.setCode(tax.getCode());
		}
		else
		{
			taxBean.setRate(new DecimalNumberBean(BigDecimal.ZERO));
		}
		bean.setTax(taxBean);
		return bean;
	}

	private List<TaxType> getTaxes(StoreFront storeFront)
	{
		TaxType taxType = storeFront.getTaxType();
		if( taxType != null )
		{
			return Lists.newArrayList(taxType);
		}
		return Collections.emptyList();
	}

	@ApiOperation(value = "Get the store image")
	@GET
	@Path("/icon/large.jpg")
	public Response getLargeIcon(@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		StoreSettings settings = configService.getProperties(new StoreSettings());
		String filename = settings.getIcon();
		if( !Check.isEmpty(filename) )
		{
			return serveContentStreamResponse(new SystemFile(), filename, request, response);
		}

		URI uriLarge;
		try
		{
			uriLarge = new URI(urlService.institutionalise(URL_DEFAULT_LARGE_ICON));
			return Response.status(Status.SEE_OTHER).location(uriLarge).build();
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@ApiOperation(value = "Get the store icon")
	@GET
	@Path("/icon/small.jpg")
	public Response getSmallIcon(@Context HttpServletRequest request, @Context HttpServletResponse response)
	{
		StoreSettings settings = configService.getProperties(new StoreSettings());
		String filename = settings.getIconSmall();
		if( !Check.isEmpty(filename) )
		{
			return serveContentStreamResponse(new SystemFile(), filename, request, response);
		}

		try
		{
			URI uriSmall = new URI(urlService.institutionalise(URL_DEFAULT_SMALL_ICON));
			return Response.status(Status.SEE_OTHER).location(uriSmall).build();
		}
		catch( URISyntaxException e )
		{
			throw Throwables.propagate(e);
		}
	}

	/**
	 * For testing a URL for 'store-ness'
	 * 
	 * @return
	 */
	@GET
	@Path("/test")
	public Response test()
	{
		StoreSettings settings = configService.getProperties(new StoreSettings());
		if( settings.getEnabled() )
		{
			return Response.ok().build();
		}
		throw new WebException(403, "not_store", resources.getString("error.notstore"));
	}

	private StoreSettings getStoreSettings()
	{
		return configService.getProperties(new StoreSettings());
	}

	private PaymentSettings getPaymentSettings()
	{
		return configService.getProperties(new PaymentSettings());
	}
}
