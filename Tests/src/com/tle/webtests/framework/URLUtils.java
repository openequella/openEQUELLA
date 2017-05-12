package com.tle.webtests.framework;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.Assert;

import com.tle.common.Check;

public class URLUtils
{
	private static final String CHARSET_ENCODING = "UTF-8";

	/**
	 * Parse a full/partial url and return it as a map.
	 * <p>
	 * Anything before the query string will go into the map using the special
	 * key {@link SectionInfo#KEY_PATH}.
	 * <p>
	 * E.g.
	 * 
	 * <pre>
	 * Map&lt;String, String[]&gt; map = SectionUtils.parseParamUrl(&quot;/access/test.do?method=hello&quot;);
	 * String path = map.get(SectionInfo.PATH_KEY)[0];
	 * String method = map.get(&quot;method&quot;)[0];
	 * </pre>
	 * 
	 * <code>path</code> will be <code>"/access/test.do"</code><br>
	 * <code>method</code> will be <code>"hello"</code>
	 * 
	 * @see #parseParamString(String)
	 * @param url The url to parse
	 * @return The map of name to values
	 */
	public static Map<String, String[]> parseParamUrl(String url, String baseUrl)
	{
		if( baseUrl != null )
		{
			Assert.assertTrue(url.startsWith(baseUrl));
			url = url.substring(baseUrl.length());
		}

		Map<String, String[]> params;
		int qInd = url.indexOf('?');
		if( qInd != -1 )
		{
			String query = url.substring(qInd + 1);
			params = parseParamString(query);
			url = url.substring(0, qInd);
		}
		else
		{
			params = new LinkedHashMap<String, String[]>();
		}
		params.put("$PATH$", new String[]{url});
		return params;
	}

	/**
	 * Parse a query string and return a map of parameters.
	 * <p>
	 * Each unique parameter has an array of values stored in the map.
	 * <p>
	 * E.g.
	 * 
	 * <pre>
	 * Map&lt;String, String[]&gt; map = parseParamString(&quot;param1=value1&lt;param2=value2&amp;param1=value3&quot;);
	 * </pre>
	 * 
	 * will return a map containing:
	 * 
	 * <pre>
	 * param1 -> [value1, value3]
	 * param2 -> [value2]
	 * </pre>
	 * 
	 * @param query The query string to parse
	 * @return A map of name to value arrays
	 */
	public static Map<String, String[]> parseParamString(String query)
	{
		Map<String, String[]> paramMap = new LinkedHashMap<String, String[]>();

		if( !Check.isEmpty(query) )
		{
			try
			{
				String[] qparams = query.split("&");
				for( String qparam : qparams )
				{
					String[] nameVal = qparam.split("=");
					String name = URLDecoder.decode(nameVal[0], CHARSET_ENCODING);
					String value;
					if( nameVal.length > 1 )
					{
						value = URLDecoder.decode(nameVal[1], CHARSET_ENCODING);
					}
					else
					{
						value = "";
					}
					String[] current = paramMap.get(name);
					if( current != null )
					{
						String[] newstrs = new String[current.length + 1];
						System.arraycopy(current, 0, newstrs, 0, current.length);
						newstrs[current.length] = value;
						current = newstrs;
					}
					else
					{
						current = new String[]{value};
					}
					paramMap.put(name, current);
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
		return paramMap;
	}
}
