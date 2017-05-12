package com.tle.webtests.pageobject.oauth;

import java.net.URI;
import java.util.Map;

import org.openqa.selenium.By;
import org.testng.Assert;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.URLUtils;
import com.tle.webtests.pageobject.AbstractPage;

public class OAuthDefaultRedirectPage extends AbstractPage<OAuthDefaultRedirectPage>
{
	private Map<String, String[]> clientParams;

	public OAuthDefaultRedirectPage(PageContext context)
	{
		super(context, By.id("oauthdefaultredirect"));
	}

	public String getClientSideToken()
	{
		Map<String, String[]> params = getClientParams();
		return params.get("access_token")[0];
	}

	public boolean hasAccessToken()
	{
		Map<String, String[]> params = getClientParams();
		return !Check.isEmpty(params.get("access_token"));
	}

	private Map<String, String[]> getClientParams()
	{
		if( clientParams == null )
		{
			clientParams = URLUtils.parseParamString(getFragment());
		}
		return clientParams;
	}

	public String getFragment()
	{
		String currentUrl = driver.getCurrentUrl();
		URI currentUri = URI.create(currentUrl);
		return currentUri.getFragment();
	}

	public String getErrorReason()
	{
		return getClientParams().get("error")[0];
	}

	public void assertToken()
	{
		Assert.assertTrue(hasAccessToken(), "Location did not contain an access token: " + getFragment());
	}

}
