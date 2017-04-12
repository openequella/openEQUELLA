package com.tle.web.pss.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;

import com.dytech.edge.exceptions.WebException;
import com.tle.beans.system.PearsonScormServicesSettings;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.SystemUserState;
import com.tle.web.core.filter.UserStateResult;
import com.tle.web.core.filter.UserStateResult.Result;
import com.tle.web.oauth.filter.AbstractOAuthV1UserStateHook;

@Bind
@Singleton
public class PearsonScormServicesUserStateHook extends AbstractOAuthV1UserStateHook
{
	@Inject
	private ConfigurationService configurationService;

	@Override
	protected String getSecretFromKey(String key)
	{
		PearsonScormServicesSettings pssSettings = configurationService
			.getProperties(new PearsonScormServicesSettings());
		if( key.equals(pssSettings.getConsumerKey()) )
		{
			if( !pssSettings.isEnable() )
			{
				String errmsg = "Pearson SCORM Services is not enabled in this institution: "
					+ CurrentInstitution.get();
				throw new WebException(403, errmsg, errmsg);
			}

			return pssSettings.getConsumerSecret();
		}
		return null;
	}

	@Override
	protected OAuthMessage getOAuthMessage(HttpServletRequest request)
	{
		return OAuthServlet.getMessage(request, urlService.getUriForRequest(request, null).toString());
	}

	@Override
	protected UserStateResult getUserStateResult(HttpServletRequest request)
	{
		return new UserStateResult(new SystemUserState(null), Result.LOGIN);
	}
}
