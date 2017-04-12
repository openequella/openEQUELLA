package com.tle.web.api.payment;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.web.api.oauth.interfaces.beans.OAuthClientBean;
import com.tle.web.api.payment.beans.StoreFrontBean;
import com.tle.web.remoting.rest.service.UrlLinkService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

/**
 * CRUD for Store Front Registrations Currently NOT registered endpoint. See
 * Redmine #6615
 * 
 * @author Aaron
 */
@Bind
@Path("storefront")
@Api(value = "/storefront", description = "storefront")
@Produces({"application/json"})
@Singleton
@SuppressWarnings("nls")
public class StoreFrontResource
{
	@Inject
	private UrlLinkService urlLinkService;
	@Inject
	private StoreFrontService storeFrontService;
	@Inject
	private StoreFrontBeanSerializer serializer;

	@GET
	@Path("")
	@ApiOperation(value = "Get a list of store front registrations")
	public List<StoreFrontBean> getStoreFronts()
	{
		final List<StoreFront> sfs = storeFrontService.enumerateEditable();
		return Lists.transform(sfs, new Function<StoreFront, StoreFrontBean>()
		{
			@Override
			public StoreFrontBean apply(StoreFront sf)
			{
				StoreFrontBean bean = serializer.serialize(sf, null, false);
				final Map<String, String> links = Maps.newHashMap();
				links.put("self", getSelfLink(sf.getUuid()).toString());
				bean.set("links", links);
				return bean;
			}
		});
	}

	@POST
	@Path("")
	@Consumes("application/json")
	@ApiOperation(value = "Register a new store front")
	public Response registerStoreFront(StoreFrontBean store)
	{
		final StoreFront sf = convertStoreFrontBean(store, new StoreFront());
		sf.setUuid(UUID.randomUUID().toString());

		// Does one really need a pack???
		EntityPack<StoreFront> pack = new EntityPack<StoreFront>();
		pack.setEntity(sf);
		storeFrontService.add(pack, false);

		return Response.status(Status.CREATED).location(getSelfLink(sf.getUuid())).build();
	}

	@PUT
	@Path("{uuid}")
	@Consumes("application/json")
	@ApiOperation(value = "Update a store front registration")
	public Response updateStoreFront(@PathParam("uuid") String uuid, StoreFrontBean store)
	{
		EntityPack<StoreFront> pack = storeFrontService.startEdit(storeFrontService.getByUuid(uuid));
		StoreFront sf = pack.getEntity();
		convertStoreFrontBean(store, sf);
		storeFrontService.stopEdit(pack, true);

		return Response.status(Status.OK).location(getSelfLink(uuid)).build();
	}

	@GET
	@Path("{uuid}")
	@ApiOperation(value = "Get details on a store front registration")
	public StoreFrontBean getStoreFront(@PathParam("uuid") String uuid)
	{
		final StoreFront sf = storeFrontService.getByUuid(uuid);
		// Enforces EDIT_STOREFRONT
		storeFrontService.getReadOnlyPack(sf.getId());
		return serializer.serialize(sf, null, true);
	}

	@DELETE
	@Path("{uuid}")
	@ApiOperation(value = "Delete a store front registration")
	public Response deleteStoreFront(@PathParam("uuid") String uuid)
	{
		final StoreFront sf = storeFrontService.getByUuid(uuid);
		storeFrontService.delete(sf, false);
		return Response.status(Status.NO_CONTENT).build();
	}

	private URI getSelfLink(String uuid)
	{
		return urlLinkService.getMethodUriBuilder(getClass(), "getStoreFront").build(uuid);
	}

	// TODO: remove this bollocks
	public StoreFront convertStoreFrontBean(StoreFrontBean sfb, StoreFront sf)
	{
		if( sfb == null )
		{
			return null;
		}

		final Locale locale = CurrentLocale.getLocale();
		if( sfb.getNameStrings() != null )
		{
			sf.setName(LangUtils.createTextTempLangugageBundle(sfb.getNameStrings().getStrings()));
		}
		else
		{
			sf.setName(LangUtils.createTextTempLangugageBundle(sfb.getName().toString(), locale));
		}
		if( sfb.getDescriptionStrings() != null )
		{
			sf.setDescription(LangUtils.createTextTempLangugageBundle(sfb.getDescriptionStrings().getStrings()));
		}
		else
		{
			sf.setDescription(LangUtils.createTextTempLangugageBundle(sfb.getDescription().toString(), locale));
		}

		sf.setUuid(sfb.getUuid());
		sf.setClient(convertOAuthClientBean(sfb.getClient()));
		sf.setContactPhone(sfb.getContactPhone());
		sf.setCountry(sfb.getCountry());
		sf.setAllowFree(sfb.isFree());
		sf.setProduct(sfb.getProduct());
		sf.setProductVersion(sfb.getProductVersion());
		sf.setAllowPurchase(sfb.isPurchase());
		sf.setAllowSubscription(sfb.isSubscription());

		sf.setDateCreated(sfb.getCreatedDate());
		sf.setDateModified(sfb.getModifiedDate());

		sf.setDisabled(!sfb.isEnabled());

		return sf;
	}

	public OAuthClient convertOAuthClientBean(OAuthClientBean cb)
	{
		if( cb == null )
		{
			return null;
		}

		final OAuthClient client = new OAuthClient();
		client.setUuid(cb.getUuid());
		if( cb.getNameStrings() != null )
		{
			client.setName(LangUtils.createTextTempLangugageBundle(cb.getNameStrings().getStrings()));
		}
		else
		{
			String name = cb.getName() != null ? cb.getName().toString() : cb.getUuid();
			client.setName(LangUtils.createTextTempLangugageBundle(name, CurrentLocale.getLocale()));
		}
		client.setClientId(cb.getClientId());
		client.setRedirectUrl(cb.getRedirectUrl());
		client.setUserId(cb.getUserId());
		return client;
	}
}
