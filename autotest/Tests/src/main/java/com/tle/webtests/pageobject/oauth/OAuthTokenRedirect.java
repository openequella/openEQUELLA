package com.tle.webtests.pageobject.oauth;

import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.By;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class OAuthTokenRedirect extends AbstractPage<OAuthTokenRedirect>
{

	public OAuthTokenRedirect(PageContext context)
	{
		super(context, By.id("redirectresponse"));
	}

	public String getToken()
	{
		return driver.findElement(By.id("access_token")).getText();
	}

	public static String getRedirectUri(PageContext context)
	{
		return context.getTestConfig().getProperty("oauth.redirector.url");
	}

	public static <T extends AbstractPage<T>> T redirect(PageContext context, String clientId, AbstractPage<T> returnPage, String... otherParams)
	{
		List<BasicNameValuePair> params = Lists.newArrayList();
		params.add(new BasicNameValuePair("test_client_id", clientId));
		params.add(new BasicNameValuePair("test_equella_url", context.getBaseUrl()));
		for (int i=0; i<otherParams.length; i++)
		{
			params.add(new BasicNameValuePair(otherParams[i], otherParams[++i]));
		}
		String redirector = getRedirectUri(context) + "?" + URLEncodedUtils.format(params, "UTF-8");
		context.getDriver().get(redirector);
		return returnPage.get();
	}

	public String getError()
	{
		return driver.findElement(By.id("error")).getText();
	}
}
