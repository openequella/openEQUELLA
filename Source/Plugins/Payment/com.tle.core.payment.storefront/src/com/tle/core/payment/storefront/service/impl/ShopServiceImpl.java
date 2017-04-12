package com.tle.core.payment.storefront.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.NotFoundException;
import com.dytech.edge.exceptions.WebException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.URLUtils;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.payment.JsonMapper;
import com.tle.core.payment.beans.store.DecimalNumberBean;
import com.tle.core.payment.beans.store.StoreBean;
import com.tle.core.payment.beans.store.StoreCatalogueBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StoreCatalogueSearchBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreCheckoutItemBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StoreHarvestableItemsBean;
import com.tle.core.payment.beans.store.StorePaymentGatewayBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.exception.OrderValueChangedException;
import com.tle.core.payment.storefront.service.ShopSearchResults;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.HttpService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.services.http.Request;
import com.tle.core.services.http.Request.Method;
import com.tle.core.services.http.Response;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.util.archive.ArchiveType;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.DebugSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(ShopService.class)
@Singleton
public class ShopServiceImpl implements ShopService
{
	private static final Logger LOGGER = Logger.getLogger(ShopService.class);
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(ShopService.class);

	private static final String ENDPOINT_STORE = "api/store/";
	private static final String ENDPOINT_CHECKOUT = ENDPOINT_STORE + "checkout/";
	private static final String ENDPOINT_TRANSACTION = ENDPOINT_STORE + "transaction/";
	private static final String ENDPOINT_CATALOGUE = ENDPOINT_STORE + "catalogue/";
	private static final String ENDPOINT_PRICING = ENDPOINT_STORE + "pricing/";
	private static final String ENDPOINT_HARVEST = ENDPOINT_STORE + "harvest/";

	/**
	 * Duplicated in OAuthWebConstants
	 */
	public static final String PARAM_ACCESS_TOKEN = "access_token";
	public static final String PARAM_ERROR_DESCRIPTION = "error_description";
	public static final String PARAM_ERROR = "error";

	private final Cache<StoreCacheKey, StoreCache> storeCache = CacheBuilder.newBuilder()
		.expireAfterWrite(5, TimeUnit.MINUTES).build();

	private static final String[] FORWARDABLE_REQUEST_HEADERS = {"If-Modified-Since", "Cache-Control", "Accept"};
	private static final String[] FORWARDABLE_RESPONSE_HEADERS = {"Content-Type", "Content-Length",
			"Content-Disposition", "Cache-Control", "Last-Modified", "Keep-Alive", "Connection"};

	@Inject
	private ConfigurationService configService;
	@Inject
	private HttpService client;
	@Inject
	private JsonMapper mapper;
	@Inject
	private FileSystemService fileService;

	private Request createRequest(String url, String token)
	{
		Request request = new Request(url);
		if( token != null )
		{
			request.addHeader("X-Authorization", PARAM_ACCESS_TOKEN + "=" + token);
		}
		return request;
	}

	@Override
	public String readToken(String storeUrl, String clientId, String ourUrl, String code)
	{
		final Request request = createRequest(PathUtils.filePath(storeUrl, "oauth/" + PARAM_ACCESS_TOKEN), null);
		request.addParameter("grant_type", "authorization_code");
		request.addParameter("client_id", clientId);
		request.addParameter("redirect_uri", ourUrl);
		request.addParameter("code", code);

		try( Response response = client.getWebContent(request, configService.getProxyDetails()) )
		{
			final ObjectNode rootNode = mapper.readJson(response.getInputStream());
			JsonNode jsonNode = rootNode != null ? rootNode.get(PARAM_ACCESS_TOKEN) : null;
			if( jsonNode != null )
			{
				return jsonNode.textValue();
			}
			// no access_token in message - look for error nodes
			String msg = null;
			if( rootNode != null )
			{
				jsonNode = rootNode.get(PARAM_ERROR);
				if( jsonNode != null )
				{
					msg = jsonNode.textValue();
					jsonNode = rootNode.get(PARAM_ERROR_DESCRIPTION);
					if( jsonNode != null )
					{
						msg += " - " + jsonNode.textValue();
					}
				}
				else
				{
					msg = "OAuth response unrecognisable - neither " + PARAM_ACCESS_TOKEN + " nor error content";
				}
			}
			else
			{
				msg = "OAuth response unreadable";
			}
			throw new RuntimeException(msg);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean testStoreUrl(String url)
	{
		try( Response response = client.getWebContent(
			createRequest(URLUtils.newURL(url, "api/store/test").toString(), null), configService.getProxyDetails(),
			false) )
		{
			final int status = response.getCode();
			if( status < 200 || status >= 400 )
			{
				ErrorResponse error = mapper
					.readObject(mapper.readJson(response.getInputStream()), ErrorResponse.class);
				throw new RuntimeException(error.getErrorDescription());
			}
			return status == 200;
		}
		catch( Exception e )
		{
			LOGGER.debug("testStoreUrl failed", e);
			return false;
		}
	}

	@Override
	public void clearCache(Store store)
	{
		storeCache.invalidate(new StoreCacheKey(CurrentInstitution.get().getUniqueId(), store.getUuid()));
	}

	@Override
	public List<StoreCatalogueBean> getCatalogues(Store store, boolean fresh)
	{
		final String storeUuid = store.getUuid();
		final StoreCache cache = ensureStoreCache(storeUuid);
		if( !fresh && !DebugSettings.isAutoTestMode() )
		{
			List<StoreCatalogueBean> catalogues = cache.getCatalogues();
			if( catalogues == null )
			{
				catalogues = getCatList(store);
				cache.setCatalogues(catalogues);
			}
			return catalogues;
		}
		final List<StoreCatalogueBean> catalogues = getCatList(store);
		cache.setCatalogues(catalogues);
		final StoreCacheKey key = new StoreCacheKey(CurrentInstitution.get().getUniqueId(), storeUuid);
		storeCache.put(key, cache);
		return catalogues;
	}

	private List<StoreCatalogueBean> getCatList(Store store)
	{
		try
		{
			final ArrayNode catalogues = getJsonFromServer(store, ENDPOINT_CATALOGUE, null, ArrayNode.class);
			final List<StoreCatalogueBean> list = Lists.newArrayList();

			for( Iterator<JsonNode> catalogue = catalogues.elements(); catalogue.hasNext(); )
			{
				final JsonNode next = catalogue.next();
				final StoreCatalogueBean bean = mapper.readObject((ObjectNode) next, StoreCatalogueBean.class);
				list.add(bean);
			}
			return list;
		}
		catch( NotFoundException nfe )
		{
			LOGGER.warn("'Not found' error from store");
		}
		catch( AccessDeniedException denied )
		{
			LOGGER.warn("'Access denied' error from store");
		}
		return new ArrayList<>();
	}

	@SecureOnCall(priv = StoreFrontConstants.PRIV_BROWSE_STORE)
	@Override
	public ShopSearchResults searchCatalogue(Store store, String catUuid, String query, String sort, boolean reverse,
		Date[] dateRange, String priceFilter, int start, int amount)
	{
		List<NameValue> params = new ArrayList<NameValue>();
		params.add(new NameValue("q", query));
		params.add(new NameValue("order", sort));
		params.add(new NameValue("reverse", Boolean.valueOf(reverse).toString()));
		params.add(new NameValue("start", Integer.valueOf(start).toString()));
		params.add(new NameValue("length", Integer.valueOf(amount).toString()));
		addDateParameters(params, dateRange);
		params.add(new NameValue("pricefilter", priceFilter));

		final StoreCatalogueSearchBean results = getBeanFromServer(store, ENDPOINT_CATALOGUE + catUuid + "/search",
			params, StoreCatalogueSearchBean.class);

		return new ShopSearchResults(results.getResults(), results.getLength(), results.getStart(),
			results.getAvailable(), results.getFiltered());
	}

	private void addDateParameters(List<NameValue> params, Date[] dateRange)
	{
		if( dateRange == null )
		{
			return;
		}

		String dateFilter;

		if( dateRange[0] != null )
		{
			if( dateRange[1] != null )
			{
				dateFilter = "between";
				params.add(new NameValue("datestart", ISO8601Utils.format(dateRange[0])));
				params.add(new NameValue("dateend", ISO8601Utils.format(dateRange[1])));
			}
			else
			{
				dateFilter = "after";
				params.add(new NameValue("datestart", ISO8601Utils.format(dateRange[0])));
			}
			params.add(new NameValue("datefilter", dateFilter));
		}
		else
		{
			if( dateRange[1] != null )
			{
				dateFilter = "before";
				params.add(new NameValue("datestart", ISO8601Utils.format(dateRange[1])));
				params.add(new NameValue("datefilter", dateFilter));
			}
		}
	}

	@Override
	public StoreCatalogueItemBean getCatalogueItem(Store store, String catUuid, String itemUuid)
	{
		StoreCatalogueItemBean item = getBeanFromServer(store,
			PathUtils.filePath(ENDPOINT_CATALOGUE, catUuid, "item", itemUuid), StoreCatalogueItemBean.class);
		return item;
	}

	@Override
	public StoreCheckoutBean submitOrder(Store store, OrderStorePart storePart, StoreTaxBean tax)
		throws OrderValueChangedException
	{
		final StoreCheckoutBean scb = new StoreCheckoutBean();
		scb.setCustomerReference(storePart.getUuid());

		final List<OrderItem> orderItems = storePart.getOrderItems();
		if( orderItems == null || orderItems.size() == 0 )
		{
			throw new RuntimeException(resources.getString("shop.error.noitems"));
		}

		final List<StoreCheckoutItemBean> checkoutItems = Lists.newArrayList();
		for( OrderItem orderItem : orderItems )
		{
			final StoreCheckoutItemBean scib = new StoreCheckoutItemBean();
			scib.setItemUuid(orderItem.getItemUuid());
			scib.setItemVersion(orderItem.getItemVersion());
			scib.setQuantity(orderItem.getUsers());
			scib.setPrice(price(orderItem.getPrice(), orderItem.getTax(), orderItem.getCurrency(), tax));
			scib.setUnitPrice(price(orderItem.getUnitPrice(), orderItem.getUnitTax(), orderItem.getCurrency(), tax));
			scib.setCatalogueUuid(orderItem.getCatUuid());

			final String purchaseTierUuid = orderItem.getPurchaseTierUuid();
			if( purchaseTierUuid != null )
			{
				final StorePurchaseTierBean purch = new StorePurchaseTierBean();
				purch.setUuid(purchaseTierUuid);
				scib.setPurchaseTier(purch);
			}
			final String subscriptionTierUuid = orderItem.getSubscriptionTierUuid();
			if( subscriptionTierUuid != null )
			{
				final StoreSubscriptionTierBean sub = new StoreSubscriptionTierBean();
				sub.setUuid(subscriptionTierUuid);
				scib.setSubscriptionTier(sub);
			}
			final String periodUuid = orderItem.getPeriodUuid();
			if( periodUuid != null )
			{
				final StoreSubscriptionPeriodBean period = new StoreSubscriptionPeriodBean();
				period.setUuid(periodUuid);
				scib.setSubscriptionPeriod(period);
				scib.setSubscriptionStartDate(orderItem.getSubscriptionStartDate());
			}

			checkoutItems.add(scib);
		}
		scb.setItems(checkoutItems);
		scb.setPrice(price(storePart.getPrice(), storePart.getTax(), storePart.getCurrency(), tax));

		StoreCheckoutBean actual = null;
		try
		{
			actual = mapper.readObject((ObjectNode) postToServer(store, ENDPOINT_CHECKOUT, scb, false),
				StoreCheckoutBean.class);
		}
		catch( NotFoundException nfe )
		{
			LOGGER.warn("'Not found' error from store");
		}
		catch( AccessDeniedException denied )
		{
			LOGGER.warn("'Access denied' error from store");
		}
		if( actual == null )
		{
			throw new RuntimeException(resources.getString("shop.error.nocheckout"));
		}
		return actual;
	}

	private StorePriceBean price(long value, long taxValue, Currency currency, StoreTaxBean tax)
	{
		final StorePriceBean price = new StorePriceBean();
		final int decimals = currency == null ? 0 : currency.getDefaultFractionDigits();
		price.setValue(new DecimalNumberBean(value, decimals));
		price.setTaxValue(new DecimalNumberBean(taxValue, decimals));
		final List<StoreTaxBean> taxes = Lists.newArrayList();
		if( tax != null )
		{
			taxes.add(tax);
		}
		price.setTaxes(taxes);
		price.setCurrency(currency == null ? null : currency.getCurrencyCode());
		return price;
	}

	@Override
	public StoreBean getStoreInformation(Store store, boolean fresh)
	{
		final StoreCache cache = ensureStoreCache(store.getUuid());
		if( !fresh && !DebugSettings.isAutoTestMode() )
		{
			StoreBean s = cache.getStore();
			if( s == null )
			{
				s = getBeanFromServer(store, ENDPOINT_STORE, StoreBean.class);
				cache.setStore(s);
			}
			return s;
		}
		final StoreBean storeBean = getBeanFromServer(store, ENDPOINT_STORE, StoreBean.class);
		cache.setStore(storeBean);
		return storeBean;
	}

	@Override
	public StoreBean getStoreInformation(String storeUrl, String token)
	{
		return getBeanFromServer(URLUtils.newURL(storeUrl, ENDPOINT_STORE).toString(), token, null, StoreBean.class);
	}

	@Override
	public StorePricingInformationBean getPricingInformation(Store store, boolean all)
	{
		List<NameValue> params = new ArrayList<NameValue>();
		if( all )
		{
			params.add(new NameValue("info", "all"));
		}

		final StorePricingInformationBean pricing = getBeanFromServer(store, ENDPOINT_PRICING, params,
			StorePricingInformationBean.class);
		return pricing;
	}

	@Override
	public StorePaymentGatewayBean getPaymentGateway(Store store, String gatewayUuid, boolean fresh)
	{
		StorePricingInformationBean pricing = getPricingInformation(store, true);

		for( StorePaymentGatewayBean pg : pricing.getPaymentGateways() )
		{
			if( pg.getUuid().equals(gatewayUuid) )
			{
				return pg;
			}
		}
		throw new RuntimeException(resources.getString("shop.error.paymentgatewaynotfound", gatewayUuid));
	}

	@Override
	public StoreTransactionBean getTransaction(Store store, String transactionUuid)
	{
		StoreTransactionBean stb = getBeanFromServer(store, ENDPOINT_TRANSACTION + transactionUuid,
			StoreTransactionBean.class);
		return stb;
	}

	@Override
	public StoreCheckoutBean getCheckout(Store store, String checkoutUuid)
	{
		return getBeanFromServer(store, ENDPOINT_CHECKOUT + checkoutUuid, StoreCheckoutBean.class);
	}

	@Override
	public StoreHarvestableItemsBean listHarvestableItems(Store store, Date from, int start, int length, boolean onlyNew)
	{
		List<NameValue> params = new ArrayList<NameValue>();
		params.add(new NameValue("info", "harvest"));
		params.add(new NameValue("updatedSince", ISO8601Utils.format((from == null ? new Date(0) : from))));
		params.add(new NameValue("start", String.valueOf(start)));
		params.add(new NameValue("length", String.valueOf(length)));
		params.add(new NameValue("new", Boolean.toString(onlyNew)));

		StoreHarvestableItemsBean itemsSearch = getBeanFromServer(store, ENDPOINT_HARVEST, params,
			StoreHarvestableItemsBean.class);

		return itemsSearch;
	}

	@Override
	public StoreHarvestableItemsBean listExpiredItems(Store store, Date from, Date until, int start, int length)
	{
		List<NameValue> params = new ArrayList<NameValue>();
		params.add(new NameValue("expired", Boolean.toString(true)));
		params.add(new NameValue("info", "harvest"));
		params.add(new NameValue("from", ISO8601Utils.format((from == null ? new Date(0) : from))));
		params.add(new NameValue("until", ISO8601Utils.format((until == null ? new Date() : until))));
		params.add(new NameValue("start", String.valueOf(start)));
		params.add(new NameValue("length", String.valueOf(length)));

		StoreHarvestableItemsBean itemsSearch = getBeanFromServer(store, ENDPOINT_HARVEST + "subscription", params,
			StoreHarvestableItemsBean.class);

		return itemsSearch;
	}

	@Override
	public StoreHarvestableItemsBean listActiveItems(Store store, Date from, Date until, int start, int length)
	{
		List<NameValue> params = new ArrayList<NameValue>();
		params.add(new NameValue("expired", Boolean.toString(false)));
		params.add(new NameValue("info", "harvest"));
		params.add(new NameValue("from", ISO8601Utils.format((from == null ? new Date(0) : from))));
		params.add(new NameValue("until", ISO8601Utils.format((until == null ? new Date() : until))));
		params.add(new NameValue("start", String.valueOf(start)));
		params.add(new NameValue("length", String.valueOf(length)));

		StoreHarvestableItemsBean itemsSearch = getBeanFromServer(store, ENDPOINT_HARVEST + "subscription", params,
			StoreHarvestableItemsBean.class);

		return itemsSearch;
	}

	@Override
	public StoreHarvestableItemBean getFullItem(Store store, String uuid)
	{
		StoreHarvestableItemBean bean = getBeanFromServer(store, ENDPOINT_HARVEST + "item/" + uuid,
			StoreHarvestableItemBean.class);

		return bean;
	}

	@Override
	public boolean isHarvestable(Store store, String uuid)
	{
		// Super raw!
		final Request request = createRequest(URLUtils.newURL(store.getStoreUrl(), ENDPOINT_HARVEST + "item/" + uuid)
			.toString(), store.getToken());
		// REST GET methods currently not supporting HEAD
		// request.setMethod(Method.HEAD);

		try( Response response = client.getWebContent(request, configService.getProxyDetails()) )
		{
			final int code = response.getCode();
			if( code == 200 )
			{
				return true;
			}
			if( code == 402 )
			{
				return false;
			}
			throw new WebException(code, response.getMessage(), "Error checking if harvestable item");
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void downloadItemFiles(Store store, StagingFile staging, StoreHarvestableItemBean item)
	{
		final String endpoint = ENDPOINT_HARVEST + "item/" + item.getUuid() + "/content";
		final String fullUrl = URLUtils.newURL(store.getStoreUrl(), endpoint).toString();
		final Request request = createRequest(fullUrl, store.getToken());

		try( Response response = client.getWebContent(request, configService.getProxyDetails()) )
		{
			final int statusCode = response.getCode();
			if( statusCode != 200 )
			{
				throw handleError(response, fullUrl, statusCode);
			}

			fileService.unzipFile(staging, response.getInputStream(), ArchiveType.TAR_GZ);
		}
		catch( NotFoundException nfe )
		{
			LOGGER.warn("'Not found' error from store");
		}
		catch( AccessDeniedException denied )
		{
			LOGGER.warn("'Access denied' error from store");
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private StoreCache ensureStoreCache(String storeUuid)
	{
		synchronized( storeCache )
		{
			final StoreCacheKey key = new StoreCacheKey(CurrentInstitution.get().getUniqueId(), storeUuid);
			StoreCache info = storeCache.getIfPresent(key);
			if( info == null )
			{
				info = new StoreCache();
				storeCache.put(key, info);
			}
			return info;
		}
	}

	private <T> T getBeanFromServer(Store store, String endpoint, Class<T> beanClass)
	{
		return getBeanFromServer(store, endpoint, null, beanClass);
	}

	private <T> T getBeanFromServer(Store store, String endpoint, List<NameValue> params, Class<T> beanClass)
	{
		return getBeanFromServer(URLUtils.newURL(store.getStoreUrl(), endpoint).toString(), store.getToken(), params,
			beanClass);
	}

	private <T> T getBeanFromServer(String fullUrl, String token, List<NameValue> params, Class<T> beanClass)
	{
		try
		{
			final ObjectNode json = getJsonFromServer(fullUrl, token, params, ObjectNode.class);
			if( json == null )
			{
				return null;
			}
			return mapper.readObject(json, beanClass);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private <T extends JsonNode> T getJsonFromServer(Store store, String endpoint, List<NameValue> params,
		Class<T> nodeClass)
	{
		return getJsonFromServer(URLUtils.newURL(store.getStoreUrl(), endpoint).toString(), store.getToken(), params,
			nodeClass);
	}

	@SuppressWarnings("unchecked")
	private <T extends JsonNode> T getJsonFromServer(String fullUrl, String token, List<NameValue> params,
		Class<T> nodeClass)
	{
		final Request request = createRequest(fullUrl, token);
		if( params != null )
		{
			for( NameValue nv : params )
			{
				request.addParameter(nv.getName(), nv.getValue());
			}
		}

		try( Response response = client.getWebContent(request, configService.getProxyDetails()) )
		{
			final int statusCode = response.getCode();
			if( statusCode != 200 )
			{
				throw handleError(response, fullUrl, statusCode);
			}
			if( nodeClass == ArrayNode.class )
			{
				return (T) mapper.readJsonToArray(response.getInputStream());
			}
			return (T) mapper.readJson(response.getInputStream());
		}
		catch( Exception e )
		{
			LOGGER.error("Error with store at URL " + fullUrl);
			throw Throwables.propagate(e);
		}
	}

	private RuntimeException handleError(Response response, String url, int statusCode)
	{
		ErrorResponse errorObj = null;
		try
		{
			final ObjectNode errorNode = mapper.readJson(response.getInputStream());
			errorObj = mapper.readObject(errorNode, ErrorResponse.class);
		}
		catch( Exception e )
		{
			try
			{
				if( response.isStreaming() )
				{
					response.getInputStream().close();
				}
			}
			catch( IOException io )
			{
				// meh. What are you going to do?
				LOGGER.debug("consume entity failed", io);
			}

			final String msg = resources.getString("shop.error.remote", url, statusCode);
			if( statusCode == 404 )
			{
				throw new NotFoundException(msg);
			}
			if( statusCode == 403 )
			{
				throw new AccessDeniedException(msg);
			}

			throw new RuntimeException(msg);
		}

		final String errorMessage = errorObj.getErrorDescription();
		if( statusCode == 404 )
		{
			throw new NotFoundException(errorMessage);
		}
		if( statusCode == 403 )
		{
			throw new AccessDeniedException(errorMessage);
		}
		// How do I shot web?
		throw new WebException(statusCode, errorObj.getError(), errorMessage);
	}

	@SuppressWarnings("unchecked")
	private <T> T postToServer(Store store, String endpoint, Object data, boolean locationResponseOnly)
	{
		final String fullUrl = URLUtils.newURL(store.getStoreUrl(), endpoint).toString();

		final Request post = createRequest(fullUrl, store.getToken());
		post.setMethod(Method.POST);
		try
		{
			post.setBody(mapper.convertToJson(data));
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
		post.setMimeType("application/json");
		post.setCharset("UTF-8");

		try( Response response = client.getWebContent(post, configService.getProxyDetails()) )
		{
			final int statusCode = response.getCode();
			if( statusCode != 201 )
			{
				throw handleError(response, fullUrl, statusCode);
			}
			if( locationResponseOnly )
			{
				return (T) response.getHeader("Location");
			}
			return (T) mapper.readJson(response.getInputStream());
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	public static class ErrorResponse
	{
		private int code;
		private String error;
		private String errorDescription;

		@JsonProperty("code")
		public int getCode()
		{
			return code;
		}

		public void setCode(int code)
		{
			this.code = code;
		}

		@JsonProperty("error")
		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}

		@JsonProperty("error_description")
		public String getErrorDescription()
		{
			return errorDescription;
		}

		public void setErrorDescription(String errorDescription)
		{
			this.errorDescription = errorDescription;
		}
	}

	private static class StoreCacheKey
	{
		private final long institution;
		private final String storeUuid;

		public StoreCacheKey(long institution, String storeUuid)
		{
			this.institution = institution;
			this.storeUuid = storeUuid;
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof StoreCacheKey )
			{
				StoreCacheKey other = (StoreCacheKey) obj;
				return storeUuid.equals(other.storeUuid) && institution == other.institution;
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return (int) institution + storeUuid.hashCode();
		}
	}

	private static class StoreCache
	{
		private StoreBean store;
		private List<StoreCatalogueBean> catalogues;

		protected StoreCache()
		{
		}

		public StoreBean getStore()
		{
			return store;
		}

		public void setStore(StoreBean store)
		{
			this.store = store;
		}

		public List<StoreCatalogueBean> getCatalogues()
		{
			return catalogues;
		}

		public void setCatalogues(List<StoreCatalogueBean> catalogues)
		{
			this.catalogues = catalogues;
		}
	}

	@Override
	public void getPreviewAttachment(Store store, String catUuid, String itemUuid, String attachmentUuid,
		HttpServletRequest request, HttpServletResponse response)
	{
		Request req = createRequest(PathUtils.filePath(store.getStoreUrl(), ENDPOINT_CATALOGUE, catUuid, "item",
			itemUuid, "attachment", attachmentUuid), store.getToken());

		Enumeration<String> headers = request.getHeaderNames();
		while( headers.hasMoreElements() )
		{
			String header = headers.nextElement();
			if( canForwardRequestHeader(header) )
			{
				Enumeration<String> hvs = request.getHeaders(header);
				while( hvs.hasMoreElements() )
				{
					String hv = hvs.nextElement();
					req.addHeader(header, hv);
				}
			}
		}

		try( Response storeResponse = client.getWebContent(req, configService.getProxyDetails()) )
		{
			response.setStatus(storeResponse.getCode());

			for( NameValue header : storeResponse.getHeaders() )
			{
				//Be more selective in what we forward on
				String headerName = header.getName();
				if( canForwardResponseHeader(headerName) )
				{
					response.addHeader(headerName, header.getValue());
				}
			}

			storeResponse.copy(response.getOutputStream());
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private boolean canForwardRequestHeader(String headerName)
	{
		return canForwardHeader(headerName, FORWARDABLE_REQUEST_HEADERS);
	}

	private boolean canForwardResponseHeader(String headerName)
	{
		return canForwardHeader(headerName, FORWARDABLE_RESPONSE_HEADERS);
	}

	private boolean canForwardHeader(String headerName, String[] allowedHeaders)
	{
		for( String allowed : allowedHeaders )
		{
			if( headerName.equalsIgnoreCase(allowed) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public StoreCatalogueBean getCatalogue(Store store, String uuid, boolean fresh)
	{
		final String storeUuid = store.getUuid();
		final StoreCache cache = ensureStoreCache(storeUuid);
		if( !fresh && !DebugSettings.isAutoTestMode() )
		{
			// try the cache first
			List<StoreCatalogueBean> catalogues = cache.getCatalogues();
			if( catalogues != null )
			{
				for( StoreCatalogueBean catalogue : catalogues )
				{
					if( uuid.equals(catalogue.getUuid()) )
					{
						return catalogue;
					}
				}
			}
		}

		// look up for
		StoreCatalogueBean catalogue = getBeanFromServer(store, ENDPOINT_CATALOGUE + uuid, StoreCatalogueBean.class);
		return catalogue; // NOSONAR (kept local variable for readability)
	}
}
