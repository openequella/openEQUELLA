package com.example.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

/**
 * A simple URL manipulation utility class
 */
public abstract class UrlUtils
{
	/**
	 * Appends a query string to a URL, detecting if the URL already contains a
	 * query string
	 * 
	 * @param url
	 * @param queryString
	 * @return
	 */
	public static String appendQueryString(String url, String queryString)
	{
		return url
			+ (queryString == null || queryString.equals("") ? "" : (url.contains("?") ? '&' : '?') + queryString);
	}

	/**
	 * @param paramNameValues An even number of strings indicating the parameter
	 *            names and values i.e. "param1", "value1", "param2", "value2"
	 * @return
	 */
	public static String queryString(String... paramNameValues)
	{
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		if( paramNameValues != null )
		{
			if( paramNameValues.length % 2 != 0 )
			{
				throw new RuntimeException("Must supply an even number of paramNameValues");
			}
			for( int i = 0; i < paramNameValues.length; i += 2 )
			{
				params.add(new BasicNameValuePair(paramNameValues[i], paramNameValues[i + 1]));
			}
		}
		return queryString(params);
	}

	/**
	 * Turns a list of NameValuePair into a query string. e.g
	 * param1=val1&param2=val2
	 * 
	 * @param params
	 * @return
	 */
	public static String queryString(List<NameValuePair> params)
	{
		if( params == null )
		{
			return null;
		}
		return URLEncodedUtils.format(params, "UTF-8");
	}

	/**
	 * URL <em>path</em> encodes a value
	 * 
	 * @param value
	 * @return
	 */
	public static String urlPathEncode(String value)
	{
		try
		{
			String encodedUrl = URLEncoder.encode(value, "UTF-8");
			// Ensure forward slashes are still slashes
			encodedUrl = encodedUrl.replaceAll("%2F", "/");
			// Ensure that pluses are changed into the correct %20
			encodedUrl = encodedUrl.replaceAll("\\+", "%20");
			return encodedUrl;
		}
		catch( UnsupportedEncodingException e )
		{
			// Can't happen
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encodes a URL parameter value
	 * 
	 * @param value
	 * @return
	 */
	public static String urlParamEncode(String value)
	{
		try
		{
			return URLEncoder.encode(value, "UTF-8");
		}
		catch( UnsupportedEncodingException e )
		{
			// Can't happen
			throw new RuntimeException(e);
		}
	}
}
