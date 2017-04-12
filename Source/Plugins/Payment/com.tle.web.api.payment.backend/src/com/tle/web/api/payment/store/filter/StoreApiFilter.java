package com.tle.web.api.payment.store.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.edge.exceptions.WebException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.common.oauth.beans.OAuthClient;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.oauth.OAuthUserState;
import com.tle.core.oauth.dao.OAuthClientDao;
import com.tle.core.payment.StoreSettings;
import com.tle.core.payment.dao.StoreFrontDao;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.web.api.payment.store.CurrentStoreFront;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilter;
import com.tle.web.remoting.resteasy.RestEasyExceptionMapper;
import com.tle.web.remoting.resteasy.RestEasyExceptionMapper.ErrorResponse;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * Ensures that the caller is a registered storefront and puts the storefront
 * into a thread local object
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class StoreApiFilter implements WebFilter
{
	private static final Logger LOGGER = Logger.getLogger(StoreApiFilter.class);
	private static PluginResourceHelper resources = ResourcesService.getResourceHelper(StoreApiFilter.class);

	@Inject
	private StoreFrontDao storeFrontDao;
	@Inject
	private OAuthClientDao clientDao;
	@Inject
	private ConfigurationService configService;

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException
	{
		try
		{
			CurrentStoreFront.set(getStoreFront(request));
		}
		catch( WebException web )
		{
			ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
			try
			{
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

				final ErrorResponse err = RestEasyExceptionMapper.convertExceptionToJsonResponse(web.getCode(),
					RestEasyExceptionMapper.mapException(web));
				final int status = err.getCode();
				if( status >= 500 )
				{
					LOGGER.error("REST API error", web);
				}
				response.setStatus(status);
				new ObjectMapper().writeValue(response.getOutputStream(), err);
				return new FilterResult(true);
			}
			finally
			{
				Thread.currentThread().setContextClassLoader(oldLoader);
			}
		}
		return FilterResult.FILTER_CONTINUE;
	}

	protected StoreFront getStoreFront(HttpServletRequest request) throws WebException
	{
		if( !request.isSecure() )
		{
			throw new WebException(400, "ssl", resources.getString("error.ssl"));
		}

		// check that this instance is setup as a store
		final StoreSettings storeSettings = configService.getProperties(new StoreSettings());
		if( !storeSettings.getEnabled() )
		{
			throw new WebException(403, "not_store", resources.getString("error.notstore"));
		}

		final UserState userState = CurrentUser.getUserState();
		if( userState instanceof OAuthUserState )
		{
			final OAuthUserState oauthUserState = (OAuthUserState) userState;
			final OAuthClient client = oauthUserState.getClient(clientDao);
			final StoreFront sf = storeFrontDao.getByOAuthClient(client);
			if( sf == null )
			{
				throw new WebException(403, "no_storefront", resources.getString("error.nostorefrontforclient"));
			}
			if( sf.isDisabled() )
			{
				throw new WebException(403, "store_disabled", resources.getString("error.storedisabled"));
			}
			return sf;
		}
		throw new WebException(401, "oauth_required", resources.getString("error.oauthrequired"));
	}
}
